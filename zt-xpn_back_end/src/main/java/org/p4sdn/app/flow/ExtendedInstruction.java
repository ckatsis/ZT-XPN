/**
 * ZT-XPN: An end-to-end Zero-Trust Architecture for Next Generation 
 * Programmable Networks
 * 
 * Authors:  Charalampos Katsis  (ckatsis@purdue.edu)
 *           Elisa Bertino       (bertino@purdue.edu)
 * =================================================================
 */


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

