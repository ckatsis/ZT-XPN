package org.p4sdn.app.pipeline.components.codeblocks;

import org.p4sdn.app.pipeline.BMV2Pipeline;
import org.p4sdn.app.pipeline.components.Action;
import org.p4sdn.app.pipeline.components.ConditionalBlock;
import org.p4sdn.app.pipeline.components.ControllerHeader;
import org.p4sdn.app.pipeline.components.Header;
import org.p4sdn.app.pipeline.components.InLineStatement;
import org.p4sdn.app.pipeline.components.Struct;

public class RecirculationChecker extends CodeBlock {

    public static final String NAME = "RecirculationChecker";

    public RecirculationChecker(Struct parsedHeaders, Header packetOutHdr, Header packetInHeader) {
        super(NAME);

        ConditionalBlock recircCondition = new ConditionalBlock(null, this);

        InLineStatement inValidPcktIn = new InLineStatement(recircCondition, this, 
                            parsedHeaders.getName().replace(STRUCT_EXTENSION, "") + DOT 
                            + packetInHeader.getName().replace(HEADER_EXTENSION, "") + DOT 
                            + Header.SET_INVALID_FUNCTION);

        recircCondition.addConditionAndStatements( 
            parsedHeaders.getName().replace(STRUCT_EXTENSION, "") + DOT + packetOutHdr.getName().replace(HEADER_EXTENSION, "") + DOT 
            + Header.IS_VALID_FUNCTION
            + AND + 
            parsedHeaders.getName().replace(STRUCT_EXTENSION, "") + DOT + packetOutHdr.getName().replace(HEADER_EXTENSION, "") + DOT 
            + ControllerHeader.PACKET_OUT_EGRESS_PORT + IS_EQUAL + BMV2Pipeline.BMV2_RECIRCULATION_PORT,
            inValidPcktIn,
            Action.recirculatePacket(0, recircCondition, this));
        
        addStatements(recircCondition);
    }    
}
