package org.p4sdn.app.flow;

import static org.onosproject.net.flow.instructions.Instruction.Type.PROTOCOL_INDEPENDENT;

public class StatefulProcessingInstruction implements ExtendedInstruction {

    private static final String SEPARATOR = ":";
    
    private final boolean keepState;
    private final int stateId;
    private final boolean isDependent;
    private final int dependencyStateId;
    private final long outputPort;

    public StatefulProcessingInstruction(
            boolean keepState, 
            int stateId, 
            boolean isDependent, 
            int dependencyStateId,
            long outputPort) {
        this.keepState = keepState;
        this.stateId = stateId;
        this.isDependent = isDependent;
        this.dependencyStateId = dependencyStateId;
        this.outputPort = outputPort;
    }

    @Override
    public Type type() {
        return PROTOCOL_INDEPENDENT;
    }

    @Override
    public ExtendedType extendedType() {
        return ExtendedType.STATEFUL_PROCESSING;
    }

    public boolean isKeepState() {
        return keepState;
    }

    public int getStateId() {
        return stateId;
    }

    public boolean isDependent() {
        return isDependent;
    }

    public int getDependencyStateId() {
        return dependencyStateId;
    }

    public long getOutputPort() {
        return outputPort;
    }

    @Override
    public String toString() {
        return extendedType().toString() + SEPARATOR + " [keepState=" + keepState + ", stateId=" + stateId + ", isDependent="
                + isDependent + ", dependencyStateId=" + dependencyStateId + ", outputPort=" + outputPort + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (keepState ? 1231 : 1237);
        result = prime * result + stateId;
        result = prime * result + (isDependent ? 1231 : 1237);
        result = prime * result + dependencyStateId;
        result = prime * result + (int) (outputPort ^ (outputPort >>> 32));
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        StatefulProcessingInstruction other = (StatefulProcessingInstruction) obj;
        if (keepState != other.keepState)
            return false;
        if (stateId != other.stateId)
            return false;
        if (isDependent != other.isDependent)
            return false;
        if (dependencyStateId != other.dependencyStateId)
            return false;
        if (outputPort != other.outputPort)
            return false;
        return true;
    }
}
