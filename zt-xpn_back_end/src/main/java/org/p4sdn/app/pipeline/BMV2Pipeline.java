package org.p4sdn.app.pipeline;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.onosproject.net.flow.criteria.Criterion;
import org.p4sdn.app.Requirement;
import org.p4sdn.app.net.Protocol;
import org.p4sdn.app.net.Switch;
import org.p4sdn.app.pipeline.components.Action;
import org.p4sdn.app.pipeline.components.ApplyBlock;
import org.p4sdn.app.pipeline.components.Component;
import org.p4sdn.app.pipeline.components.Control;
import org.p4sdn.app.pipeline.components.ControllerHeader;
import org.p4sdn.app.pipeline.components.Define;
import org.p4sdn.app.pipeline.components.Header;
import org.p4sdn.app.pipeline.components.Inlcude;
import org.p4sdn.app.pipeline.components.ControlParameter;
import org.p4sdn.app.pipeline.components.ParameterDirection;
import org.p4sdn.app.pipeline.components.Parser;
import org.p4sdn.app.pipeline.components.State;
import org.p4sdn.app.pipeline.components.Struct;
import org.p4sdn.app.pipeline.components.Variable;
import org.p4sdn.app.pipeline.components.codeblocks.CodeBlock;
import org.p4sdn.app.pipeline.components.codeblocks.PacketEmitter;
import org.p4sdn.app.pipeline.components.codeblocks.PacketOutChecker;
import org.p4sdn.app.pipeline.components.codeblocks.ParseChecker;
import org.slf4j.Logger;


public class BMV2Pipeline extends Pipeline {
    private static Logger log;

    public static final String BMV2_PIPELINE_DECLARATION = "V1Switch";
    public static final String PIPELINE_INSTANCE_DECLARATION = Component.SPACE + "main" + Component.STATEMENT_TERMINATOR;
    public static final String BMV2_CPU_PORT = "255";
    public static final String BMV2_RECIRCULATION_PORT = "256";
    public static final int BMV2_PORT_LEN = 9;
    public static final String PARSED_HEADERS_NAME = "parsed_headers";
    public static final String LOCAL_META_NAME = "local_metadata";
    public static final String STD_META_NAME = "standard_metadata";
    public static final String P4CORE_ERROR_STRUCT_NAME = "error";
    public static final String P4CORE_ERROR_NOMATCH = P4CORE_ERROR_STRUCT_NAME + Component.DOT + "NoMatch";
    public static final String P4CORE_ERROR_NOERROR = P4CORE_ERROR_STRUCT_NAME + Component.DOT + "NoError";
    public static final String INGRESS_PORT = "ingress_port";
    public static final String START_STATE_TRANSITION_CONDITION = BMV2Pipeline.STD_META_NAME + Component.DOT + BMV2Pipeline.INGRESS_PORT;
    public static final String EXTRACT_PREFIX = PARSED_HEADERS_NAME + Component.DOT;
    public static final String INGRESS_CHECKSUM_NAME = "IngressCheckSumCheck";
    public static final String INGRESS_CONTROL_NAME = "IngressControl";
    public static final String EGRESS_CONTROL_NAME = "EgressControl";
    public static final String EGRESS_CHECKSUM_NAME = "EgressComputeChecksum";
    public static final String RETURN_STATEMENT = "return";
    public static final String STD_META_EGRESS_SPEC_NAME = STD_META_NAME + Component.DOT + "egress_spec";
    public static final String STD_META_EGRESS_INGRESS_PORT_NAME = STD_META_NAME + Component.DOT + "ingress_port";
    public static final String STD_META_INSTANCE_TYPE_NAME = STD_META_NAME + Component.DOT + "instance_type";
    public static final String DEPARSED_PACKET_PARAMETER_NAME = "packet";

    private ArrayList<Inlcude> includes = new ArrayList<>();
    private ArrayList<Define> defines = new ArrayList<>();
    private ArrayList<Variable> programGlobalVariables = new ArrayList<>();
    private ArrayList<Header> headers = new ArrayList<>();
    private ArrayList<Struct> structs = new ArrayList<>();
    private Parser parser;
    private ArrayList<Control> controlBlocks = new ArrayList<>();
    private Struct parsedHeaders;
    private Struct localMetadata;
    private Struct stdMetadata;

