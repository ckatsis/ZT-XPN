/**
 * ZT-XPN: An end-to-end Zero-Trust Architecture for Next Generation 
 * Programmable Networks
 * 
 * Authors:  Charalampos Katsis  (ckatsis@purdue.edu)
 *           Elisa Bertino       (bertino@purdue.edu)
 * =================================================================
 */


package org.p4sdn.app.pipeline.components;

import java.util.HashMap;
import java.util.Map;

import org.p4sdn.app.pipeline.BMV2Pipeline;

public class State extends Component {

    public static final String START_STATE_NAME = "start";
    public static final String PARSE_STATE_PREFIX = "parse_";
    public static final String ENCAPSULATION_ERROR_STATE_NAME = "error_unsupported_encapsulation";
    public static final String PACKET_OUT_STATE_NAME = PARSE_STATE_PREFIX + "packet_out";
    public static final String STATE_DECLARATION = "state" + SPACE;
    public static final String EXTRACT_METHOD = "extract";
    public static final String VERIFY_METHOD = "verify";
    public static final String TRANSITION = "transition" + SPACE;
    public static final String SELECT = "select" + SPACE;
    public static final String DEFAULT_STATE = "default"; // If this state has only one next state
    public static final ComponentType type = ComponentType.PARSER_STATE;

    private String extracted_header;
    private String transition_condition;
    private InLineStatement verificationStatement;
    private HashMap<String, State> next_states = new HashMap<>();
    private Parser associatedParser;


    /**
     * Only used by RejectState or AcceptState childs of this class
     * @param name
     */
    protected State(String name) {
        super(name, type);
    }

    /**
     * Constructor for State object. Do not use for Start state or accept and reject states!! Use generateStartState() instead.
     * @param name
     * @param associatedParser
     * @param extracted_header
     * @param transition_condition
     */
    public State(String name, Parser associatedParser, String extracted_header, 
                    String transition_condition) {
        super((PARSE_STATE_PREFIX + name).toLowerCase(), type);
        this.associatedParser = associatedParser;
        this.extracted_header = extracted_header;
        this.transition_condition = transition_condition; 
    }

    public static State generateStartState(Parser associatedParser, String extracted_header, 
                                            String transition_condition) {
        State start = new State(START_STATE_NAME);
        start.setAssociatedParser(associatedParser);
        start.setExtractedHeader(extracted_header);
        start.setTransitionCondition(transition_condition);
        return start;
    }

    public static State generatePacketOutState(Parser associatedParser, String extracted_header, 
                                            String transition_condition) {
        State packet_out = new State(PACKET_OUT_STATE_NAME);
        packet_out.setAssociatedParser(associatedParser);
        packet_out.setExtractedHeader(extracted_header);
        packet_out.setTransitionCondition(transition_condition);
        return packet_out;
    }

    public static State generateEncapsulationErrrorState(Parser associatedParser) {
        State encErrorState = new State(ENCAPSULATION_ERROR_STATE_NAME);
        encErrorState.setAssociatedParser(associatedParser);
        String verification = VERIFY_METHOD + LEFT_PAR + FALSE + COMMA + 
                    BMV2Pipeline.P4CORE_ERROR_NOMATCH + RIGHT_PAR;
        InLineStatement statement = new InLineStatement(null, encErrorState, verification);
        encErrorState.setVerificationStatement(statement);
        
        return encErrorState;
    }

    public void setVerificationStatement(InLineStatement verificationStatement) {
        this.verificationStatement = verificationStatement;
    }

    public void setAssociatedParser(Parser associatedParser) {
        this.associatedParser = associatedParser;
    }

    public void setExtractedHeader(String extracted_header) {
        this.extracted_header = extracted_header;
    }

    public void setTransitionCondition(String transition_condition) {
        this.transition_condition = transition_condition;
    }

    public String getTransitionCondition() {
        return transition_condition;
    }

    public State getNextState(String key) {
        return next_states.get(key);
    }

