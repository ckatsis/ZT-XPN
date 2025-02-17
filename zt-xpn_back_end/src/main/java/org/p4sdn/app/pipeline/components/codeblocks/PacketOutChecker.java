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
import org.p4sdn.app.pipeline.components.ControllerHeader;
import org.p4sdn.app.pipeline.components.Header;
import org.p4sdn.app.pipeline.components.InLineStatement;
import org.p4sdn.app.pipeline.components.Struct;

public class PacketOutChecker extends CodeBlock {

    public static final String NAME = "PacketOutChecker";

    private boolean supportReactiveForwarding = false;

    /**
     * Packet Out Handler.
     * 
     * @param parsedHeaders
     * @param packetOutHdr
     * @param setEgressAction
     */
    public PacketOutChecker(
        boolean supportReactiveForwarding, 
        Struct parsedHeaders, 
        Header packetOutHdr, 
        Action setEgressAction) {

        super(NAME);

        this.supportReactiveForwarding = supportReactiveForwarding;
        packetOutHandler(parsedHeaders, packetOutHdr, setEgressAction);   
    }

    private void packetOutHandler(
        Struct parsedHeaders, 
        Header packetOutHdr, 
        Action setEgressAction) {

        if (this.supportReactiveForwarding) {

            ConditionalBlock packetOutCondition = new ConditionalBlock(null, this);
            ConditionalBlock egressCondition = new ConditionalBlock(packetOutCondition, this);

            InLineStatement setEgressStatement = setEgressAction.getInlineInvocation(
                            parsedHeaders.getName().replace(STRUCT_EXTENSION, "") + DOT 
                            + packetOutHdr.getName().replace(HEADER_EXTENSION, "") + DOT 
                            + ControllerHeader.PACKET_OUT_EGRESS_PORT
                        );
            InLineStatement inValidPcktOut = new InLineStatement(packetOutCondition, this, 
                                parsedHeaders.getName().replace(STRUCT_EXTENSION, "") + DOT 
                                + packetOutHdr.getName().replace(HEADER_EXTENSION, "") + DOT 
                                + Header.SET_INVALID_FUNCTION
                            );
            InLineStatement ret = new InLineStatement(packetOutCondition, this, RETURN_STATEMENT);

            egressCondition.addConditionAndStatements(
                parsedHeaders.getName().replace(STRUCT_EXTENSION, "") + DOT + packetOutHdr.getName().replace(HEADER_EXTENSION, "") +
                DOT + ControllerHeader.PACKET_OUT_EGRESS_PORT + NOT_EQUAL + BMV2Pipeline.BMV2_RECIRCULATION_PORT,
                setEgressStatement, inValidPcktOut, ret);
            
            ConditionalBlock isRecirculate = new ConditionalBlock(egressCondition, this);
            setEgressStatement = setEgressAction.getInlineInvocation(BMV2Pipeline.BMV2_RECIRCULATION_PORT);
            isRecirculate.addConditionAndStatements(
                BMV2Pipeline.STD_META_INSTANCE_TYPE_NAME + NOT_EQUAL + "4", 
                // setEgressStatement, 
                ret);
            // isRecirculate.addConditionAndStatements(null, 
            //     setEgressStatement, ret);
            
            egressCondition.addConditionAndStatements(null, isRecirculate);

            String isPacketOutCondition = parsedHeaders.getName().replace(STRUCT_EXTENSION, "") + DOT 
                            + packetOutHdr.getName().replace(HEADER_EXTENSION, "") + DOT 
                            + Header.IS_VALID_FUNCTION;
            packetOutCondition.addConditionAndStatements(isPacketOutCondition, 
                egressCondition);

            addStatements(packetOutCondition);
            
        } else {
            // this.supportReactiveForwarding = false;

            ConditionalBlock packetOutCondition = new ConditionalBlock(null, this);
            String condition = parsedHeaders.getName().replace(STRUCT_EXTENSION, "") + DOT 
                            + packetOutHdr.getName().replace(HEADER_EXTENSION, "") + DOT 
                            + Header.IS_VALID_FUNCTION;
            InLineStatement setEgressStatement = setEgressAction.getInlineInvocation(
                            parsedHeaders.getName().replace(STRUCT_EXTENSION, "") + DOT 
                            + packetOutHdr.getName().replace(HEADER_EXTENSION, "") + DOT 
                            + ControllerHeader.PACKET_OUT_EGRESS_PORT
                        );
            InLineStatement inValidPcktOut = new InLineStatement(packetOutCondition, this, 
                                parsedHeaders.getName().replace(STRUCT_EXTENSION, "") + DOT 
                                + packetOutHdr.getName().replace(HEADER_EXTENSION, "") + DOT 
                                + Header.SET_INVALID_FUNCTION
                            );
            InLineStatement ret = new InLineStatement(packetOutCondition, this, RETURN_STATEMENT);
            packetOutCondition.addConditionAndStatements(condition, setEgressStatement, inValidPcktOut, ret);        
            addStatements(packetOutCondition);
        }
    }

