package org.p4sdn.app.flow;

import static org.onosproject.net.flow.instructions.Instruction.Type.PROTOCOL_INDEPENDENT;

import java.util.Objects;

public final class DependencyStateIdInstruction implements ExtendedInstruction {

    private static final String SEPARATOR = ":";
    private final int dependencyStateId;

    public DependencyStateIdInstruction(int dependencyStateId) {
        this.dependencyStateId = dependencyStateId;
    }

    public int dependencyStateId() {
        return dependencyStateId;
    }

    @Override
    public Type type() {
        return PROTOCOL_INDEPENDENT;
    }

    @Override
    public ExtendedType extendedType() {
        return ExtendedType.DEPENDENCY_STATE;
    }

    @Override
    public String toString() {
        return extendedType().toString() + SEPARATOR + dependencyStateId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(extendedType().ordinal(), dependencyStateId);
    }


    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof DependencyStateIdInstruction) {
            DependencyStateIdInstruction that = (DependencyStateIdInstruction) obj;
            return Objects.equals(dependencyStateId, that.dependencyStateId);

        }
        return false;
    }        
    
}
