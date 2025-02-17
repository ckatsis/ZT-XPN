/**
 * ZT-XPN: An end-to-end Zero-Trust Architecture for Next Generation 
 * Programmable Networks
 * 
 * Authors:  Charalampos Katsis  (ckatsis@purdue.edu)
 *           Elisa Bertino       (bertino@purdue.edu)
 * =================================================================
 */


package org.p4sdn.app;

import static org.onlab.util.Tools.get;
import static org.onosproject.net.pi.model.PiPipeconf.ExtensionType.BMV2_JSON;
import static org.onosproject.net.pi.model.PiPipeconf.ExtensionType.P4_INFO_TEXT;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.onlab.packet.Ip4Address;
import org.onosproject.cfg.ComponentConfigService;
import org.onosproject.net.behaviour.Pipeliner;
import org.onosproject.net.host.HostService;
import org.onosproject.net.pi.model.DefaultPiPipeconf;
import org.onosproject.net.pi.model.PiPipeconf;
import org.onosproject.net.pi.model.PiPipeconfId;
import org.onosproject.net.pi.model.PiPipelineInterpreter;
import org.onosproject.net.pi.model.PiPipelineModel;
import org.onosproject.net.pi.service.PiPipeconfService;
import org.onosproject.p4runtime.model.P4InfoParser;
import org.onosproject.p4runtime.model.P4InfoParserException;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.p4sdn.app.exceptions.ProtocolEncapsulationException;
import org.p4sdn.app.net.EdgeSwitch;
import org.p4sdn.app.net.Switch;
import org.p4sdn.app.net.Host;
import org.p4sdn.app.net.Protocol;
import org.p4sdn.app.pipeline.Pipeline;
import org.p4sdn.app.pipeline.PipelineRole;
import org.p4sdn.app.policyEngine.PolicyEngine;
import org.p4sdn.app.topology.DataPlaneRequirementsManager;
import org.p4sdn.app.topology.TopologyMap;
import org.slf4j.Logger;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;


/**
 * Skeletal ONOS application component.
 */
@Component(immediate = true,
           service = {SomeInterface.class},
           property = {
               "someProperty=Some Default String Value",
           })
public class PolicyOrchistrator implements SomeInterface {

    private final Logger log = getLogger(getClass());

    final public static String OUTPUT_DIR = "";
    final public static String POLICY_FILE_NAME = "network_req.json";

    // TODO: The following two needs to be removed... we can automatically get them from the compiled switch binary
    // private static final URL P4INFO_URL = PolicyOrchistrator.class.getResource("/genp4_p4info.txt");
    // private static final URL BMV2_JSON_URL = PolicyOrchistrator.class.getResource("/genp4.json");
    // public static final PiPipeconfId PIPECONF_ID = new PiPipeconfId("p4-tutorial-pipeconf");

    /** Some configurable property. */
    private String someProperty;
    private TopologyMap topologyMap = null;

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected ComponentConfigService cfgService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected HostService hostService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    private PiPipeconfService piPipeconfService;

    private static HashMap<String, Pipeline> generatedPipelinesMap;
    public static HashMap<Integer, Protocol> statefulFidToPolicyMap = new HashMap<Integer, Protocol>();

    int totalReqs = 0;
    long totalPEinsertionTime = 0;

