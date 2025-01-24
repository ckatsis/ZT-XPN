package org.p4sdn.app.pipeline.components;

import java.util.ArrayList;

public class Action extends Component {

    public static final ComponentType type = ComponentType.PARSER_ACTION;
    public static final String ACTION = "action" + SPACE;
    public static final String RECIRCULATION_FUNCTION_NAME = "recirculate_preserving_field_list";

    private Component associatedComponent;
    private ArrayList<ActionParameter> parameters = new ArrayList<>();
    private ArrayList<Statement> statements = new ArrayList<>();

    public Action(String name, Component associatedComponent) {
        super(name, type);
        this.associatedComponent = associatedComponent;
    }

    public Action(String name, Component associatedComponent, 
                ArrayList<ActionParameter> parameters, ArrayList<Statement> statements) {
        super(name, type);
        this.associatedComponent = associatedComponent;
        this.parameters = parameters;
        this.statements = statements;
    }

    public void addParameters(ActionParameter... params) {
        for(ActionParameter p: params){
            if(!this.parameters.contains(p)) {
                this.parameters.add(p);
            }
        }
    }

    public void addStatements(Statement... statements) {
        for(Statement s: statements){
            if(!this.statements.contains(s)) {
                this.statements.add(s);
            }
        }
    }

    public ArrayList<ActionParameter> getParameters() {
        return parameters;
    }

    public InLineStatement getInlineInvocation(String... params) {
        StringBuilder invocation = new StringBuilder();
        invocation.append(getName() + Component.LEFT_PAR);

        for(int i = 0; i < params.length; i++) {
            invocation.append(params[i]);
            if(i + 1 < params.length)
                invocation.append(COMMA);
        }

        invocation.append(Component.RIGHT_PAR);
        InLineStatement s = new InLineStatement(null, this, invocation.toString());
        return s;
    }

    public static InLineStatement recirculatePacket(
        int index, 
        Statement associatedStatement, 
        Component associatedComponent) {
            
        StringBuilder recircBuilder = new StringBuilder();
        recircBuilder.append(RECIRCULATION_FUNCTION_NAME);
        recircBuilder.append(LEFT_PAR + String.valueOf(index) + RIGHT_PAR);
        return new InLineStatement(associatedStatement, associatedComponent, recircBuilder.toString());
    }

    @Override
    public String toString() {
        StringBuilder actionDeclaration = new StringBuilder();
        actionDeclaration.append(generateIndentation());
        actionDeclaration.append(ACTION);
        actionDeclaration.append(getName());
        actionDeclaration.append(LEFT_PAR);

        for(int i = 0; i < parameters.size(); i++) {
            actionDeclaration.append(parameters.get(i).compile());
            
            if(i + 1 < parameters.size())
                actionDeclaration.append(COMMA);
        }

        actionDeclaration.append(RIGHT_PAR);
        actionDeclaration.append(LEFT_BRACE);
        actionDeclaration.append(NEW_LINE);

        for(Statement s: statements){
            int actionBodyIndent = getComponentIndentation() + 1;
            s.setCompileIndentation(actionBodyIndent);
            actionDeclaration.append(s.compile());
            actionDeclaration.append(NEW_LINE);
        }

        actionDeclaration.append(generateIndentation());
        actionDeclaration.append(RIGHT_BRACE);

        return actionDeclaration.toString();
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
