/**
 * ZT-XPN: An end-to-end Zero-Trust Architecture for Next Generation 
 * Programmable Networks
 * 
 * Authors:  Charalampos Katsis  (ckatsis@purdue.edu)
 *           Elisa Bertino       (bertino@purdue.edu)
 * =================================================================
 */



package org.p4sdn.app.pipeline.components;

public class StructMember extends Component {

    public static final ComponentType type = ComponentType.STRUCT_MEMBER;

    private Header hdr; // A member of a struct in P4 is an instance of a header.
    
    public StructMember(String name, Header hdr) {
        super(name, type);
        this.hdr = hdr;
    }

    public Header getHdr() {
        return hdr;
    }

    @Override
    public String toString() {
        return hdr.getName() + SPACE + getName();
    }

    @Override
    public String compile() {
        return toString();
    }
}