    @Activate
    protected void activate() {
        log.info("Starting...");

        long orchestrationStartTime = System.nanoTime();
        cfgService.registerProperties(getClass());
        topologyMap = new TopologyMap(true); 

        // Populate dataplane manager with the network's switches
        DataPlaneRequirementsManager.init(topologyMap.getSwitches());
        
        log.info("Loding IR file");
        long processIRStartTime = System.nanoTime();
        JsonElement jsonGraph = loadPolicy(OUTPUT_DIR, POLICY_FILE_NAME);

        log.info("Processing IR");
        processPolicy(jsonGraph);
        long processIREndTime = System.nanoTime();
        long processIRExecutionTime = (processIREndTime - processIRStartTime) / 1000000; // Convert to milliseconds

        log.info("Building Requirements");
        long buildReqsStartTime = System.nanoTime();
        DataPlaneRequirementsManager.addLogger(log);
        generatedPipelinesMap = DataPlaneRequirementsManager.buildPipelines();
        long buildReqsEndTime = System.nanoTime();
        long buildReqsExecutionTime = (buildReqsEndTime - buildReqsStartTime) / 1000000; // Convert to milliseconds

        log.info("Pipeline(s) have been generated");

        // log.info("Initializing interpreter with pipeline " + generatedPipelinesMap.get("device:bmv2:s1").getName());
        // PipelineIntepreterHandler.init(generatedPipelinesMap.get("device:bmv2:s1"), log);
        long pipelineInitStartTime = System.nanoTime();

        // Start pipeline registration process
        for (Pipeline pln: generatedPipelinesMap.values()) {

            if (pln.getPipelineRole() == PipelineRole.ACL) {
                log.info("Initializing ACL pipeline " + pln.getName());
                PipelineIntepreterHandler.initACL(pln, log);
            } else {
                log.info("Initializing FWD pipeline " + pln.getName());
                PipelineIntepreterHandler.initFwd(pln, log);
            }

            // matchFields, acTable.getFullyQualifiedName(), isNoAction, 
            //         acTable.getActionByName(Control.SET_OUT_PORT_ACTION_NAME),
            //         acTable.getActionByName(Control.SEND_TO_CONTROLLER_ACTION_NAME));
            
            log.info("Registering pipeline: " + pln.getName());

            try {
                piPipeconfService.register(buildPipeconf(pln.getName()));
                log.info("Registration completed");
            } catch (Exception e) {
                log.error("Fail to register {} - Exception: {} - Cause: {}",
                    pln.getName(), e.getMessage(), e.getCause().getMessage());
            }
        }
        long pipelineInitEndTime = System.nanoTime();
        long orchestrationEndTime = System.nanoTime();
        long pipelineInitExecutionTime = (pipelineInitEndTime - pipelineInitStartTime) / 1000000; // Convert to milliseconds
        long orchestrationExecutionTime = (orchestrationEndTime - orchestrationStartTime) / 1000000; // Convert to milliseconds

        // Print the execution time
        log.info("IR load and processing execution time: " + processIRExecutionTime + " ms");
        log.info("Building Reqs/programs execution time: " + buildReqsExecutionTime + " ms");
        log.info("Pipeline init execution time: " + pipelineInitExecutionTime + " ms");
        log.info("Policy Orchestration Execution time: " + orchestrationExecutionTime + " ms");
        log.info("Total Policy Insertion time: " + totalPEinsertionTime + " ms");
        double avgPEinsertionTime = (double) totalPEinsertionTime / totalReqs;
        log.info("Total processed requirements: " + totalReqs);
        log.info("Avg PE insertion time: " + avgPEinsertionTime + " ms");
    }

    public static Pipeline getSwitchPipeline(String swId) {
        return generatedPipelinesMap.get(swId);
    }

    private PiPipeconf buildPipeconf(String pipelineName) throws P4InfoParserException {
        
        URL p4Info_URL = PolicyOrchistrator.class.getResource("/" + pipelineName + ".txt");
        URL bmv2_json_URL = PolicyOrchistrator.class.getResource("/" + pipelineName + ".json");
        // PiPipelineModel pipelineModel = P4InfoParser.parse(P4INFO_URL);
        PiPipelineModel pipelineModel = P4InfoParser.parse(p4Info_URL);
        PiPipeconfId pipeconfId = new PiPipeconfId(pipelineName);

        // return DefaultPiPipeconf.builder()
        //         .withId(pipeconfId)
        //         .withPipelineModel(pipelineModel)
        //         .addBehaviour(PiPipelineInterpreter.class, PipelineIntepreterHandler.class)
        //         // .addBehaviour(PortStatisticsDiscovery.class, PortStatisticsDiscoveryImpl.class)
        //         // Since mytunnel.p4 defines only 1 table, we re-use the existing single-table pipeliner.
        //         .addBehaviour(Pipeliner.class, PipelinerHandler.class)
        //         .addExtension(P4_INFO_TEXT, P4INFO_URL)
        //         .addExtension(BMV2_JSON, BMV2_JSON_URL)
        //         .build();
        return DefaultPiPipeconf.builder()
                .withId(pipeconfId)
                .withPipelineModel(pipelineModel)
                .addBehaviour(PiPipelineInterpreter.class, PipelineIntepreterHandler.class)
                // .addBehaviour(PortStatisticsDiscovery.class, PortStatisticsDiscoveryImpl.class)
                // Since mytunnel.p4 defines only 1 table, we re-use the existing single-table pipeliner.
                .addBehaviour(Pipeliner.class, PipelinerHandler.class)
                .addExtension(P4_INFO_TEXT, p4Info_URL)
                .addExtension(BMV2_JSON, bmv2_json_URL)
                .build();
    }


