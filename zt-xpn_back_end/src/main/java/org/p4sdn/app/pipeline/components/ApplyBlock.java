package org.p4sdn.app.pipeline.components;

import java.util.ArrayList;

import org.p4sdn.app.pipeline.components.codeblocks.CodeBlock;

public class ApplyBlock {

    public static final String APPLY_BLOCK_DECLARATION = "apply" + Component.SPACE;

    private Component associatedComponent;
    private ArrayList<CodeBlock> codeBlocls = new ArrayList<>();
    private int compileIndentation = 0;
    

    public ApplyBlock(Component associatedComponent) {
        this.associatedComponent = associatedComponent;
    }

    public ApplyBlock(Component associatedComponent, ArrayList<CodeBlock> cbs) {
        this.associatedComponent = associatedComponent;
        this.codeBlocls = cbs;
    }

    // public void addStatements(Statement... statements) {
    //     for(Statement s: statements){
    //         if(!this.codeBlocls.contains(s)) {
    //             this.codeBlocls.add(s);
    //         }
    //     }
    // }

    public void addCodeBlocks(CodeBlock... cbs) {
        for(CodeBlock cb: cbs){
            if(!this.codeBlocls.contains(cb)) {
                this.codeBlocls.add(cb);
            }
        }
    }

    public int getCodeBlockIndexByName(String cbName) {
        for (int i = 0; i < codeBlocls.size(); i++) {
            if (codeBlocls.get(i).getName().equals(cbName)) {
                return i;
            }
        }
        
        return -1;
    }

    public void setCodeblock(int index, CodeBlock codeBlock) {
        codeBlocls.set(index, codeBlock);
    }

    public CodeBlock getCodeBlockByName(String cbName) {
        for (CodeBlock cb: codeBlocls) {
            if (cb.getName().equals(cbName)) {
                return cb;
            }
        }
        
        return null;
    }

    public int getCompileIndentation() {
        return compileIndentation;
    }

    public void setCompileIndentation(int compileIndentation) {
        this.compileIndentation = compileIndentation;
    }

    @Override
    public String toString() {
        StringBuilder applyDeclaration = new StringBuilder();
        applyDeclaration.append(Component.generateIndentation(getCompileIndentation()));
        applyDeclaration.append(APPLY_BLOCK_DECLARATION);
        applyDeclaration.append(Component.LEFT_BRACE);
        applyDeclaration.append(Component.NEW_LINE);

        for(CodeBlock cb: codeBlocls) {
            cb.setCompileIndentation(getCompileIndentation() + 1);
            applyDeclaration.append(cb.compile());
            applyDeclaration.append(Component.NEW_LINE);
        }

        applyDeclaration.append(Component.generateIndentation(getCompileIndentation()));
        applyDeclaration.append(Component.RIGHT_BRACE);

        return applyDeclaration.toString();
    }

    public String compile() {
        return toString();
    }
}
