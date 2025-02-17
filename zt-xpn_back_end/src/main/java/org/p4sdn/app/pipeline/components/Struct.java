/**
 * ZT-XPN: An end-to-end Zero-Trust Architecture for Next Generation 
 * Programmable Networks
 * 
 * Authors:  Charalampos Katsis  (ckatsis@purdue.edu)
 *           Elisa Bertino       (bertino@purdue.edu)
 * =================================================================
 */


package org.p4sdn.app.pipeline.components;

import java.util.ArrayList;
import java.util.Comparator;

public class Struct extends Component{

    public static final ComponentType type = ComponentType.STRUCT;
    public static final String STRUCT_DECLARATION = "struct" + SPACE;

    private ArrayList<StructMember> memberList = new ArrayList<>();

    public Struct(String name) {
        super((name + STRUCT_EXTENSION).toLowerCase(), type);
    }

    public void addMember(Header hdr) {
        StructMember entry = new StructMember(hdr.getName().replace(HEADER_EXTENSION, ""), hdr);
        memberList.add(entry);
    }

    public void sortMemberList() {
        memberList.sort(Comparator.comparing(obj -> getParentDepth(obj)));
    }

    // Helper method to calculate the depth of parent-child relationship
    private static int getParentDepth(StructMember member) {
        int depth = 0;
        Header obj = member.getHdr();
        while (obj.getParentHeader() != null) {
            obj = obj.getParentHeader();
            depth++;
        }
        return depth;
    }

    public ArrayList<StructMember> getMemberList() {
        return memberList;
    }

    public void addMember(String name, Header hdr) {
        StructMember entry = new StructMember(name, hdr);
        memberList.add(entry);
    }

    @Override
    public String toString() {
        StringBuilder structDeclaration = new StringBuilder();
        structDeclaration.append(generateIndentation());
        structDeclaration.append(STRUCT_DECLARATION);
        structDeclaration.append(getName());
        structDeclaration.append(LEFT_BRACE);
        structDeclaration.append(NEW_LINE);
        int structBodyIndent = getComponentIndentation() + 1;

        for(int i = 0; i < memberList.size(); i++) {
            structDeclaration.append(generateIndentation(structBodyIndent));
            structDeclaration.append(memberList.get(i).toString());
            structDeclaration.append(STATEMENT_TERMINATOR);             
            structDeclaration.append(NEW_LINE);
        }
        structDeclaration.append(generateIndentation());
        structDeclaration.append(RIGHT_BRACE);

        return structDeclaration.toString();
    }

    @Override
    public String compile() {
        sortMemberList();
        return toString();
    }
} 
