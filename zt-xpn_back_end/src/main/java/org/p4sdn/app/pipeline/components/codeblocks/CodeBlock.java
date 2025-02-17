/**
 * ZT-XPN: An end-to-end Zero-Trust Architecture for Next Generation 
 * Programmable Networks
 * 
 * Authors:  Charalampos Katsis  (ckatsis@purdue.edu)
 *           Elisa Bertino       (bertino@purdue.edu)
 * =================================================================
 */


package org.p4sdn.app.pipeline.components.codeblocks;

import java.util.ArrayList;

import org.p4sdn.app.pipeline.components.Component;
import org.p4sdn.app.pipeline.components.ComponentType;
import org.p4sdn.app.pipeline.components.Statement;

public abstract class CodeBlock extends Component {

    public static final ComponentType TYPE = ComponentType.CODE_BLOCK;
    public static final String RETURN_STATEMENT = "return";
    
    private ArrayList<Statement> statements = new ArrayList<>();

    protected CodeBlock(String name) {
        super(name, TYPE);
    }

    public void addStatements(Statement... statements) {
        for(Statement s: statements){
            if(!this.statements.contains(s)) {
                this.statements.add(s);
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder codeBlock = new StringBuilder();

        for(Statement s: statements) {
            s.setCompileIndentation(getComponentIndentation());
            codeBlock.append(s.compile());
            codeBlock.append(NEW_LINE);
        }

        return codeBlock.toString();
    }

    @Override
    public String compile() {
        return toString();
    }
}
