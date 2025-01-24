package org.p4sdn.app.pipeline.components;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.onosproject.net.flow.criteria.Criterion;
import org.onosproject.net.flow.criteria.Criterion.Type;
import org.p4sdn.app.net.Protocol;
import org.p4sdn.app.pipeline.BMV2Pipeline;
import org.p4sdn.app.pipeline.Pipeline;
import org.p4sdn.app.pipeline.components.codeblocks.CodeBlock;
import org.p4sdn.app.pipeline.components.codeblocks.FlowStateChecker;
import org.p4sdn.app.pipeline.components.codeblocks.PacketOutChecker;
import org.p4sdn.app.pipeline.components.codeblocks.RecirculationChecker;
import org.p4sdn.app.pipeline.components.codeblocks.TableExecutor;

public class Control extends Component {
    public static final String DROP_ACTION_NAME = "dropPacket";
    public static final String SEND_TO_CONTROLLER_ACTION_NAME = "send_to_controller";
    public static final String SEND_TO_EGRESS_PORT_PARAMETER_NAME = "out_port";
    public static final int SEND_TO_EGRESS_PORT_PARAMETER_NAME_LEN = BMV2Pipeline.BMV2_PORT_LEN;
    public static final String SET_OUT_PORT_ACTION_NAME = "set_out_port";
    public static final String STATEFUL_PROC_ACTION_NAME = "stateful_packet_processing";
    public static final ComponentType type = ComponentType.CONTROL;
    public static final String CONTROL_DECLARATION = "control" + SPACE;
    public static final String ACL_TABLE_NAME = "acl_t";
    public static final String L2_FWD_TABLE_NAME = "l2_fwd_t";
    public static final String FLOW_STATE_REGISTER_NAME = "flow_state_register";
    public static final int FLOW_STATE_REGISTER_BITWIDTH = 1;
    public static final String IS_STATEFUL_FLOW_VAR_NAME = "isStatefulFlow";
    public static final String IS_STATEFUL_FLOW_PARAM_NAME = "isStatefulFlow_param";
    public static final String IS_STATEFUL_PROCESSING_VAR_NAME = "isStatefulProcessing";
    public static final String IS_DEPENDENT_VAR_NAME = "isDependentFlow";
    public static final String IS_DEPENDENT_PARAM_NAME = "isDependentFlow_param";
    public static final int BOOL_VAR_SIZE = 1;
    public static final String STATE_INDEX_VAR_NAME = "state_index";
    public static final String STATE_INDEX_PARAM_NAME = "state_index_param";
    public static final int STATE_INDEX_VAR_SIZE = 32;
    public static final String DEPENDENCY_STATE_INDEX_VAR_NAME = "dependency_state_index";
    public static final String DEPENDENCY_STATE_INDEX_PARAM_NAME = "dependency_state_index_param";
    public static final String DEPENDENCY_STATE_VALUE_VAR_NAME = "dependency_state_value";


    private ArrayList<ControlParameter> parameters = new ArrayList<>();
    private ArrayList<Action> actions = new ArrayList<>();
    private ArrayList<Table> tables = new ArrayList<>();
    private ApplyBlock applyBlock;
    private Register flowRegister;
    private ArrayList<Variable> controlVariables = new ArrayList<>();


    public Control(String name) {
        super(name, type);
        this.applyBlock = new ApplyBlock(this);
    }

    public Control(String name, ArrayList<ControlParameter> parameters, ArrayList<Action> actions,
            ArrayList<Table> tables, ApplyBlock applyBlock) {
        super(name, type);
        this.parameters = parameters;
        this.actions = actions;
        this.tables = tables;
        this.applyBlock = applyBlock;
    }

    public void addControlVariables(Variable... vars) {
        for (Variable v: vars) {
            if (!this.controlVariables.contains(v)) {
                this.controlVariables.add(v);
            }
        }
    }

    public void addParameters(ControlParameter... parameters) {
        for(ControlParameter p: parameters){
            if(!this.parameters.contains(p))
                this.parameters.add(p);
        }
    }