    public BMV2Pipeline(String name, PipelineRole pipelineRole, boolean reportParsingErrors, Switch sw) {
        super(name, pipelineRole, reportParsingErrors, sw);
        init();
    }

    public static void addLogger(Logger l) {
        log = l;
    }

    /**
     * Generates a minimal BMv2 pipeline with only the required components.
     */
    private void init() {
        // Compiler include directives
        addIncludes(
            new Inlcude("core.p4"), 
            new Inlcude("v1model.p4")
        );
        // CPU port const declaration
        addGlobalProgramVariables(new Variable(CPU_PORT_DECLARATION, true, BMV2_PORT_LEN, BMV2_CPU_PORT));
        // Struct with parsed headers
        this.parsedHeaders = new Struct(PARSED_HEADERS_NAME);
        this.localMetadata = new Struct(LOCAL_META_NAME);
        this.stdMetadata = new Struct(STD_META_NAME);
        this.stdMetadata.setExtern(true); // This is an extern stuct.
        addStructs(parsedHeaders, localMetadata, stdMetadata);
        // We need to declare the packet-in and packet-out headers
        Header pckt_in = ControllerHeader.generatePacketInHeader();
        Header pckt_out = ControllerHeader.generatePacketOutHeader();
        addHeaders(pckt_in, pckt_out);
        // Instantiating the parser
        parser = new Parser(PARSER_NAME, START_STATE_TRANSITION_CONDITION, 
                EXTRACT_PREFIX + pckt_out.getName().replace(Header.HEADER_EXTENSION, ""));
        parser.addParameters(
            new ControlParameter(ParameterDirection.OUT, this.parsedHeaders, parser),
            new ControlParameter(ParameterDirection.INOUT, this.localMetadata, parser),
            new ControlParameter(ParameterDirection.INOUT, this.stdMetadata, parser)
        );

        State start = parser.getStartState();
        State packet_out = parser.getPacketOutState();
        start.addNextState(CPU_PORT_DECLARATION, packet_out);
        // We need to create the rest of the pipeline
        Control checkSum = new Control(INGRESS_CHECKSUM_NAME);
        checkSum.addParameters(
            new ControlParameter(ParameterDirection.INOUT, this.parsedHeaders, checkSum),
            new ControlParameter(ParameterDirection.INOUT, this.localMetadata, checkSum)
        );

        Control ingressControl = new Control(INGRESS_CONTROL_NAME);
        ingressControl.addParameters(
            new ControlParameter(ParameterDirection.INOUT, this.parsedHeaders, parser),
            new ControlParameter(ParameterDirection.INOUT, this.localMetadata, parser),
            new ControlParameter(ParameterDirection.INOUT, this.stdMetadata, parser)
        );

        // Standard actions for the ingress
        Action dropAction = ingressControl.generateDropAction();
        ingressControl.generateSendToControllerAction();
        Action setOutputAction = ingressControl.generateSetOutPortAction();
        ApplyBlock block = ingressControl.getApplyBlock();

        if (isRoportParsingErrorsToControlPlane() == false) {
            // 1. We need an error checker in the Ingress apply block:
            CodeBlock parseErrorHandler = new ParseChecker(false, dropAction);
            block.addCodeBlocks(parseErrorHandler);

            // 2. We need a parsing error state at the parser.
            parser.enableEncapsulationErrors();
        }

        // Implementing the packet out block 
        CodeBlock packetOutHandler = new PacketOutChecker(false, parsedHeaders, pckt_out, setOutputAction);
        block.addCodeBlocks(packetOutHandler);

        Control egressControl = new Control(EGRESS_CONTROL_NAME);
        egressControl.addParameters(
            new ControlParameter(ParameterDirection.INOUT, this.parsedHeaders, parser),
            new ControlParameter(ParameterDirection.INOUT, this.localMetadata, parser),
            new ControlParameter(ParameterDirection.INOUT, this.stdMetadata, parser)
        );

        Control egressCheckSum = new Control(EGRESS_CHECKSUM_NAME);
        egressCheckSum.addParameters(
            new ControlParameter(ParameterDirection.INOUT, this.parsedHeaders, parser),
            new ControlParameter(ParameterDirection.INOUT, this.localMetadata, parser)
        );

        Control deparser = new Control(DEPARSER_NAME);
        deparser.addParameters(
            new ControlParameter(ParameterDirection.PACKET_OUT, null, DEPARSED_PACKET_PARAMETER_NAME, parser),
            new ControlParameter(ParameterDirection.IN, this.parsedHeaders, parser)
        );

        addControlBlocks(checkSum, ingressControl, egressControl, egressCheckSum, deparser);
    }

