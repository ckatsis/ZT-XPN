/**
 * ZT-XPN: An end-to-end Zero-Trust Architecture for Next Generation 
 * Programmable Networks
 * 
 * Authors:  Charalampos Katsis  (ckatsis@purdue.edu)
 *           Elisa Bertino       (bertino@purdue.edu)
 * =================================================================
 */



package org.p4sdn.app.pipeline.components;

public class ControlParameter  extends Parameter {

    public static final ComponentType TYPE = ComponentType.CONTROL_PARAMETER;

    private ParameterDirection direction;
    private Struct struct;
    /**
     * Constructor that enforces naming convention for parameters
     * @param direction
     * @param struct
     */
    public ControlParameter(ParameterDirection direction, Struct struct, Component associatedComponent) {
        super(struct.getName().replace(STRUCT_EXTENSION, ""), TYPE, associatedComponent);
        this.direction = direction;
    
        if(direction == ParameterDirection.PACKET_IN || direction == ParameterDirection.PACKET_OUT) {
            this.struct = null; // ignore header
        } else {
           this.struct = struct;
        }
    }

    /**
     * Only use this constructor to overwrite the naming convention for parameters
     * @param direction
     * @param struct
     * @param name
     * @param associatedComponent
     */
    public ControlParameter(ParameterDirection direction, Struct struct, String name, Component associatedComponent) {
        super(name, TYPE, associatedComponent);
        this.direction = direction;
        
        if(direction == ParameterDirection.PACKET_IN || direction == ParameterDirection.PACKET_OUT) {
            this.struct = null; // ignore header
        } else {
           this.struct = struct;
        }
    }

    

    @Override
    public String toString() {
        StringBuilder parameterDeclaration = new StringBuilder();

        // if(direction == ParameterDirection.PACKET_IN)
        //     parameterDeclaration.append(PACKET_IN);
        // else if(direction == ParameterDirection.PACKET_OUT)
        //     parameterDeclaration.append(PACKET_OUT);
        // else if (direction == ParameterDirection.IN)
        //     parameterDeclaration.append(IN);
        // else if (direction == ParameterDirection.OUT)
        //     parameterDeclaration.append(OUT);
        // else
        //     parameterDeclaration.append(INOUT);
        parameterDeclaration.append(direction.toString().toLowerCase());
        parameterDeclaration.append(SPACE);
        
        if(struct != null) {
            parameterDeclaration.append(struct.getName());
            parameterDeclaration.append(SPACE);
        }
        
        parameterDeclaration.append(getName());

        return parameterDeclaration.toString();
    }


    @Override
    public String compile() {
        return toString();
    }
}
