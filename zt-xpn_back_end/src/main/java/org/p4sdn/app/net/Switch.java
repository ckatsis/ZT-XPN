package org.p4sdn.app.net;

import java.util.HashSet;

public abstract class Switch {

    private static final String prefix = "device:bmv2:s";
    private static int currentId = 0;
    // private int swId;
    private String name;
    private HashSet<Host> hostConnections = new HashSet<>();

    public Switch() {
        // swId = currentId;
        currentId++;
        name = prefix + Integer.toString(currentId);
    }

    // public int getSwId() {
    //     return swId;
    // }

    public String getName() {
        return name;
    }

    public void addHost(Host host) {
        hostConnections.add(host);
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
        Switch other = (Switch) obj;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        return true;
    }

    // @Override
    // public int hashCode() {
    //     final int prime = 31;
    //     int result = 1;
    //     result = prime * result + ((swId == null) ? 0 : swId.hashCode());
    //     return result;
    // }

    // @Override
    // public boolean equals(Object obj) {
    //     if (this == obj)
    //         return true;
    //     if (obj == null)
    //         return false;
    //     if (getClass() != obj.getClass())
    //         return false;
    //     Switch other = (Switch) obj;
    //     if (swId == null) {
    //         if (other.swId != null)
    //             return false;
    //     } else if (!swId.equals(other.swId))
    //         return false;
    //     return true;
    // }

    

    // @Override
    // public int hashCode() {
    //     final int prime = 31;
    //     int result = 1;
    //     result = prime * result + swId;
    //     return result;
    // }

    // @Override
    // public boolean equals(Object obj) {
    //     if (this == obj)
    //         return true;
    //     if (obj == null)
    //         return false;
    //     if (getClass() != obj.getClass())
    //         return false;
    //     Switch other = (Switch) obj;
    //     if (swId != other.swId)
    //         return false;
    //     return true;
    // }
}
