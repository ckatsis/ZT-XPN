/**
 * ZT-XPN: An end-to-end Zero-Trust Architecture for Next Generation 
 * Programmable Networks
 * 
 * Authors:  Charalampos Katsis  (ckatsis@purdue.edu)
 *           Elisa Bertino       (bertino@purdue.edu)
 * =================================================================
 */



package org.p4sdn.app.pipeline.components;

public class ActionParameter extends Parameter {

    public static final ComponentType TYPE = ComponentType.ACTION_PARAMETER;
    
    private Variable parameter;

    public ActionParameter(String name, int len, Component associatedComponent) {
        super(name, TYPE, associatedComponent);
        this.parameter = new Variable(name, len);
    }

    @Override
    public String toString() {
        StringBuilder actionParameter = new StringBuilder();
        this.parameter.setIncludeStatementTerminator(false); // Do not print ";"
        actionParameter.append(this.parameter.compile());

        return actionParameter.toString();
    }


    @Override
    public String compile() {
        return toString();
    }

}
