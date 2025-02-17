/**
 * ZT-XPN: An end-to-end Zero-Trust Architecture for Next Generation 
 * Programmable Networks
 * 
 * Authors:  Charalampos Katsis  (ckatsis@purdue.edu)
 *           Elisa Bertino       (bertino@purdue.edu)
 * =================================================================
 */


package org.p4sdn.app.pipeline.components;

public final class AcceptState extends State {

    public static final String ACCEPT_STATE = "accept";

    public AcceptState() {
        super(ACCEPT_STATE);
    }

    @Override
    public String toString() {
        return getName() + STATEMENT_TERMINATOR;
    }
}
