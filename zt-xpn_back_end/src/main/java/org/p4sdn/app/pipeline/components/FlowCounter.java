package org.p4sdn.app.pipeline.components;

public class FlowCounter extends Component {

    public static final ComponentType type = ComponentType.FLOW_COUNTER;
    public static final String FLOW_COUNTER_DECLARATION = "direct_counter";
    public static final String FLOW_COUNTER_NAMING_SUFFIX = "_flow_counter";

    private Component associatedComponent;
    private CounterType counterType;
    private Table associatedTable;

    public FlowCounter(Table associatedTable, CounterType counterType, Component associatedComponent){
        super(associatedTable.getName() + FLOW_COUNTER_NAMING_SUFFIX, type);
        this.counterType = counterType;
        this.associatedTable = associatedTable;
        this.associatedComponent = associatedComponent;
    }

    public void setAssociatedTable(Table associatedTable) {
        this.associatedTable = associatedTable;
    }

    public CounterType getCounterType() {
        return counterType;
    }

    @Override
    public String toString() {
        StringBuilder flowCountBuilder = new StringBuilder();
        flowCountBuilder.append(generateIndentation());
        flowCountBuilder.append(FLOW_COUNTER_DECLARATION + LEFT_PAR);
        flowCountBuilder.append(CounterType.class.getSimpleName() + DOT);
        flowCountBuilder.append(getCounterType().name().toLowerCase() + RIGHT_PAR);
        flowCountBuilder.append(getName());
        flowCountBuilder.append(STATEMENT_TERMINATOR);

        return flowCountBuilder.toString();
    }

    @Override
    public String compile() {
        return toString();
    }

    enum CounterType {
        PACKETS,
        BYTES,
        PACKETS_AND_BYTES
    }
}
