/**
 * ZT-XPN: An end-to-end Zero-Trust Architecture for Next Generation 
 * Programmable Networks
 * 
 * Authors:  Charalampos Katsis  (ckatsis@purdue.edu)
 *           Elisa Bertino       (bertino@purdue.edu)
 * =================================================================
 */


package org.p4sdn.app.topology;

import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.p4sdn.app.Requirement;
import org.p4sdn.app.net.EdgeSwitch;
import org.p4sdn.app.net.EthernetProtocol;
import org.p4sdn.app.net.Protocol;
import org.p4sdn.app.net.Switch;
import org.p4sdn.app.pipeline.BMV2Pipeline;
import org.p4sdn.app.pipeline.Pipeline;
import org.p4sdn.app.pipeline.PipelineRole;
import org.slf4j.Logger;

public final class DataPlaneRequirementsManager {

    private static Logger log;

    // This is where we store all the policies
    private static HashMap<Switch, HashSet<Requirement>> switchRequirements = new HashMap<>();

    private DataPlaneRequirementsManager() {}

    public static void addLogger(Logger l) {
        log = l;
    }

    public static void init(HashSet<Switch> networkSwitches) {

        for (Switch sw: networkSwitches) {
            switchRequirements.put(sw, new HashSet<>());
        }

    }

    public static void addAllSwitchRequirement(Requirement requirement) {
        for(Switch sw: switchRequirements.keySet()) {

            HashSet<Requirement> reqs = null;
            Protocol p = requirement.getProtocol();
                    
            if (p.isKeepState()) {
                p.setStateEnforcementSwitch(sw);
            }

            if ((reqs = switchRequirements.get(sw)) != null) {
                if(!reqs.contains(requirement)) {
                    reqs.add(requirement);
                }
            } else {
                reqs = new HashSet<>();
                reqs.add(requirement);
                switchRequirements.put(sw, reqs);
            }
        }
    }

    public static void addSwitchRequirement(Switch sw, Requirement requirement) {
        if (switchRequirements.containsKey(sw)) {
            HashSet<Requirement> reqs = switchRequirements.get(sw);

            if(!reqs.contains(requirement)) {
                reqs.add(requirement);
            }
        } else {
            HashSet<Requirement> reqs = new HashSet<>();
            reqs.add(requirement);
            switchRequirements.put(sw, reqs);
        }
    }

    public static void addL2SwitchRequirement(Switch sw) {
        EthernetProtocol protocol = new EthernetProtocol();
        protocol.setDestinationAddress("ANY");
        Requirement l2Requirement = new Requirement("ANY", "ANY", protocol);
        addSwitchRequirement(sw, l2Requirement);
    }

    public static HashMap<String, Pipeline> buildPipelines() {

        log.info("Building pipelines...");

        HashMap<String, Pipeline> swToPipeLineMap = new HashMap<>();
        
         for (Map.Entry<Switch, HashSet<Requirement>> entry : switchRequirements.entrySet()) {
            Switch sw = entry.getKey();
            HashSet<Requirement> reqs = entry.getValue();
            
            // The pipeline name is sw_<id> where <id> is the identifier of the sw.
            StringBuilder pipeName = new StringBuilder();
            // pipeName.append("sw_");
            // pipeName.append(sw.getSwId());
            pipeName.append(sw.getName());

            // log.info("Building pipeline " + pipeName.toString() + " for switch " + sw.getSwId());
            log.info("Building pipeline " + pipeName.toString() + " for switch " + sw.getName());

            PipelineRole role = null;

            if (sw instanceof EdgeSwitch) {
                role = PipelineRole.ACL;              
            } else {
                role = PipelineRole.FWD;
                addL2SwitchRequirement(sw);
                reqs = switchRequirements.get(sw);
            }

            // Create a new BMV2 pipeline object.
            // We are assuming all switches are BMV2 Switches
            Pipeline pipeline = new BMV2Pipeline(pipeName.toString(), 
                        role, false, sw); // All edge switches have an ACL and Fwd role
            BMV2Pipeline.addLogger(log);

            try{
                buildRequirements(pipeline, reqs);
            } catch(Exception e) {
                log.error("Exception caught", e);
            } 
            
            log.info("Build completed");

            String filePath = pipeName.toString() + ".p4";

            // Compile the pipeline 
            String program = "";
            try {
                program  = pipeline.compile();
            } catch(Exception e) {
                log.error("Exception caught", e);
            }
            
            FileWriter writer;
            try {
                writer = new FileWriter(filePath);
                writer.write(program);
                writer.close();
                log.info("Pipeline written to file " + filePath);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                log.error("Exception caught", e);
            }            

            // Add this pipeline object to the pipeline manager. 
            // TODO: The manager should check if another switch has the same pipeline implementation.
            // If that is the case, the corresponding switches should be managed by the same pipeline.
            swToPipeLineMap.put(pipeline.getName(), pipeline);
         }

         return swToPipeLineMap;
    }

    public static void buildRequirements(Pipeline pipeline, HashSet<Requirement> reqs) 
            throws IllegalArgumentException, IllegalAccessException, Exception {
        
        for(Requirement r: reqs){
            pipeline.addPipelineRequirement(r);
        }
    }
}
