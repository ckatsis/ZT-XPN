package org.p4sdn.app.pipeline.components;

public class Define extends Component {
    
    public static final ComponentType type = ComponentType.DEFINE;

    private String value;

    public Define(String name, String value) {
        super(name, type);
        this.value = value;
    }

    @Override
    public String toString() {
        // return "Define [value=" + value + "]";
        return "#define" + SPACE + this.getName() + SPACE + value;
    }

    @Override
    public String compile() {
        return toString();
    }
    
    
    

}
