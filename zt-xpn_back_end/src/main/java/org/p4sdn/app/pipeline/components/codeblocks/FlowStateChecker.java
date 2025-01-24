package org.p4sdn.app.pipeline.components.codeblocks;

import org.p4sdn.app.pipeline.components.Action;
import org.p4sdn.app.pipeline.components.ConditionalBlock;
import org.p4sdn.app.pipeline.components.Header;
import org.p4sdn.app.pipeline.components.InLineStatement;
import org.p4sdn.app.pipeline.components.Register;
import org.p4sdn.app.pipeline.components.Struct;
import org.p4sdn.app.pipeline.components.Variable;

public class FlowStateChecker extends CodeBlock {

    public static String NAME = "FlowStateChecker";

    public FlowStateChecker(
        Variable statefulProcessing,
        Variable isStatefulFlow,
        Variable stateIndex,
        Variable isDependent,
        Variable dependencyIndex,
        Variable dependencyValue,
        Action dropAction,
        Register stateReg,
        Struct parsedHeaders,
        Header packetOutHdr
        ) {
        super(NAME);

        ConditionalBlock hasDependencyBlock = new ConditionalBlock(null, this);
        ConditionalBlock checkDependencyBlock = new ConditionalBlock(hasDependencyBlock, this);
        ConditionalBlock isStatefulBlock = new ConditionalBlock(checkDependencyBlock, this);
        isStatefulBlock.addConditionAndStatements(
            isStatefulFlow.getName() + IS_EQUAL + "1", 
            stateReg.inLineWrite(isStatefulBlock, this, stateIndex, "1"));


        checkDependencyBlock.addConditionAndStatements(
            dependencyValue.getName() + IS_EQUAL + "0",
            dropAction.getInlineInvocation()
        );
        checkDependencyBlock.addConditionAndStatements(null, isStatefulBlock);

        hasDependencyBlock.addConditionAndStatements(
            isDependent.getName() + IS_EQUAL + "1", 
            stateReg.inLineRead(hasDependencyBlock, this, dependencyValue, dependencyIndex),
            checkDependencyBlock
        );
        hasDependencyBlock.addConditionAndStatements(null, 
            stateReg.inLineWrite(hasDependencyBlock, this, stateIndex, "1")
        );

        ConditionalBlock flowStateChecker = new ConditionalBlock(null, this);
        InLineStatement inValidPcktOut = new InLineStatement(flowStateChecker, this, 
                            parsedHeaders.getName().replace(STRUCT_EXTENSION, "") + DOT 
                            + packetOutHdr.getName().replace(HEADER_EXTENSION, "") + DOT 
                            + Header.SET_INVALID_FUNCTION
                        );

        flowStateChecker.addConditionAndStatements(
            statefulProcessing.getName() + IS_EQUAL + "1",
            inValidPcktOut,
            hasDependencyBlock
        );
        
        addStatements(flowStateChecker);
    }
}