    private void addIncludes(Inlcude... inlcudes) {
        for(Inlcude i: inlcudes) {
            this.includes.add(i);
        }
    }

    private void addGlobalProgramVariables(Variable... variables) {
        for(Variable v: variables) {
            if(!this.programGlobalVariables.contains(v))
                this.programGlobalVariables.add(v);
        }
    }

    private void addStructs(Struct... structs) {
        for(Struct s: structs) {
            if(!this.structs.contains(s))
                this.structs.add(s);
        }
    }

    private void addHeaders(Header... headers) {
        for(Header h: headers) {
            if(!this.headers.contains(h)) { // Only add if this header does not exist
                this.headers.add(h); 
                this.parsedHeaders.addMember(h);
            }
        }
    }

    private void addControlBlocks(Control... blocks) {
        for(Control c: blocks){
            if(!this.controlBlocks.contains(c)) {
                this.controlBlocks.add(c);
            }
        }
    }

    @Override
    public Control getControlBlockByName(String name) {
        for(Control c: controlBlocks){
            if(c.getName().equals(name)){
                return c;
            }
        }
        return null;
    }

    private void addDefines(Define... defines) {
        for(Define d: defines) {
            if(!this.defines.contains(d))
                this.defines.add(d);
        }
    }

    private Header getHeaderByName(String headerName) {
        Header returnHeader = null;
        String headerStr = null;

        if(!headerName.endsWith(Component.HEADER_EXTENSION)){
            headerStr = headerName + Component.HEADER_EXTENSION;
        } else
            headerStr = headerName;

        for(Header header: headers) { 
            if(header.getName().equals(headerStr)) {
                returnHeader = header;
                break;
            }
        }

        return returnHeader;
    }

    private Struct getStructByName(String structName) {
        Struct returnStruct = null;
        String structStr = null;

        if(!structName.endsWith(Component.STRUCT_EXTENSION)){
            structStr = structName + Component.STRUCT_EXTENSION;
        } else
            structStr = structName;

        for(Struct struct: structs) { 
            if(struct.getName().equals(structStr)) {
                returnStruct = struct;
                break;
            }
        }

        return returnStruct;
    }


    public static String compileList(ArrayList<? extends Component> listOfComponents) {
        StringBuilder compiledList = new StringBuilder();
        int indent = 0;

        for(Component c: listOfComponents) {
            if(c.isExtern() == true)
                continue;
            c.setCompileIndentation(indent);
            compiledList.append(c.compile());
            compiledList.append(Component.NEW_LINE);
        }
        
        return compiledList.toString();
    }  

    @Override
    public String toString() {
        StringBuilder bmv2Program = new StringBuilder();
        // Printing includes
        bmv2Program.append(compileList(includes));
        // Printing defines
        bmv2Program.append(compileList(defines));
        // Compiling program variables.
        bmv2Program.append(compileList(programGlobalVariables));
        // Compiling headers
        bmv2Program.append(compileList(headers));
        // Compiling structs
        bmv2Program.append(compileList(structs));
        // Compiling the parser
        int indent = 0;
        parser.setCompileIndentation(indent);
        bmv2Program.append(parser.compile());
        bmv2Program.append(Component.NEW_LINE);
        bmv2Program.append(compileList(controlBlocks));
        bmv2Program.append(Component.NEW_LINE);

        // Instantiate the pipeline
        bmv2Program.append(BMV2_PIPELINE_DECLARATION + Component.LEFT_PAR);
        bmv2Program.append(Component.NEW_LINE);
        bmv2Program.append(parser.getName() + Component.LEFT_PAR + Component.RIGHT_PAR + Component.COMMA);
        bmv2Program.append(Component.NEW_LINE);

        
        for (int i = 0; i < controlBlocks.size(); i++) {
            Control c = controlBlocks.get(i);
            bmv2Program.append(c.getName() + Component.LEFT_PAR + Component.RIGHT_PAR);

            if(i + 1 < controlBlocks.size())
                bmv2Program.append(Component.COMMA);

            bmv2Program.append(Component.NEW_LINE);
        }

        bmv2Program.append(Component.RIGHT_PAR);
        bmv2Program.append(PIPELINE_INSTANCE_DECLARATION);   

        return bmv2Program.toString();
    }

