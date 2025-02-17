/**
 * ZT-XPN: An end-to-end Zero-Trust Architecture for Next Generation 
 * Programmable Networks
 * 
 * Authors:  Charalampos Katsis  (ckatsis@purdue.edu)
 *           Elisa Bertino       (bertino@purdue.edu)
 * =================================================================
 */


package org.p4sdn.app.net;

public class Host {

    private String name;
    private String ipv4;
    private EdgeSwitch edgeSwitchConnection;


    public Host(String name, String ipv4, EdgeSwitch edgeSwitchConnection) {
        this.name = name;
        this.ipv4 = ipv4;
        this.edgeSwitchConnection = edgeSwitchConnection;
    }


    public String getName() {
        return name;
    }


    public String getIpv4() {
        return ipv4;
    }


    public EdgeSwitch getEdgeSwitchConnection() {
        return edgeSwitchConnection;
    }


    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
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
        Host other = (Host) obj;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        return true;
    }

    
    

}
