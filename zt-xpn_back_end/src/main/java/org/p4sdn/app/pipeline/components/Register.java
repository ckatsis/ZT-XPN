package org.p4sdn.app.pipeline.components;

public class Register extends Component {

    public static final ComponentType type = ComponentType.REGISTER;
    public static final String REGISTER_DECLERATION = "register";
    public static final String BITWIDTH_DECLERATION = "bit";
    public static final String READ_FUNCTION = "read";
    public static final String WRITE_FUNCTION = "write";

    private Component associatedComponent;
    private int totalSize = 0;
    private int index = -1;
    private final int elementBitWidth;

    public Register(String name, int elementBitWidth, Component associatedComponent) {
        super(name, type);
        this.elementBitWidth = elementBitWidth;
        this.associatedComponent = associatedComponent;
    }

    private void incrementRegisterSize() {
        this.totalSize += 1;
    }

    public InLineStatement inLineRead (
        Statement associatedStatement,
        Component associatedComponent,
        Variable storeVar, Variable readIndex) {
        InLineStatement readStatement = new InLineStatement(associatedStatement, associatedComponent, 
            getName() + DOT + READ_FUNCTION + LEFT_PAR + storeVar.getName() + COMMA + readIndex.getName() + 
            RIGHT_PAR);
        return readStatement;
    }

    public InLineStatement inLineWrite (
        Statement associatedStatement,
        Component associatedComponent,
        Variable index, String value
    ) {
        InLineStatement writeStatement = new InLineStatement(associatedStatement, associatedComponent, 
            getName() + DOT + WRITE_FUNCTION + LEFT_PAR + index.getName() + COMMA + value + RIGHT_PAR
        );
        return writeStatement;
    }

    private int getNewIndex() {
        this.index += 1;
        int alloc = index;
        incrementRegisterSize();

        return alloc;
    }

    public int allocateState() {
        return getNewIndex();
    }

    @Override
    public String toString() {
        StringBuilder register = new StringBuilder();

        String decleration = String.format(REGISTER_DECLERATION + LESS +
                    BITWIDTH_DECLERATION + LESS +
                    "%s" + GREATER + GREATER + LEFT_PAR +
                    "%s" + RIGHT_PAR + getName(), elementBitWidth, totalSize);

        register.append(generateIndentation());
        register.append(decleration);
        register.append(STATEMENT_TERMINATOR);

        return register.toString();
    }

    @Override
    public String compile() {
        return toString();
    }
    
}
