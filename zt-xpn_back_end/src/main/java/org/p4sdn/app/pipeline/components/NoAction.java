/**
 * ZT-XPN: An end-to-end Zero-Trust Architecture for Next Generation 
 * Programmable Networks
 * 
 * Authors:  Charalampos Katsis  (ckatsis@purdue.edu)
 *           Elisa Bertino       (bertino@purdue.edu)
 * =================================================================
 */


package org.p4sdn.app.pipeline.components;

public class NoAction extends Action {

    public static String NO_ACTION_NAME = "NoAction";

    public NoAction(Component associatedComponent) {
        super(NO_ACTION_NAME, associatedComponent);
        setExtern(true);
    }
}