    public void addActions(Action... actions) {
        for(Action a: actions){
            if(!this.actions.contains(a))
                this.actions.add(a);
        }
    }

    public void addTables(Table... tables) {
        for(Table t: tables) {
            if(!this.tables.contains(t))
                this.tables.add(t);
        }
    }

    public ApplyBlock getApplyBlock() {
        return applyBlock;
    }

    public void setApplyBlock(ApplyBlock block) {
        this.applyBlock = block;
    }

    public Action generateDropAction() { // struct.getName().replace(STRUCT_EXTENSION, "")
        Action markToDrop = new MarkToDropAction(this);
        Action dropAction = new Action(DROP_ACTION_NAME, this);
        InLineStatement dropStatement = markToDrop.getInlineInvocation(BMV2Pipeline.STD_META_NAME);
        dropAction.addStatements(dropStatement);
        addActions(dropAction);

        return dropAction;
    }

    public Action generateSendToControllerAction() {
        Action sendToController = new Action(SEND_TO_CONTROLLER_ACTION_NAME, this);
        // standard_metadata.egress_spec = CPU_PORT;
        StringBuilder setEgressPort = new StringBuilder();
        setEgressPort.append(BMV2Pipeline.STD_META_EGRESS_SPEC_NAME + 
                                Component.EQUALS + BMV2Pipeline.CPU_PORT_DECLARATION);
        InLineStatement setEgressPortStatement = new InLineStatement(null, 
                                    sendToController, setEgressPort.toString());
        // hdr.packet_in.setValid();
        StringBuilder setPcktInHdrValid = new StringBuilder();
        setPcktInHdrValid.append(BMV2Pipeline.PARSED_HEADERS_NAME + DOT + ControllerHeader.PACKET_IN_HDR + DOT
                                    + Header.SET_VALID_FUNCTION);
        InLineStatement setPcktInHdrValidStatement = new InLineStatement(null, sendToController, 
                                    setPcktInHdrValid.toString());
        // hdr.packet_in.ingress_port = standard_metadata.ingress_port;
        StringBuilder setPcktInIngressPort = new StringBuilder();
        setPcktInIngressPort.append(BMV2Pipeline.PARSED_HEADERS_NAME + DOT + ControllerHeader.PACKET_IN_HDR + DOT 
                                + ControllerHeader.PACKET_IN_INGRESS_PORT
                                + EQUALS + BMV2Pipeline.STD_META_EGRESS_INGRESS_PORT_NAME);
        InLineStatement setPcktInIngressPortStatement = new InLineStatement(null, 
                                    sendToController, setPcktInIngressPort.toString());
        
        sendToController.addStatements(setEgressPortStatement, setPcktInHdrValidStatement, setPcktInIngressPortStatement);
        addActions(sendToController);

        return sendToController;
    }

    public void generateFlowStateRegister() {
        if (flowRegister == null) {
            flowRegister = new Register(FLOW_STATE_REGISTER_NAME, FLOW_STATE_REGISTER_BITWIDTH, this);
        }
    }

    public Action generateSetOutPortAction() {
        Action setOutPortAction = new Action(SET_OUT_PORT_ACTION_NAME, this);
        ActionParameter outPort = new ActionParameter(SEND_TO_EGRESS_PORT_PARAMETER_NAME, 
                                        SEND_TO_EGRESS_PORT_PARAMETER_NAME_LEN, 
                                        setOutPortAction);
        setOutPortAction.addParameters(outPort);
        StringBuilder setOutPort = new StringBuilder();
        setOutPort.append(BMV2Pipeline.STD_META_EGRESS_SPEC_NAME + Component.EQUALS + SEND_TO_EGRESS_PORT_PARAMETER_NAME);
        InLineStatement setOutPortStatement = new InLineStatement(null, setOutPortAction, setOutPort.toString());
        setOutPortAction.addStatements(setOutPortStatement);
        addActions(setOutPortAction);

        return setOutPortAction;
    }

