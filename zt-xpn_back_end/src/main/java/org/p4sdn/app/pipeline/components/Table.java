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
import java.util.stream.Collectors;

import org.p4sdn.app.pipeline.components.FlowCounter.CounterType;

public class Table extends Component {

    public static final ComponentType type = ComponentType.TABLE;
    public static final String KEY_SECTION = "key";
    public static final String ACTIONS_SECTION = "actions";
    public static final String TABLE_DECLARATION = "table" + SPACE;
    public static final String DEFUALT_ACTION_DECLARATION = "default_action";
    public static final String COUNTERS_DECLARATION = "counters";

    private ArrayList<KeyEntry> keyEntries;
    private ArrayList<Action> actions;
    private Action defaultAction;
    private FlowCounter flowCounter;
    private Component associatedComponent;


    public Table(String name, Component associatedComponent, boolean defaultFlowCounter) {
        super(name, type);
        this.associatedComponent = associatedComponent;
        keyEntries = new ArrayList<>();
        actions = new ArrayList<>();

        if (defaultFlowCounter) {
            this.flowCounter = new FlowCounter(this, 
                CounterType.PACKETS_AND_BYTES, associatedComponent);
        }
    }

    public void addKeyEntry(KeyEntry entry){
        if(!keyEntries.contains(entry))
            keyEntries.add(entry);
    }

    public void addActions(Action... actions) {
        for(Action a: actions){
            if(!this.actions.contains(a)) {
                this.actions.add(a);
            }
        }
    }

    public void setFlowCounter(FlowCounter flowCounter) {
        this.flowCounter = flowCounter;
        this.flowCounter.setAssociatedTable(this);
    }

    public ArrayList<KeyEntry> getKeyEntries() {
        return keyEntries;
    }

    public List<String> getKeyEntriesAsStringList() {
        List<String> strKeys = keyEntries.stream().map(KeyEntry::getKey).collect(Collectors.toList());
        return strKeys;
    }

    public Action getActionByName(String actionName) {
        for(Action action: this.actions) {
            if(action.getName().equals(actionName))
                return action;
        }
        
        return null;
    }

    public void setKeyEntries(ArrayList<KeyEntry> keyEntries) {
        this.keyEntries = keyEntries;
    }

    public void setActions(ArrayList<Action> actions) {
        this.actions = actions;
    }

    public void setDefaultAction(Action defaultAction) {
        this.defaultAction = defaultAction;
    }

    @Override
    public String toString() {
        StringBuilder tableDeclaration = new StringBuilder();
        tableDeclaration.append(generateIndentation());

        // First generate the declaration of the flow counter (if any)
        if (flowCounter != null) {
            tableDeclaration.append(flowCounter.compile());
            tableDeclaration.append(NEW_LINE);
        }

        tableDeclaration.append(generateIndentation());
        tableDeclaration.append(TABLE_DECLARATION);
        tableDeclaration.append(getName());
        tableDeclaration.append(LEFT_BRACE);
        tableDeclaration.append(NEW_LINE);
        int tableBodyIndent = getComponentIndentation() + 1;
        tableDeclaration.append(generateIndentation(tableBodyIndent));
        tableDeclaration.append(KEY_SECTION + EQUALS + LEFT_BRACE);
        tableDeclaration.append(NEW_LINE);
        int keyBodyIndent = tableBodyIndent + 1;

        for(KeyEntry e: keyEntries){
            tableDeclaration.append(generateIndentation(keyBodyIndent));
            tableDeclaration.append(e.getKey());
            tableDeclaration.append(COLON);
            tableDeclaration.append(e.getMatchKind().name().toLowerCase());
            tableDeclaration.append(STATEMENT_TERMINATOR);
            tableDeclaration.append(NEW_LINE);
        }

        tableDeclaration.append(generateIndentation(tableBodyIndent));
        tableDeclaration.append(RIGHT_BRACE);
        tableDeclaration.append(NEW_LINE);
        tableDeclaration.append(generateIndentation(tableBodyIndent));
        tableDeclaration.append(ACTIONS_SECTION + EQUALS + LEFT_BRACE);
        tableDeclaration.append(NEW_LINE);
        int actionBodyIndent = keyBodyIndent;

        for(Action a: actions){
            tableDeclaration.append(generateIndentation(actionBodyIndent));
            tableDeclaration.append(a.getName());
            tableDeclaration.append(STATEMENT_TERMINATOR);
            tableDeclaration.append(NEW_LINE);            
        }

        tableDeclaration.append(generateIndentation(tableBodyIndent));
        tableDeclaration.append(RIGHT_BRACE);
        tableDeclaration.append(NEW_LINE);

        if(defaultAction != null) {
            tableDeclaration.append(generateIndentation(tableBodyIndent));
            tableDeclaration.append(DEFUALT_ACTION_DECLARATION + EQUALS);
            tableDeclaration.append(defaultAction.getName() + LEFT_PAR + RIGHT_PAR + STATEMENT_TERMINATOR);
            tableDeclaration.append(NEW_LINE);
        }

        if (flowCounter != null) {
            tableDeclaration.append(generateIndentation(tableBodyIndent));
            tableDeclaration.append(COUNTERS_DECLARATION + EQUALS);
            tableDeclaration.append(flowCounter.getName() + STATEMENT_TERMINATOR);
            tableDeclaration.append(NEW_LINE);
        }
        
        // tableDeclaration.append(NEW_LINE);
        tableDeclaration.append(generateIndentation());
        tableDeclaration.append(RIGHT_BRACE);

        return tableDeclaration.toString();
    }

    @Override
    public String compile() {
        return toString();
    }

    public String getFullyQualifiedName() {
        StringBuilder fullName = new StringBuilder();
        fullName.append(associatedComponent.getName());
        fullName.append(DOT);
        fullName.append(getName());
        return fullName.toString();
    }
}

enum MatchKind {
    EXACT,
    LPM,
    TERNARY
}

class KeyEntry {

    private String key;
    private MatchKind matchKind;
    
    public KeyEntry(String key, MatchKind matchKind) {
        this.key = key;
        this.matchKind = matchKind;
    }

    public String getKey() {
        return key;
    }

    public MatchKind getMatchKind() {
        return matchKind;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((key == null) ? 0 : key.hashCode());
        result = prime * result + ((matchKind == null) ? 0 : matchKind.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        KeyEntry other = (KeyEntry) obj;
        if (key == null) {
            if (other.key != null)
                return false;
        } else if (!key.equals(other.key))
            return false;
        if (matchKind != other.matchKind)
            return false;
        return true;
    }
}