    /**
     * Call this constructor when threre are requirements for stateful packet processing. 
     * 
     * @param parsedHeaders
     * @param packetOutHdr
     * @param setEgressAction
     * @param std_meta
     */
    // public PacketOutChecker(Struct parsedHeaders, Header packetOutHdr, Action setEgressAction) {
    //     super(NAME);

    //     supportReactiveForwarding = true;

    //     ConditionalBlock packetOutCondition = new ConditionalBlock(null, this);
    //     ConditionalBlock egressCondition = new ConditionalBlock(packetOutCondition, this);

    //     InLineStatement setEgressStatement = setEgressAction.getInlineInvocation(
    //                     parsedHeaders.getName().replace(STRUCT_EXTENSION, "") + DOT 
    //                     + packetOutHdr.getName().replace(HEADER_EXTENSION, "") + DOT 
    //                     + ControllerHeader.PACKET_OUT_EGRESS_PORT
    //                 );
    //     InLineStatement inValidPcktOut = new InLineStatement(packetOutCondition, this, 
    //                         parsedHeaders.getName().replace(STRUCT_EXTENSION, "") + DOT 
    //                         + packetOutHdr.getName().replace(HEADER_EXTENSION, "") + DOT 
    //                         + Header.SET_INVALID_FUNCTION
    //                     );
    //     InLineStatement ret = new InLineStatement(packetOutCondition, this, RETURN_STATEMENT);

    //     egressCondition.addConditionAndStatements(
    //         parsedHeaders.getName().replace(STRUCT_EXTENSION, "") + DOT + packetOutHdr.getName().replace(HEADER_EXTENSION, "") +
    //         DOT + ControllerHeader.PACKET_OUT_EGRESS_PORT + NOT_EQUAL + BMV2Pipeline.BMV2_RECIRCULATION_PORT,
    //         setEgressStatement, inValidPcktOut, ret);
        
    //     ConditionalBlock isRecirculate = new ConditionalBlock(egressCondition, this);
    //     setEgressStatement = setEgressAction.getInlineInvocation(BMV2Pipeline.BMV2_RECIRCULATION_PORT);
    //     isRecirculate.addConditionAndStatements(
    //         BMV2Pipeline.STD_META_INSTANCE_TYPE_NAME + NOT_EQUAL + "4", 
    //         // setEgressStatement, 
    //         ret);
    //     // isRecirculate.addConditionAndStatements(null, 
    //     //     setEgressStatement, ret);
        
    //     egressCondition.addConditionAndStatements(null, isRecirculate);

    //     String isPacketOutCondition = parsedHeaders.getName().replace(STRUCT_EXTENSION, "") + DOT 
    //                     + packetOutHdr.getName().replace(HEADER_EXTENSION, "") + DOT 
    //                     + Header.IS_VALID_FUNCTION;
    //     packetOutCondition.addConditionAndStatements(isPacketOutCondition, 
    //         egressCondition);

    //     addStatements(packetOutCondition);
    // }

    public boolean isSupportReactiveForwarding() {
        return supportReactiveForwarding;
    }    
    
}
