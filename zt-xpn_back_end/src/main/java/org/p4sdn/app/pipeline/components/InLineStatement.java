/**
 * ZT-XPN: An end-to-end Zero-Trust Architecture for Next Generation 
 * Programmable Networks
 * 
 * Authors:  Charalampos Katsis  (ckatsis@purdue.edu)
 *           Elisa Bertino       (bertino@purdue.edu)
 * =================================================================
 */



package org.p4sdn.app.pipeline.components;

public class InLineStatement extends Statement {

    private String statement;

    public InLineStatement(Statement associatedStatement, Component assosiatedComponent, String statement) {
        super(associatedStatement, assosiatedComponent);
        this.statement = statement;
    }
    
    @Override
    public String compile() {
        return toString();
    }
    
    @Override
    public String toString() {
        StringBuilder inLineStatement = new StringBuilder();
        inLineStatement.append(generateIndentation());
        inLineStatement.append(statement);
        inLineStatement.append(Component.STATEMENT_TERMINATOR);

        return inLineStatement.toString();
    }
}