    public void addNextState(String value, State nexState) {
        // TODO: There should be a check for whether the provided value exists or not. If it does
        // check to see whether the next state is the same; if not throw an error.
        if (value != null && (next_states.get(value) != null)) {
            if (nexState instanceof AcceptState) { // Do not overwrite the a specific map state to an accept state.
                return;
            }
        }
        
        if (transition_condition == null) // There is no select statement
            next_states.put(DEFAULT_STATE, nexState); // Ignore the provided value
        else
            next_states.put(value, nexState);
    }

    public String getExtract() {
        if (extracted_header == null) 
            return null;

        String packet_in = associatedParser.getParameters().get(0).getName(); // Index 0 is always the packet_in parameter as per P4 standard.
        return packet_in + DOT + EXTRACT_METHOD + LEFT_PAR + extracted_header + RIGHT_PAR + STATEMENT_TERMINATOR;
    }

    public String getSelect() {
        StringBuilder selectDeclaration = new StringBuilder();
        selectDeclaration.append(SELECT);
        selectDeclaration.append(LEFT_PAR + transition_condition + RIGHT_PAR + LEFT_BRACE + NEW_LINE);
        State defaultState = null;
        int selectBodyIndent = getComponentIndentation() + 2;
        int selectIndent = selectBodyIndent - 1;

        for(Map.Entry<String, State> entry : next_states.entrySet()){
            if (entry.getKey().equals(DEFAULT_STATE)) {
                defaultState = entry.getValue();
                continue;
            }
            selectDeclaration.append(generateIndentation(selectBodyIndent));
            selectDeclaration.append(entry.getKey());
            selectDeclaration.append(COLON);
            selectDeclaration.append(entry.getValue().getName());
            selectDeclaration.append(STATEMENT_TERMINATOR);
            selectDeclaration.append(NEW_LINE);
        }
        
        // It could be that there is no default state
        if (defaultState != null){
            selectDeclaration.append(generateIndentation(selectBodyIndent));
            selectDeclaration.append(DEFAULT_STATE);
            selectDeclaration.append(COLON);
            selectDeclaration.append(defaultState.getName());
            selectDeclaration.append(STATEMENT_TERMINATOR);
            selectDeclaration.append(NEW_LINE);  
        }

        selectDeclaration.append(generateIndentation(selectIndent));
        selectDeclaration.append(RIGHT_BRACE);

        return selectDeclaration.toString();
    }

    private void finalizeState() {
        if(transition_condition == null && next_states.get(DEFAULT_STATE) == null) {
            State accept = new AcceptState();
            addNextState(null, accept);
        }
    } 

    @Override
    public String toString() {
        StringBuilder stateDeclaration = new StringBuilder();
        stateDeclaration.append(generateIndentation());
        stateDeclaration.append(STATE_DECLARATION);
        stateDeclaration.append(getName());
        stateDeclaration.append(LEFT_BRACE);
        stateDeclaration.append(NEW_LINE);
        int bodyIndent = getComponentIndentation() + 1;
        stateDeclaration.append(generateIndentation(bodyIndent));
        
        if (verificationStatement != null) {
            stateDeclaration.append(verificationStatement.compile());
            stateDeclaration.append(NEW_LINE);
            stateDeclaration.append(generateIndentation(bodyIndent));
        }
        
        String extract_statement = getExtract();

        if (extract_statement != null) {
            stateDeclaration.append(extract_statement);
            stateDeclaration.append(NEW_LINE);
            stateDeclaration.append(generateIndentation(bodyIndent));
        }

        stateDeclaration.append(TRANSITION);
        
        if (transition_condition == null) {
            State next = next_states.get(DEFAULT_STATE);
            stateDeclaration.append(next.getName());
            stateDeclaration.append(STATEMENT_TERMINATOR);
        } else {
            stateDeclaration.append(getSelect());
        }
        
        stateDeclaration.append(NEW_LINE);
        stateDeclaration.append(generateIndentation());
        stateDeclaration.append(RIGHT_BRACE);

        return stateDeclaration.toString();
    }
    
    @Override
    public String compile() {
        finalizeState();
        return toString();
    }
}
