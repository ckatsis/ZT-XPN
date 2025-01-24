package org.p4sdn.app.pipeline.components;

public abstract class Component {

    public static final String SPACE = " ";
    public static final String STATEMENT_TERMINATOR = ";";
    public static final String COMMA = "," + SPACE;
    public static final String DOT = ".";
    public static final String COLON = SPACE + ":" + SPACE;
    public static final String EQUALS = SPACE + "=" + SPACE;
    public static final String LEFT_BRACE = SPACE + "{" + SPACE;
    public static final String LEFT_PAR = SPACE + "(" + SPACE;
    public static final String RIGHT_PAR = SPACE + ")" + SPACE;
    public static final String RIGHT_BRACE = SPACE + "}" + SPACE;
    public static final String INDENTATION = "\t";
    public static final String QUOTE = "\"";
    public static final String NEW_LINE = "\n";
    public static final String HEADER_EXTENSION = "_t";
    public static final String STRUCT_EXTENSION = "_t";
    public static final String NOT_EQUAL = SPACE + "!=" + SPACE;
    public static final String FALSE = "false";
    public static final String LESS = "<";
    public static final String GREATER = ">";
    public static final String IS_EQUAL = SPACE + "==" + SPACE;
    public static final String AND = SPACE + "&&" + SPACE;


    private String name;
    private ComponentType type;
    private int compileIndentation = 0;
    private boolean isExtern = false;

    public Component(String name, ComponentType type) {
        this.name = name;
        this.type = type;
    }

    public abstract String compile();

    public String getName() {
        return name;
    }

    public ComponentType getType() {
        return type;
    }

    public void setExtern(boolean value) {
        this.isExtern = value;
    }

    public void setCompileIndentation(int compileIndentation) {
        this.compileIndentation = compileIndentation;
    }

    public int getComponentIndentation() {
        return compileIndentation;
    }

    public boolean isExtern() {
        return isExtern;
    }

    public String generateIndentation() {
        StringBuilder indent = new StringBuilder();
        
        for(int i = 0; i < compileIndentation; i++)
            indent.append(INDENTATION);
        
        return indent.toString();
    }

    public static String generateIndentation(int indentation) {
        StringBuilder indent = new StringBuilder();
        
        for(int i = 0; i < indentation; i++)
            indent.append(INDENTATION);
        
        return indent.toString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((type == null) ? 0 : type.hashCode());
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
        Component other = (Component) obj;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        if (type != other.type)
            return false;
        return true;
    }
}