    public Action generateStatefulProcessingAction (
        Variable isStatefulProc, Variable isStatefulFlow, Variable stateId, 
        Variable isDependent, Variable depStateId) {
        Action statefulPacketProcAction = new Action(STATEFUL_PROC_ACTION_NAME, this);
        ActionParameter isStatefulFlowActionParameter = new ActionParameter(IS_STATEFUL_FLOW_PARAM_NAME, 
                                BOOL_VAR_SIZE, statefulPacketProcAction);
        ActionParameter index = new ActionParameter(STATE_INDEX_PARAM_NAME, STATE_INDEX_VAR_SIZE, 
                                statefulPacketProcAction);
        ActionParameter isDep = new ActionParameter(IS_DEPENDENT_PARAM_NAME, BOOL_VAR_SIZE, statefulPacketProcAction);
        ActionParameter depIndex = new ActionParameter(DEPENDENCY_STATE_INDEX_PARAM_NAME, 
                                STATE_INDEX_VAR_SIZE, statefulPacketProcAction);
        ActionParameter outPort = new ActionParameter(SEND_TO_EGRESS_PORT_PARAMETER_NAME, 
                SEND_TO_EGRESS_PORT_PARAMETER_NAME_LEN, 
                statefulPacketProcAction);
        statefulPacketProcAction.addParameters(isStatefulFlowActionParameter, index, isDep,
                depIndex, outPort);           
        InLineStatement isStateFulProc = new InLineStatement(null, statefulPacketProcAction,
                    isStatefulProc.getName() + Component.EQUALS + "1"); 
        InLineStatement setIsStateful = new InLineStatement(null, statefulPacketProcAction,
                    isStatefulFlow.getName() + Component.EQUALS + IS_STATEFUL_FLOW_PARAM_NAME);
        InLineStatement setStateId = new InLineStatement(null, statefulPacketProcAction,
                    stateId.getName() + Component.EQUALS + STATE_INDEX_PARAM_NAME);
        InLineStatement setIsDependent = new InLineStatement(null, statefulPacketProcAction,
                    isDependent.getName() + Component.EQUALS + IS_DEPENDENT_PARAM_NAME);
        InLineStatement setDepStateId = new InLineStatement(null, statefulPacketProcAction,
                    depStateId.getName() + Component.EQUALS + DEPENDENCY_STATE_INDEX_PARAM_NAME);
        InLineStatement setPort = new InLineStatement(null, statefulPacketProcAction,
                    BMV2Pipeline.STD_META_EGRESS_SPEC_NAME + Component.EQUALS + SEND_TO_EGRESS_PORT_PARAMETER_NAME);
        statefulPacketProcAction.addStatements(isStateFulProc, setIsStateful, setStateId, setIsDependent, 
                    setDepStateId, setPort);
        addActions(statefulPacketProcAction);

        return statefulPacketProcAction;
    }

