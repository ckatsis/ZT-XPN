/**
 * ZT-XPN: An end-to-end Zero-Trust Architecture for Next Generation 
 * Programmable Networks
 * 
 * Authors:  Charalampos Katsis  (ckatsis@purdue.edu)
 *           Elisa Bertino       (bertino@purdue.edu)
 * =================================================================
 */


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
