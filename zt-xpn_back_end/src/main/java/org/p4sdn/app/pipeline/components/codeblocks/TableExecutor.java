package org.p4sdn.app.pipeline.components.codeblocks;

import org.p4sdn.app.pipeline.components.InLineStatement;
import org.p4sdn.app.pipeline.components.Table;

public class TableExecutor extends CodeBlock {

    public static final String NAME = "TableExecutor";
    public static final String TABLE_APPLY_INVOCATION = DOT + "apply" + LEFT_PAR + RIGHT_PAR;

    public TableExecutor(Table table) {
        super(NAME);
        StringBuilder applyTable = new StringBuilder();
        applyTable.append(table.getName() + TABLE_APPLY_INVOCATION);
        InLineStatement applyTableStatement = new InLineStatement(null, this, applyTable.toString());
        addStatements(applyTableStatement);
    }

}
