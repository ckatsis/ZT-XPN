package org.p4sdn.app.pipeline.components;

public abstract class Parameter extends Component {

    private Component associatedComponent;

    public Parameter(String name, ComponentType type, Component associatedComponent) {
        super(name, type);
        this.associatedComponent = associatedComponent;
    }
}
