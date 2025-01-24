package org.p4sdn.app.flow;

import static org.onosproject.net.flow.instructions.Instruction.Type.PROTOCOL_INDEPENDENT;

import java.util.Objects;

public final class StateIdInstruction implements ExtendedInstruction {

    private final String SEPARATOR = ":";

    private final int stateId;

    public StateIdInstruction(int stateId) {
        this.stateId = stateId;
    }

    @Override
    public Type type() {
        return PROTOCOL_INDEPENDENT;
    }

    @Override
    public ExtendedType extendedType() {
        return ExtendedType.STATE;
    }

    public int stateId() {
        return stateId;
    }

    @Override
    public String toString() {
        return extendedType().toString() + SEPARATOR + stateId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(extendedType().ordinal(), stateId);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof StateIdInstruction) {
            StateIdInstruction that = (StateIdInstruction) obj;
            return Objects.equals(stateId, that.stateId);

        }
        return false;
    } 
}
