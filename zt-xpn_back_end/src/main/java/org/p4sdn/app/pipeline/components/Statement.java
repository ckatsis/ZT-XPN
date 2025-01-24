package org.p4sdn.app.pipeline.components;

public abstract class Statement {

    private int compileIndentation = 0;
    private Statement associatedStatement;
    private Component assosiatedComponent;

    public Statement(Statement associatedStatement, Component assosiatedComponent) {
        this.associatedStatement = associatedStatement;
        this.assosiatedComponent = assosiatedComponent;
    }

    public abstract String compile();

    public void setCompileIndentation(int compileIndentation) {
        this.compileIndentation = compileIndentation;
    }

    public int getStatementIndentation() {
        return compileIndentation;
    }

    public String generateIndentation() {
        StringBuilder indent = new StringBuilder();
        
        for(int i = 0; i < compileIndentation; i++)
            indent.append(Component.INDENTATION);
        
        return indent.toString();
    }
}
