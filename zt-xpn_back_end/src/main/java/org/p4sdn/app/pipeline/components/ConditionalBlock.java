package org.p4sdn.app.pipeline.components;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class ConditionalBlock extends Statement {

    public static final String ELSE_CONDITION = "ELSE";
    public static final String IF = "if" + Component.SPACE;
    public static final String ELSEIF = "else if" + Component.SPACE;
    public static final String ELSE = "else" + Component.SPACE;

    private ArrayList<String> conditions;
    private HashMap<String, ArrayList<Statement>> conditionToStatementsMap;

    public ConditionalBlock(Statement associatedStatement, Component assosiatedComponent) {
        super(associatedStatement, assosiatedComponent);
        conditions = new ArrayList<>();
        conditionToStatementsMap = new HashMap<>();
    }

    public ConditionalBlock(ArrayList<String> conditions, HashMap<String, ArrayList<Statement>> conditionToStatementsMap, 
                    Statement associatedStatement, Component assosiatedComponent) {
        super(associatedStatement, assosiatedComponent);
        this.conditions = conditions;
        this.conditionToStatementsMap = conditionToStatementsMap;
    }

    public void addConditionAndStatements(String condition, Statement... statements) {
        // TODO: There must be some check for the statements. For e.g., it doesn't make sense to have
        //      dublicate statements at the same place
        if (condition == null) {
            condition = ELSE_CONDITION;            
        }
        
        conditions.add(condition);
        ArrayList<Statement> list = new ArrayList<>(Arrays.asList(statements));
        conditionToStatementsMap.put(condition, list);
    }

    public void addConditionAndStatements(String condition, ArrayList<Statement> statements) {
        // TODO: There must be some check for the statements. For e.g., it doesn't make sense to have
        //      dublicate statements at the same place
        conditions.add(condition);
        conditionToStatementsMap.put(condition, statements);
    }
    
    @Override
    public String toString() {
        StringBuilder conditionalBlock = new StringBuilder();
        conditionalBlock.append(generateIndentation());

        for(int i = 0; i < conditions.size(); i++) {
            String con = conditions.get(i);
            boolean isElse = false;

            if (i == 0) {
                conditionalBlock.append(IF);
            } else {
                if(con.equals(ELSE_CONDITION)){
                    isElse = true;
                    conditionalBlock.append(ELSE);
                }
                else {
                    conditionalBlock.append(ELSEIF);
                }
            }

            ArrayList<Statement> stmnts;

            if(isElse == false) {
                conditionalBlock.append(Component.LEFT_PAR);
                conditionalBlock.append(con);
                conditionalBlock.append(Component.RIGHT_PAR);
                stmnts = conditionToStatementsMap.get(con);
            } else {
                stmnts = conditionToStatementsMap.get(ELSE_CONDITION);
            }

            conditionalBlock.append(Component.LEFT_BRACE);
            conditionalBlock.append(Component.NEW_LINE);

            for(Statement s: stmnts) {
                int ifBodyIndent = this.getStatementIndentation() + 1;
                s.setCompileIndentation(ifBodyIndent);
                conditionalBlock.append(s.compile());
                conditionalBlock.append(Component.NEW_LINE);
            }

            conditionalBlock.append(generateIndentation());
            conditionalBlock.append(Component.RIGHT_BRACE);
        }

        return conditionalBlock.toString();
    }

    @Override
    public String compile() {
        return toString();
    }

}