    @Deactivate
    protected void deactivate() {
        cfgService.unregisterProperties(getClass(), false);
        log.info("Stopped");
    }

    @Modified
    public void modified(ComponentContext context) {
        Dictionary<?, ?> properties = context != null ? context.getProperties() : new Properties();
        if (context != null) {
            someProperty = get(properties, "someProperty");
        }
        log.info("Reconfigured");
    }

    @Override
    public void someMethod() {
        log.info("Invoked");
    }

    public JsonElement loadPolicy(String targetDir, String file_name) {

        Path path = Paths.get(
				System.getProperty("user.dir"), 
				targetDir, 
				file_name);

        JsonElement jsonElement = null;
        try (FileReader fileReader = new FileReader(path.toString())) {
            // JsonParser parser = new JsonParser();
            // JsonObject jsonObject = parser.parse(fileReader).getAsJsonObject();

            jsonElement = JsonParser.parseReader(fileReader);

            // Gson gson = new Gson();
            // String jsonOutput = gson.toJson(jsonElement);
            // log.info("JSON read from file and converted to Gson format:");
            // log.info(jsonOutput);
        } catch (IOException e) {
            log.error("Exception caught", e);
            // e.printStackTrace();
        }

        return jsonElement;
    }

    public void processNetworkPolicy(JsonElement requirementJsonElement) {
        // for(Switch sw: topologyMap.getSwitches()) {

        Set<Map.Entry<String, JsonElement>> req_set = requirementJsonElement.getAsJsonObject().entrySet();
        
        for (Map.Entry<String, JsonElement> e : req_set) {
            JsonElement protocol = e.getValue();
            JsonArray protocol_arr = protocol.getAsJsonArray();

            for(JsonElement protoJson: protocol_arr.asList()) {
                JsonObject protoObj = protoJson.getAsJsonObject();
                // protocol name
                String proto = protoObj.get("protocol").getAsString();
                // protocol type
                String type = protoObj.get("etherType").getAsString();
                int fid = protoObj.get("fid").getAsInt();
                boolean keepState = protoObj.get("isStateful").getAsBoolean();
                int dependency_fid = protoObj.get("dependecyFid").getAsInt();
                
                // Creating the protocol object
                try {
                    Protocol proto_requirment = Protocol.generateFromArguments(proto, type, log);
                    proto_requirment.setFid(fid);
                    proto_requirment.setKeepState(keepState);

                    if (keepState) {
                        statefulFidToPolicyMap.put(Integer.valueOf(fid), proto_requirment);
                    }

                    if (dependency_fid != -1) {
                        Protocol p = statefulFidToPolicyMap.get(Integer.valueOf(dependency_fid));

                        if (p != null) {
                            proto_requirment.setDependencyProtocol(p);
                        } else {
                            log.error(String.format("Processing fid %d: Dependency %d not recorded -- Skipping",
                                fid, dependency_fid));
                            continue;
                        }
                    }

                    totalReqs++;
                    PolicyEngine.setLogger(log);
                    long policyInsertionStartTime = System.nanoTime();
                    PolicyEngine.addPolicy(proto_requirment);
                    long policyInsertionEndTime = System.nanoTime();
                    long policyInsertionExecutionTime = (policyInsertionEndTime - policyInsertionStartTime) / 1000000; // Convert to milliseconds
                    totalPEinsertionTime += policyInsertionExecutionTime;
                    Requirement requirement = new Requirement("ANY", "ANY", proto_requirment);
                    // Finally, adding the requirement for the edge switch
                    DataPlaneRequirementsManager.addAllSwitchRequirement(requirement);
                    log.info("Added network policy...");
                } catch (ProtocolEncapsulationException e1) {
                    log.error("Exception", e1);
                }
            }
        }
        // }
    }

