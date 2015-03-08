package org.osgl.oms.handler.builtin.controller;

import org.osgl.mvc.result.Result;
import org.osgl.oms.app.AppContext;

/**
 * Dispatch request to real controller action method
 */
public class ControllerAction extends ActionHandler<ControllerAction> {

    private ActionHandlerInvoker handlerInvoker;

    public ControllerAction(ActionHandlerInvoker invoker) {
        super(-1);
        this.handlerInvoker = invoker;
    }

    @Override
    public Result handle(AppContext appContext) {
        return handlerInvoker.handle(appContext);
    }
}
