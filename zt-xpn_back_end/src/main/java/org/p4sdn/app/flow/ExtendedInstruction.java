package org.p4sdn.app.flow;

import org.onosproject.net.flow.instructions.Instruction;

public interface ExtendedInstruction extends Instruction {

    enum ExtendedType {
        /**
         * Signifies that this is a stateful objective.
         */
        STATE,

        /**
         * Signifies that there is a dependency on 
         * another flow's state
         */
        DEPENDENCY_STATE,

        STATEFUL_PROCESSING
    }

    /**
     * Returns the extended type of instruction.
     *
     * @return extended type of instruction
     */
    ExtendedType extendedType();
    
} 

