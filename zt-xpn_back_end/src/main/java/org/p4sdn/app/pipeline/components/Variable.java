/**
 * ZT-XPN: An end-to-end Zero-Trust Architecture for Next Generation 
 * Programmable Networks
 * 
 * Authors:  Charalampos Katsis  (ckatsis@purdue.edu)
 *           Elisa Bertino       (bertino@purdue.edu)
 * =================================================================
 */


package org.p4sdn.app.pipeline.components;

public class Variable extends Component {

    public static final ComponentType type = ComponentType.VARIABLE;
    public static final String VARIABLE_DECLARATION = "bit";
    public static final String CONST_DECLARATION = "const" + SPACE;
    
    private boolean isConst;
    private int len;
    private String value;
    private boolean hasValue;
    private boolean includeStatementTerminator = true;
    
    // Just variable declaration
    public Variable(String name, int len) {
        super(name, type);
        this.isConst = false; // it cannot be const in this case
        this.len = len;
        hasValue = false;
    }

    
    public Variable(String name, boolean isConst, int len, String value) {
        super(name, type);
        this.isConst = isConst;
        this.len = len;
        this.value = value;
        hasValue = true;
    } 

    public void setIncludeStatementTerminator(boolean includeStatementTerminator) {
        this.includeStatementTerminator = includeStatementTerminator;
    }

    @Override
    public String toString() {
        StringBuilder variableDeclaration = new StringBuilder();
        variableDeclaration.append(generateIndentation());
        
        if (isConst) {
            variableDeclaration.append(CONST_DECLARATION);
        }

        variableDeclaration.append(VARIABLE_DECLARATION);
        variableDeclaration.append("<" + len + ">" + SPACE);
        variableDeclaration.append(getName());

        if (hasValue) {
            variableDeclaration.append(EQUALS);
            variableDeclaration.append(value);
        }

        if(includeStatementTerminator)
            variableDeclaration.append(Component.STATEMENT_TERMINATOR);

        return variableDeclaration.toString();
    }


    @Override
    public String compile() {
        return toString();
    }
}
