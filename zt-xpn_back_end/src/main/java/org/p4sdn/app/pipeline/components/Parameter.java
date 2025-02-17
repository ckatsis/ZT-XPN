/**
 * ZT-XPN: An end-to-end Zero-Trust Architecture for Next Generation 
 * Programmable Networks
 * 
 * Authors:  Charalampos Katsis  (ckatsis@purdue.edu)
 *           Elisa Bertino       (bertino@purdue.edu)
 * =================================================================
 */


package org.p4sdn.app.pipeline.components;

public abstract class Parameter extends Component {

    private Component associatedComponent;

    public Parameter(String name, ComponentType type, Component associatedComponent) {
        super(name, type);
        this.associatedComponent = associatedComponent;
    }
}
