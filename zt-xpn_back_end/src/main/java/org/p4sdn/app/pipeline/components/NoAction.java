package org.p4sdn.app.pipeline.components;

public class NoAction extends Action {

    public static String NO_ACTION_NAME = "NoAction";

    public NoAction(Component associatedComponent) {
        super(NO_ACTION_NAME, associatedComponent);
        setExtern(true);
    }
}
