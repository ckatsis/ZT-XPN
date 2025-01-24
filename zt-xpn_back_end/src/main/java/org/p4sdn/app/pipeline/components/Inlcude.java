package org.p4sdn.app.pipeline.components;

public class Inlcude extends Component {

    public static final ComponentType type = ComponentType.INCLUDE;

    public Inlcude(String name) {
        super(name, type);
    }    

    @Override
    public String compile() {
        return this.toString();
    }

    @Override
    public String toString() {
        return "#include<" + this.getName() + ">";
    }
}