    public void processPolicy(JsonElement policyJsonElement) {

        final String UNSPECIFIED = "ANY";

        if (policyJsonElement.isJsonObject()) {
            // Get the JSON object
            JsonObject policyJsonObj = policyJsonElement.getAsJsonObject();

            // Get the entry set of the JSON object
            Set<Map.Entry<String, JsonElement>> entrySet = policyJsonObj.entrySet();
            
            // Iterate through each entry in the JSON object
            for (Map.Entry<String, JsonElement> entry : entrySet) {
                // Get the key and value of the entry
                String source_entity = entry.getKey();
                JsonElement requirementJsonElement = entry.getValue();
                log.info("Source entity " + source_entity);
                
                if (source_entity.equals(UNSPECIFIED)) {
                    processNetworkPolicy(requirementJsonElement);
                    continue;
                }
                
                Host source_host = getHostByEntityName(source_entity);

                if(source_host == null) {
                    log.error("No host found for entity " + source_entity);
                    continue;
                } else
                    log.info("Found source host!");
                
                // Obtain the edge switch
                // HostLocation edge_switch = source_host.location();
                // EdgeSwitch edgeSw = source_host.getEdgeSwitchConnection();
                // log.info("Edge switch ID " + edgeSw.getName());
                

                if(requirementJsonElement.isJsonObject()) {
                    Set<Map.Entry<String, JsonElement>> req_set = requirementJsonElement.getAsJsonObject().entrySet();
                
                    for (Map.Entry<String, JsonElement> e : req_set) {
                        String dst_entity = e.getKey();
                        JsonElement protocol = e.getValue();
                        Host dst_host = getHostByEntityName(dst_entity);
                        JsonArray protocol_arr = protocol.getAsJsonArray();

                        if(dst_host == null) {
                            log.error("No host found for entity " + dst_entity);
                            continue;
                        } else
                            log.info("Found dst host!");
                        
                        for(JsonElement protoJson: protocol_arr.asList()) {
                            // log.info("0");
                            JsonObject protoObj = protoJson.getAsJsonObject();
                            // protocol name
                            // log.info("1");
                            String proto = protoObj.get("protocol").getAsString();
                            // protocol ports
                            // log.info("2");
                            JsonArray portsJsonArray = protoObj.get("ports").getAsJsonArray();
                            // log.info("3");
                            int[] ports = new int[portsJsonArray.size()];
                            int fid = protoObj.get("fid").getAsInt();
                            boolean keepState = protoObj.get("isStateful").getAsBoolean();
                            boolean isFwdDir = protoObj.get("isFwdDir").getAsBoolean();
                            int dependency_fid = protoObj.get("dependecyFid").getAsInt();
                            EdgeSwitch edgeSw; // = source_host.getEdgeSwitchConnection();
                            // log.info("Edge switch ID " + edgeSw.getName());
                            // log.info("4");
                            // log.info("fid = " + fid);

                            if (isFwdDir){
                                // log.info("5");
                                for (int i = 0; i < portsJsonArray.size(); i++) {
                                    JsonElement element = portsJsonArray.get(i);
                                    ports[i] = element.getAsInt();
                                } 
                                // log.info("6");
                                edgeSw = source_host.getEdgeSwitchConnection();
                            } else { // Reverse the ports
                                // log.info("7");
                                ports[0] = portsJsonArray.get(1).getAsInt();
                                ports[1] = portsJsonArray.get(0).getAsInt();
                                // log.info("8");
                                edgeSw = dst_host.getEdgeSwitchConnection();
                            }

                            // log.info("9");

                            // Creating the protocol object
                            try {
                                // Protocol proto_requirment = Protocol.generateFromArguments(proto, getIpByEntityName(source_entity), 
                                                // getIpByEntityName(dst_entity), ports);
                                Protocol proto_requirment;

                                if (isFwdDir) {
                                    // log.info("9.1");
                                    proto_requirment = Protocol.generateFromArguments(proto, getIpByEntityName(source_entity), 
                                                getIpByEntityName(dst_entity), ports);
                                    // log.info("9.2");
                                } else {
                                    // log.info("9.3");
                                    proto_requirment = Protocol.generateFromArguments(proto, getIpByEntityName(dst_entity), 
                                            getIpByEntityName(source_entity), ports);
                                    // log.info("9.4");
                                }
                                // log.info("10");
                                proto_requirment.setFid(fid);
                                proto_requirment.setKeepState(keepState);

                                if (keepState) {
                                    log.info(String.format("Flow %d is stateful", fid));
                                    statefulFidToPolicyMap.put(Integer.valueOf(fid), proto_requirment);
                                    proto_requirment.setStateEnforcementSwitch(edgeSw); // temporarily keep the state at 
                                                                                        // the edge switch connected
                                }
                                // log.info("11");

                                Protocol p = null;

                                if (dependency_fid != -1) {
                                    log.info(String.format("Flow %d is dependent on flow %d", fid, dependency_fid));
                                    p = statefulFidToPolicyMap.get(Integer.valueOf(dependency_fid));

                                    if (p != null) {
                                        proto_requirment.setDependencyProtocol(p);
                                        proto_requirment.setStateEnforcementSwitch(p.getStateEnforcementSwitch());

                                        // if (keepState) {
                                        //     // If the state is dependent on another keep this state at
                                        //     // the same place with the parent
                                        //     // TODO: This choice can be more sophisticated.
                                        //     proto_requirment.setStateEnforcementSwitch(p.getStateEnforcementSwitch());
                                        // }
                                    } else {
                                        log.error(String.format("Processing fid %d: Dependency %d not recorded -- Skipping",
                                            fid, dependency_fid));
                                        continue;
                                    }
                                }
                                // log.info("12");
                                PolicyEngine.setLogger(log);
                                long policyInsertionStartTime = System.nanoTime();
                                PolicyEngine.addPolicy(proto_requirment);
                                long policyInsertionEndTime = System.nanoTime();
                                long policyInsertionExecutionTime = (policyInsertionEndTime - policyInsertionStartTime) / 1000000; // Convert to milliseconds
                                totalPEinsertionTime += policyInsertionExecutionTime;
                                // Requirement requirement = new Requirement(source_entity, dst_entity, proto_requirment);
                                Requirement requirement;
                                
                                if (isFwdDir) {
                                    requirement = new Requirement(source_entity, dst_entity, proto_requirment);
                                } else {
                                    requirement = new Requirement(dst_entity, source_entity, proto_requirment);
                                }

                                totalReqs++;
                                // Finally, adding the requirement for the edge switch
                                DataPlaneRequirementsManager.addSwitchRequirement(edgeSw, requirement);
                                
                                if ((keepState || dependency_fid != -1) && p != null) {
                                    DataPlaneRequirementsManager.addSwitchRequirement(p.getStateEnforcementSwitch(), requirement);
                                }

                                log.info("Thankfully all went well...");
                            } catch (ProtocolEncapsulationException e1) {
                                log.error("Exception", e1);
                            }
                        }
                    }
                }           
            }
        } else {
            log.error("The provided JSON is not an object.");
        }
    }

    public int getIpByEntityName(String entity) {
        // log.info("entity = " + entity);
        // if (topologyMap.getIpAddress(entity) == null) {
        //     log.error("NULL IP!!!");
        // } else {
        //     log.info("entity = " + entity + " ip = " + topologyMap.getIpAddress(entity));
        // }
        Ip4Address ip = Ip4Address.valueOf(topologyMap.getIpAddress(entity)); // Assume IPv4
        // log.info("entity = " + entity + " ip = " + ip.toString());
        return ip.toInt();
    }

    public Host getHostByEntityName(String entity) {
        return topologyMap.getHostByName(entity);
    }
}