    @Override
    public String compile() {
        // We need to prepare the deparser
        Control deparser = getControlBlockByName(DEPARSER_NAME);
        ApplyBlock applyBlock = deparser.getApplyBlock();
        CodeBlock emitBlock = new PacketEmitter(parsedHeaders);
        applyBlock.addCodeBlocks(emitBlock);
        return toString();
    }

    /**
     * The following method takes a Requirement and builds the appropriate pipeline
     * components to satisfy the requirement. Note that, a Requirement may be 
     * statisfied from a previous method invocation. In this case, no further actions
     * are taken.
     * 
     * @param requirement The edge representing the requirement. A requirement is compoesed
     * of key-value pairs describing the communication between end-points. Thus, it 
     * includes protocol encapsulations and the per-protocol features that decribe the 
     * communication.
     * @throws IllegalAccessException 
     * @throws IllegalArgumentException 
     * 
    */
    @Override
    public void addPipelineRequirement(Requirement requirement) 
                throws IllegalArgumentException, IllegalAccessException {
        Protocol prev = null;
        Protocol p = requirement.getProtocol();
        Protocol next = p.getEncapsulated_proto();   // This is essentially the policy
        Header prevHeader = null;
        
        while (p != null) {
            // 1. Add header
            Header hdr = p.generateP4Header(p.getName());
            
            // log.info("Current Header is " + hdr.getName());
            
            if(prevHeader != null) {
                hdr.setParentHeader(prevHeader);
            } else {
                // This is the initial header. The parent should be pckt_in
                Header pckt_in = getHeaderByName(ControllerHeader.PACKET_IN_HDR);
                // if(pckt_in == null)
                //     throw new IllegalArgumentException("pckt in is null and hdr is " + hdr.getName());
                // log.info("Setting " + hdr.getName() + " to pckt in " + pckt_in.getName());
                hdr.setParentHeader(pckt_in);
            }

            addHeaders(hdr);
            // 2. Add needed parsing state
            parser.processProtocol(prev, p, EXTRACT_PREFIX, log);
            //3. Update headers in the ACL table
            Control ingress = getControlBlockByName(INGRESS_CONTROL_NAME);
            Set<Criterion.Type> types = null;

            if (getPipelineRole() == PipelineRole.ACL) {
                types = ingress.processACLRequirement(EXTRACT_PREFIX, p);

                // Processing of statefull requirement
                if (p.isKeepState()) {
                    Switch stateEnforcementSw = p.getStateEnforcementSwitch();

                    if (stateEnforcementSw != null && stateEnforcementSw.equals(getSw())) {
                        ingress.processStatefulACLRequirement(p, 
                            getControlBlockByName(EGRESS_CONTROL_NAME), 
                            getStructByName(PARSED_HEADERS_NAME), 
                            getHeaderByName(ControllerHeader.PACKET_OUT_HDR),
                            getStructByName(STD_META_NAME),
                            getHeaderByName(ControllerHeader.PACKET_IN_HDR)
                        );

                        // Adding recirculation support in the parser.
                        // Note: When a packet is recirculated, the std_meta stracture is zeroed.
                        State startState = parser.getStartState();
                        State packetOutState = parser.getPacketOutState();
                        startState.addNextState("0", packetOutState);
                    }
                }
            } else {
                types = ingress.processFwdRequirement(EXTRACT_PREFIX, p);
            }
            
            addSupportedCriterionTypes(types);

            prev = p;
            p = next;
            prevHeader = hdr;
            
            if(p != null)
                next = p.getEncapsulated_proto();
        }
    }
}
