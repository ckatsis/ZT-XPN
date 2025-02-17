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
import java.util.List;

import org.p4sdn.app.exceptions.AnnotationComponentException;

public class Header extends Component {
    public static final ComponentType type = ComponentType.HEADER;
    public static final String HEADER_DECLARATION = "header" + SPACE;
    public static final String SET_VALID_FUNCTION_NAME = "setValid";
    public static final String SET_INVALID_FUNCTION_NAME = "setInvalid";
    public static final String IS_VALID_FUNCTION_NAME = "isValid";
    public static final String SET_VALID_FUNCTION = SET_VALID_FUNCTION_NAME + LEFT_PAR + RIGHT_PAR;
    public static final String IS_VALID_FUNCTION = IS_VALID_FUNCTION_NAME + LEFT_PAR + RIGHT_PAR;
    public static final String SET_INVALID_FUNCTION = SET_INVALID_FUNCTION_NAME + LEFT_PAR + RIGHT_PAR;

    private List<Variable> variables = new ArrayList<>();
    private Annotation annotation = null;
    private Header parentHeader = null;
    private Header childHeader = null;

    public Header(String name) {
        super((name + HEADER_EXTENSION).toLowerCase(), type);
        annotation = null;
    }

    public void addVariable(Variable var) {
        variables.add(var);
    }

    public void addAnnotation(Annotation annotation) throws AnnotationComponentException {
        if(!annotation.getAssociatedComponent().equals(this)) {
            throw new AnnotationComponentException(annotation.getName(), this.getClass());
        }
        this.annotation = annotation;
    }

    public Header getParentHeader() {
        return parentHeader;
    }

    public void setChildHeader(Header childHeader) {
        this.childHeader = childHeader;
        // childHeader.setParentHeader(this);
    }

    public void setParentHeader(Header parentHeader) {
        this.parentHeader = parentHeader;
        // parentHeader.setChildHeader(this);
    }
    
    @Override
    public String toString() {
        StringBuilder headerDeclaration = new StringBuilder();

        if(annotation != null) {
            headerDeclaration.append(annotation.compile());
        }
        
        headerDeclaration.append(NEW_LINE);
        headerDeclaration.append(HEADER_DECLARATION);
        headerDeclaration.append(getName());
        headerDeclaration.append(LEFT_BRACE);
        headerDeclaration.append(NEW_LINE);

        for(Variable v: variables){
            v.setCompileIndentation(getComponentIndentation() + 1);
            headerDeclaration.append(v.compile());
            headerDeclaration.append(NEW_LINE);
        }

        headerDeclaration.append(RIGHT_BRACE);

        return headerDeclaration.toString();
    }

    @Override
    public String compile() {
        return toString();
    }
}
