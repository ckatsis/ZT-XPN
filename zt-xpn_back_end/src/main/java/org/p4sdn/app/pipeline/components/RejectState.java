/**
 * ZT-XPN: An end-to-end Zero-Trust Architecture for Next Generation 
 * Programmable Networks
 * 
 * Authors:  Charalampos Katsis  (ckatsis@purdue.edu)
 *           Elisa Bertino       (bertino@purdue.edu)
 * =================================================================
 */


package org.p4sdn.app.pipeline.components;

public final class RejectState extends State {
    public static final String REJECT_STATE = "reject";

    public RejectState() {
        super(REJECT_STATE);
    }

    @Override
    public String toString() {
        return getName() + STATEMENT_TERMINATOR;
    }
}
