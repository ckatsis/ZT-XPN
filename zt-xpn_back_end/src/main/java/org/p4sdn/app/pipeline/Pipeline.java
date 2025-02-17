/**
 * ZT-XPN: An end-to-end Zero-Trust Architecture for Next Generation 
 * Programmable Networks
 * 
 * Authors:  Charalampos Katsis  (ckatsis@purdue.edu)
 *           Elisa Bertino       (bertino@purdue.edu)
 * =================================================================
 */


package org.p4sdn.app.pipeline;

import java.util.HashSet;
import java.util.Set;

import org.onosproject.net.flow.criteria.Criterion;
import org.p4sdn.app.Requirement;
import org.p4sdn.app.net.Switch;
import org.p4sdn.app.pipeline.components.Control;


public abstract class Pipeline {

    public static final String CPU_PORT_DECLARATION = "CPU_PORT";
    public static final String PARSER_NAME = "ParserImpl";
    public static final String DEPARSER_NAME = "DeparserImpl";

    private String name;
    private PipelineRole pipelineRole;
    private boolean roportParsingErrorsToControlPlane;
    private Set<Criterion.Type> supportedCriteriaTypes = new HashSet<>();
    private Switch sw;

    // ArrayList<Component> componentList = new ArrayList<>();

    public Pipeline(String name, PipelineRole pipelineRole, boolean reportParsingErrors, Switch sw) {
        this.name = name;
        this.pipelineRole = pipelineRole;
        this.roportParsingErrorsToControlPlane = reportParsingErrors;
        this.sw = sw;
    }

    // public void addComponent(Component component) {
    //     // Only add the component if it does not exist
    //     if(!componentList.contains(component))
    //         componentList.add(component);
    // }

    public abstract void addPipelineRequirement(Requirement requirement) throws IllegalArgumentException, IllegalAccessException;
    
    public String getName() {
        return name;
    }

    public boolean isRoportParsingErrorsToControlPlane() {
        return roportParsingErrorsToControlPlane;
    }

    public PipelineRole getPipelineRole() {
        return pipelineRole;
    }

    public Switch getSw() {
        return sw;
    }

    public void setSw(Switch sw) {
        this.sw = sw;
    }

    public abstract String compile();

    public abstract Control getControlBlockByName(String name);

    public static String removePrefix(String str, String prefix) {
        if (str != null && prefix != null && str.startsWith(prefix)) {
            return str.substring(prefix.length());
        }
        return str;
    }

    public void addSupportedCriterionTypes(Set<Criterion.Type> types) {
        supportedCriteriaTypes.addAll(types);
    }

    public boolean isSupportedCriterion(Criterion criterion) {
        if (supportedCriteriaTypes.contains(criterion.type())) {
            return true;
        } else
            return false;
    }

}
