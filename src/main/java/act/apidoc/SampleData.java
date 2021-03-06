package act.apidoc;

/*-
 * #%L
 * ACT Framework
 * %%
 * Copyright (C) 2014 - 2018 ActFramework
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import javax.inject.Provider;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Namespace
 */
public abstract class SampleData {
    private SampleData() {}

    /**
     * Mark on a field specify the {@link SampleDataProvider} that
     * should be used to generate sample data for the field been marked
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD, ElementType.PARAMETER})
    public @interface ProvidedBy {
        Class<? extends Provider> value();
    }

    /**
     * Mark on a field specify the list of string that
     * can be randomly choosen as sample data for the field
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD, ElementType.PARAMETER})
    public @interface StringList {
        String[] value();
    }

    /**
     * Mark on a field specify the list of integer that
     * can be randomly choosen as sample data for the field
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD, ElementType.PARAMETER})
    public @interface IntList {
        int[] value();
    }

    /**
     * Mark on a field specify the list of double that
     * can be randomly choosen as sample data for the field
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD, ElementType.PARAMETER})
    public @interface DoubleList {
        double[] value();
    }

    /**
     * Mark on an implementation class of {@link SampleDataProvider}
     * to specify the sample data category the provider applied
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.TYPE, ElementType.FIELD, ElementType.PARAMETER})
    public @interface Category {
        SampleDataCategory value();
    }

    /**
     * Mark on an implementation class of {@link SampleDataProvider}
     * to specify the sample data locale the provider applied
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.TYPE, ElementType.FIELD, ElementType.PARAMETER})
    public @interface Locale {
        String value();
    }

}
