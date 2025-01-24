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
