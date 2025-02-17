/**
 * ZT-XPN: An end-to-end Zero-Trust Architecture for Next Generation 
 * Programmable Networks
 * 
 * Authors:  Charalampos Katsis  (ckatsis@purdue.edu)
 *           Elisa Bertino       (bertino@purdue.edu)
 * =================================================================
 */


package org.p4sdn.app.pipeline.components.codeblocks;

import org.p4sdn.app.pipeline.BMV2Pipeline;
import org.p4sdn.app.pipeline.components.Action;
import org.p4sdn.app.pipeline.components.ConditionalBlock;
import org.p4sdn.app.pipeline.components.InLineStatement;

public class ParseChecker extends CodeBlock {

    public static final String NAME = "ParseChecker";
    public static final String PARSE_ERROR_CONDITION = BMV2Pipeline.STD_META_NAME + DOT + "parser_error" + 
                        NOT_EQUAL + BMV2Pipeline.P4CORE_ERROR_NOERROR;


    private boolean isRoportParsingErrorsToControlPlane = false;

    public ParseChecker(
            boolean isRoportParsingErrorsToControlPlane,
            Action dropAction
        ) {
        super(NAME);
        this.isRoportParsingErrorsToControlPlane = isRoportParsingErrorsToControlPlane;

        if (this.isRoportParsingErrorsToControlPlane == false) {
            ConditionalBlock blockParsingErrors = new ConditionalBlock(null, this);
            InLineStatement ret = new InLineStatement(blockParsingErrors, this, RETURN_STATEMENT);
            blockParsingErrors.addConditionAndStatements(PARSE_ERROR_CONDITION, dropAction.getInlineInvocation(), ret);
            addStatements(blockParsingErrors);
        }
    }
}
