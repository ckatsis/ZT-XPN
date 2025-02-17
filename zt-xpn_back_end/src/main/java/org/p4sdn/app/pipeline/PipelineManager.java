/**
 * ZT-XPN: An end-to-end Zero-Trust Architecture for Next Generation 
 * Programmable Networks
 * 
 * Authors:  Charalampos Katsis  (ckatsis@purdue.edu)
 *           Elisa Bertino       (bertino@purdue.edu)
 * =================================================================
 */


package org.p4sdn.app.pipeline;

import java.util.HashMap;

import org.p4sdn.app.net.EdgeSwitch;
import org.slf4j.Logger;

public class PipelineManager {

    private static Logger log;
    private static HashMap<EdgeSwitch, Pipeline> switchPipelines = new HashMap<>();

    private PipelineManager(){}

    public static void registerLogger(Logger l){
        log = l;
    }

    public static void addSwitchPipeline(EdgeSwitch sw, Pipeline pipeline) {
        if(switchPipelines.containsKey(sw)) {
            log.error("The pipeline for the sw " + sw + " already generated");
            return;
        }

        // TODO: Add logic to merge pipeline
    }
    
}
