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
