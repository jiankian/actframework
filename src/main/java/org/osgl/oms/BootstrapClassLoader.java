package org.osgl.oms;

import org.osgl._;
import org.osgl.oms.asm.ClassReader;
import org.osgl.oms.asm.ClassWriter;
import org.osgl.oms.util.BytecodeVisitor;
import org.osgl.oms.util.Jars;
import org.osgl.util.C;
import org.osgl.util.E;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.osgl.oms.Constants.ASM_PKG;
import static org.osgl.oms.Constants.OMS_HOME;
import static org.osgl.oms.Constants.OMS_PKG;

/**
 * This class loader is responsible for loading OMS classes
 */
public class BootstrapClassLoader extends ClassLoader {

    private File lib;
    private File plugin;

    private Map<String, byte[]> libBC = C.newMap();
    private Map<String, byte[]> pluginBC = C.newMap();
    private List<Class<?>> pluginClasses = C.newList();

    public BootstrapClassLoader(ClassLoader parent) {
        super(parent);
        preload();
    }

    public BootstrapClassLoader() {
        this(_getParent());
    }

    private static ClassLoader _getParent() {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        if (null == cl) {
            cl = ClassLoader.getSystemClassLoader();
        }
        return cl;
    }

    private void preload() {
        String omsHome = System.getProperty(OMS_HOME);
        if (null == omsHome) {
            omsHome = System.getenv(OMS_HOME);
        }
        if (null == omsHome) {
            omsHome = guessHome();
        }
        File home = new File(omsHome);
        lib = new File(home, "lib");
        plugin = new File(home, "plugin");
        verifyHome();
        buildIndex();
    }

    private String guessHome() {
        return new File(".").getAbsolutePath();
    }

    private void verifyHome() {
        if (!verifyDir(lib) || !verifyDir(plugin)) {
            throw E.unexpected("Cannot load OMS: can't find lib or plugin dir");
        }
    }

    private boolean verifyDir(File dir) {
        return dir.exists() && dir.canExecute() && dir.isDirectory();
    }

    private void buildIndex() {
        libBC.putAll(Jars.buildClassNameIndex(lib));
        pluginBC.putAll(Jars.buildClassNameIndex(plugin));
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        Class<?> c = findLoadedClass(name);
        if (c != null) {
            return c;
        }

        if (!protectedClasses.contains(name)) {
            c = loadOmsClass(name, resolve, false);
        }

        if (null == c) {
            return super.loadClass(name, resolve);
        } else {
            return c;
        }
    }

    public List<Class<?>> pluginClasses() {
        for (String className: C.list(pluginBC.keySet())) {
            Class<?> c = loadOmsClass(className, true, true);
            assert null != c;
            pluginClasses.add(c);
        }
        return C.list(pluginClasses);
    }

    private Class<?> loadOmsClass(String name, boolean resolve, boolean pluginOnly) {
        boolean fromPlugin = false;
        byte[] ba = pluginBC.remove(name);
        if (null == ba) {
            if (!pluginOnly) {
                ba = libBC.remove(name);
            }
        } else {
            fromPlugin = true;
        }

        if (null == ba) {
            if (pluginOnly) {
                return findLoadedClass(name);
            }
            return null;
        }

        Class<?> c = null;
        if (!name.startsWith(OMS_PKG) || name.startsWith(ASM_PKG)) {
            // skip bytecode enhancement for asm classes or non oms classes
            c = super.defineClass(name, ba, 0, ba.length, DOMAIN);
        }

        if (null == c) {
            _.Var<ClassWriter> cw = _.val(null);
            BytecodeVisitor enhancer = OMS.enhancerManager().generalEnhancer(name, cw);
            if (null == enhancer) {
                c = super.defineClass(name, ba, 0, ba.length, DOMAIN);
            } else {
                ClassWriter w = new ClassWriter(0);
                cw.set(w);
                enhancer.commitDownstream();
                ClassReader r;
                r = new ClassReader(ba);
                try {
                    r.accept(enhancer, 0);
                    byte[] baNew = w.toByteArray();
                    c = super.defineClass(name, baNew, 0, baNew.length, DOMAIN);
                } catch (RuntimeException e) {
                    throw e;
                } catch (Error e) {
                    throw e;
                } catch (Exception e) {
                    throw E.unexpected("Error processing class " + name);
                }
            }
        }
        if (resolve) {
            super.resolveClass(c);
        }
        if (fromPlugin) {
            pluginClasses.add(c);
        }
        return c;
    }

    public Class<?> createClass(String name, byte[] b) throws ClassFormatError {
        return super.defineClass(name, b, 0, b.length, DOMAIN);
    }

    private static java.security.ProtectionDomain DOMAIN;

    static {
        DOMAIN = (java.security.ProtectionDomain)
                java.security.AccessController.doPrivileged(
                        new java.security.PrivilegedAction() {
                            public Object run() {
                                return BootstrapClassLoader.class.getProtectionDomain();
                            }
                        });
    }

    private static final Set<String> protectedClasses = C.set(
            BootstrapClassLoader.class.getName()
            //Plugin.class.getName(),
            //ClassFilter.class.getName()
    );
}
