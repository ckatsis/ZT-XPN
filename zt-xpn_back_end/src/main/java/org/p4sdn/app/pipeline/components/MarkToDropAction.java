package org.p4sdn.app.pipeline.components;

public class MarkToDropAction extends Action {

    public static String MARK_TO_DROP_ACTION_NAME = "mark_to_drop";


    public MarkToDropAction(Component assComponent) {
        super(MARK_TO_DROP_ACTION_NAME, assComponent);
        setExtern(true); // Provided by the V1 model
        // Parameter par = new Parameter(ParameterDirection.INOUT, stdMeta, assComponent);
        // addParameters(par);
    }
}