    public Table getTable(String name) {
        for(Table t: tables) {
            if(t.getName().equals(name)) {
                return t;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        StringBuilder controlBlockDeclaration = new StringBuilder();
        controlBlockDeclaration.append(generateIndentation());
        controlBlockDeclaration.append(CONTROL_DECLARATION);
        controlBlockDeclaration.append(getName());
        controlBlockDeclaration.append(LEFT_PAR);

        for(int i = 0; i < parameters.size(); i++) {
            controlBlockDeclaration.append(parameters.get(i).compile());

            if(i + 1 < parameters.size())
                controlBlockDeclaration.append(COMMA);
        }

        controlBlockDeclaration.append(RIGHT_PAR);
        controlBlockDeclaration.append(LEFT_BRACE);
        controlBlockDeclaration.append(NEW_LINE);
        int controBodyIndent = getComponentIndentation() + 1;

        // Compile any control variables and registers
        // Regs:
        if (flowRegister != null) {
            flowRegister.setCompileIndentation(controBodyIndent);
            controlBlockDeclaration.append(flowRegister.compile()); 
            controlBlockDeclaration.append(NEW_LINE);
        }

        // Vars:
        for(Variable v: controlVariables) {
            v.setCompileIndentation(controBodyIndent);
            controlBlockDeclaration.append(v.compile()); 
            controlBlockDeclaration.append(NEW_LINE);
        }

        for(Action a: actions) {
            if(a.isExtern() == true) // We skip extern decalrations as they are already declared
                continue;
            a.setCompileIndentation(controBodyIndent);
            controlBlockDeclaration.append(a.compile());
            controlBlockDeclaration.append(NEW_LINE);
        }

        for(Table t: tables) {
            t.setCompileIndentation(controBodyIndent);
            controlBlockDeclaration.append(t.compile());
            controlBlockDeclaration.append(NEW_LINE);
        }

        applyBlock.setCompileIndentation(controBodyIndent);
        controlBlockDeclaration.append(applyBlock.compile());
        controlBlockDeclaration.append(NEW_LINE);
        controlBlockDeclaration.append(generateIndentation());
        controlBlockDeclaration.append(RIGHT_BRACE);

        return controlBlockDeclaration.toString();
    }

    @Override
    public String compile() {
        return toString();
    }

    public Action getActionByName(String actionName) {
        for(Action action: this.actions) {
            if(action.getName().equals(actionName))
                return action;
        }
        
        return null;
    }

    public Set<Type> processFwdRequirement(String parseHdrPrefix, Protocol protocol) 
                throws IllegalArgumentException, IllegalAccessException {
        Set<Criterion.Type> generatedCiteriaTypes = new HashSet<>();
        Table fwdTable = getTable(L2_FWD_TABLE_NAME);

        if (fwdTable == null) {
            fwdTable = new Table(L2_FWD_TABLE_NAME, this, true);

            for (Action action: actions)
                fwdTable.addActions(action);
                
            Action drop = getActionByName(DROP_ACTION_NAME);
            fwdTable.setDefaultAction(drop);
            addTables(fwdTable);
            ApplyBlock applyBlock = getApplyBlock();
            applyBlock.addCodeBlocks(new TableExecutor(fwdTable));
        }

        String prefix = parseHdrPrefix + protocol.getName().toLowerCase() + Component.DOT;
        // Now parse the protocol (i.e., the protocol features)
        List<String> properties = protocol.getAssignedProperties(prefix);
        
        for(String property: properties) {
            // We assume ternary match for all key members
            KeyEntry newEntry = new KeyEntry(property, MatchKind.TERNARY);
            fwdTable.addKeyEntry(newEntry);
            Criterion.Type type =  Protocol.getCriterionType(protocol.getName(), Pipeline.removePrefix(property, prefix));

            if (type != null)
                generatedCiteriaTypes.add(type);
        }

        return generatedCiteriaTypes;
    }

    public Set<Type> processACLRequirement(String parseHdrPrefix, Protocol protocol) 
            throws IllegalArgumentException, IllegalAccessException {
        
        Set<Criterion.Type> generatedCiteriaTypes = new HashSet<>();
        // Check to see if there is an ACL table
        Table aclTable = getTable(ACL_TABLE_NAME);

        if(aclTable == null) {
            aclTable = new Table(ACL_TABLE_NAME, this, true);
            
            for(Action action: actions)
                aclTable.addActions(action);
                
            Action drop = getActionByName(DROP_ACTION_NAME);
            aclTable.setDefaultAction(drop);
            addTables(aclTable);
            ApplyBlock applyBlock = getApplyBlock();
            applyBlock.addCodeBlocks(new TableExecutor(aclTable));
        }

        String prefix = parseHdrPrefix + protocol.getName().toLowerCase() + Component.DOT;
        // Now parse the protocol (i.e., the protocol features)
        List<String> properties = protocol.getAssignedProperties(prefix);
        
        for(String property: properties) {
            // We assume ternary match for all key members
            KeyEntry newEntry = new KeyEntry(property, MatchKind.TERNARY);
            aclTable.addKeyEntry(newEntry);
            Criterion.Type type =  Protocol.getCriterionType(protocol.getName(), Pipeline.removePrefix(property, prefix));

            if (type != null)
                generatedCiteriaTypes.add(type);
        }

        return generatedCiteriaTypes;
    }

    public void processStatefulACLRequirement(
        Protocol protocol, Control egressControl,
        Struct parsedHeaders, Header packetOutHeader,
        Struct stdMeta, Header packetInHeader) {

        if (!protocol.isKeepState()) {
            return;            
        }

        if (flowRegister == null) { // Stateful packet processing logic has not been added
            // Add variables and flow state register
            generateFlowStateRegister();
            Variable isStatefulProcessing = new Variable(IS_STATEFUL_PROCESSING_VAR_NAME, false, BOOL_VAR_SIZE, "0");
            Variable isStatefulFlow = new Variable(IS_STATEFUL_FLOW_VAR_NAME, false, BOOL_VAR_SIZE, "0");
            Variable stateIndex = new Variable(STATE_INDEX_VAR_NAME, false, STATE_INDEX_VAR_SIZE, "0");
            Variable isDependent = new Variable(IS_DEPENDENT_VAR_NAME, false, BOOL_VAR_SIZE, "0");
            Variable dependencyIndex = new Variable(DEPENDENCY_STATE_INDEX_VAR_NAME, false, STATE_INDEX_VAR_SIZE, "0");
            Variable dependencyValue = new Variable(DEPENDENCY_STATE_VALUE_VAR_NAME, FLOW_STATE_REGISTER_BITWIDTH);
            addControlVariables(isStatefulProcessing, isStatefulFlow, stateIndex, isDependent, dependencyIndex, dependencyValue);

            // Generate the apply plock for the stateful packet processing
            ApplyBlock ingressApplyBlock = getApplyBlock();
            Action dropAction = getActionByName(DROP_ACTION_NAME);
            FlowStateChecker flowCheckerBlock = new FlowStateChecker(isStatefulProcessing, isStatefulFlow, stateIndex, 
                isDependent, dependencyIndex, dependencyValue, dropAction, flowRegister, parsedHeaders, packetOutHeader);
            ingressApplyBlock.addCodeBlocks(flowCheckerBlock);

            // Gnerate the action that sets the aforementioned variables
            Action stateful_proc_action = generateStatefulProcessingAction(isStatefulProcessing, isStatefulFlow, 
                stateIndex, isDependent, dependencyIndex);
            
            Table aclTable = getTable(ACL_TABLE_NAME);
            aclTable.addActions(stateful_proc_action);
            
            // We need to replace the packet out processing in the egress with a statefull one
            CodeBlock packetOutChecker = ingressApplyBlock.getCodeBlockByName(PacketOutChecker.NAME);
            if (packetOutChecker != null) {
                PacketOutChecker checkerObj = (PacketOutChecker) packetOutChecker;

                if (!checkerObj.isSupportReactiveForwarding()) {
                    Action setEgressAction = getActionByName(SET_OUT_PORT_ACTION_NAME);
                    int index = ingressApplyBlock.getCodeBlockIndexByName(PacketOutChecker.NAME);
                    PacketOutChecker statefulPcktOut = 
                        new PacketOutChecker(true, parsedHeaders, packetOutHeader, setEgressAction);
                    ingressApplyBlock.setCodeblock(index, statefulPcktOut);
                }
            }

            RecirculationChecker recirculationBlock = new RecirculationChecker(parsedHeaders, packetOutHeader, packetInHeader);
            ApplyBlock egressApplyBlock = egressControl.getApplyBlock(); 
            egressApplyBlock.addCodeBlocks(recirculationBlock); // Add it to egress control
        }

        int stateId = flowRegister.allocateState();
        protocol.setStateId(stateId);
    }
}
