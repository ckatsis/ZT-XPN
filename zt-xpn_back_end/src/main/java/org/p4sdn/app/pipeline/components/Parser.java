/**
 * ZT-XPN: An end-to-end Zero-Trust Architecture for Next Generation 
 * Programmable Networks
 * 
 * Authors:  Charalampos Katsis  (ckatsis@purdue.edu)
 *           Elisa Bertino       (bertino@purdue.edu)
 * =================================================================
 */


package org.p4sdn.app.pipeline.components;

import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.p4sdn.app.net.Protocol;

public class Parser extends Component {

    public static ComponentType type = ComponentType.PARSER;
    public static final String PARSER_DECLARATION = "parser" + SPACE;

    private ArrayList<ControlParameter> parameters = new ArrayList<>();
    private ArrayList<State> states = new ArrayList<>();
    private State startState;
    private State packetOutState;
    private State encErrorState = null;

    public Parser(String name, String startStateTransitionCondition, String pakcetOutExtractHeader) {
        super(name, type);

        // Adding the mandatory (as per P4 spec) packet_in parameter.
        ControlParameter packet_in = new ControlParameter(ParameterDirection.PACKET_IN, null, "packet", this);
        parameters.add(packet_in);
        // Start state is always the first state
        startState = State.generateStartState(this, null, startStateTransitionCondition);
        states.add(startState);
        packetOutState = State.generatePacketOutState(this, pakcetOutExtractHeader, null);
        states.add(packetOutState);
    }

    public State getStartState() {
        return startState;
    }

    public State getPacketOutState() {
        return packetOutState;
    }

    public void addParameters(ControlParameter... parameters) {
        for(ControlParameter p: parameters){
            if(!this.parameters.contains(p))
                this.parameters.add(p);
        }
    }

    public void addState(State s) {
        if(!states.contains(s)) 
            states.add(s);
    }

    public ArrayList<ControlParameter> getParameters() {
        return parameters;
    }

    public boolean doesStateExist(String name) {
        for(State s: states) {
            if(s.getName().equals(name)) {
                return true;
            }
        }
        return false;
    }

    public State getStateByName(String name) {
        for(State s: states) {
            if(s.getName().equals(name)) {
                return s;
            }
        }
        return null;
    }

    public void pruneStateByName(String name) {
        State state = getStateByName(name);
        states.remove(state);
    }

    /**
     * Generates a sink state where the packet parsing only if an
     * unsupported packet encapsulation is detected.
     */
    public void enableEncapsulationErrors() {
        encErrorState = State.generateEncapsulationErrrorState(this);
        addState(encErrorState);
    }

    public void finilizeParser() {
        if (encErrorState != null) {
            boolean errorStateConnected = false;

            for(State s: states) {
                if (s.getTransitionCondition() != null && 
                    s.getNextState(State.DEFAULT_STATE) == null
                ) {
                    errorStateConnected = true;
                    s.addNextState(State.DEFAULT_STATE, encErrorState);
                }
            }

            if (!errorStateConnected) { // no state goes to error state --> remove state
                pruneStateByName(State.ENCAPSULATION_ERROR_STATE_NAME);
            }
        }
    }

    public void updateState(Protocol currentProto, State stateToUpdate, String transitionConditionPrefix) {

        if (stateToUpdate == null) {
            return;
        }

        String condition;

        if((condition = currentProto.getTransitionConditionSuffix()) != null) {
            List<String> properties = null;

            try {
                properties = currentProto.getAssignedProperties(null);
            } catch (IllegalArgumentException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            if (properties == null)
                return;

            if (properties.contains(condition)) {
                State accept = new AcceptState();

                if (stateToUpdate.getTransitionCondition() == null) {
                    String transCond = transitionConditionPrefix += condition;
                    stateToUpdate.setTransitionCondition(transCond);
                }

                stateToUpdate.addNextState(currentProto.getTransitionPropertyValue(), accept);
            } else { // Transition condition not set (which means ANY)
                State accept = new AcceptState();
                stateToUpdate.addNextState(State.DEFAULT_STATE, accept);
            }
        }
    }

    public void processProtocol(Protocol parrentProto, Protocol currentProto, String extractPrefix, Logger l) {
        // First check if the current state exists
        String currentName = State.PARSE_STATE_PREFIX + currentProto.getName().toLowerCase();
        String extract = extractPrefix + currentProto.getExtractSuffix();
        String tansitionConditionPrefix = extract + Component.DOT;
        
        if(doesStateExist(currentName)) {
            State state = getStateByName(currentName);
            updateState(currentProto, state, tansitionConditionPrefix);
            return;
        }

        List<String> properties = new ArrayList<>();
        try {
            properties = currentProto.getAssignedProperties(null);
        } catch (IllegalArgumentException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        String condition = currentProto.getTransitionConditionSuffix();
        String tansitionCondition = new String(tansitionConditionPrefix);
        
        if(condition != null && properties.contains(condition)) {
            tansitionCondition += currentProto.getTransitionConditionSuffix();
        } else 
            tansitionCondition = null;
                
        State newState = new State(currentProto.getName(), this, 
                            extract, tansitionCondition);
        
        if(parrentProto == null) { // This is the first protocol in the stack --> ETHERNET
            startState.addNextState(State.DEFAULT_STATE, newState); // Connect the start state
            packetOutState.addNextState(State.DEFAULT_STATE, newState); // Connect the packet out state

            if (currentProto.getEncapsulated_proto() == null) { // If the is a policy for the ethernet frame
                updateState(currentProto, newState, tansitionConditionPrefix);
            }
        } else { // if it is an ecapsulated protocol (i.e., not ethernet)
            // 1. Find parent state object
            String parrentName = State.PARSE_STATE_PREFIX + parrentProto.getName().toLowerCase();
            State parrentState = getStateByName(parrentName); // this should not give null...
            // 2. Connect the parent to the child
            parrentState.addNextState(currentProto.getParentTransitionValue(), newState);
        }
        
        addState(newState);
    }

    @Override
    public String toString() {
        StringBuilder parserDeclaration = new StringBuilder();
        parserDeclaration.append(PARSER_DECLARATION);
        parserDeclaration.append(getName());
        parserDeclaration.append(LEFT_PAR);

        for(int i = 0; i < parameters.size(); i++) {
            parserDeclaration.append(parameters.get(i).compile());
            if(i + 1 < parameters.size())
                parserDeclaration.append(COMMA);
        }

        parserDeclaration.append(RIGHT_PAR);
        parserDeclaration.append(LEFT_BRACE);
        parserDeclaration.append(NEW_LINE);

        for(State s: states) {
            s.setCompileIndentation(getComponentIndentation() + 1);
            parserDeclaration.append(s.compile());
            parserDeclaration.append(NEW_LINE);
        }

        parserDeclaration.append(RIGHT_BRACE);

        return parserDeclaration.toString();
    }

    @Override
    public String compile() {
        finilizeParser();
        return toString();
    }
    
}