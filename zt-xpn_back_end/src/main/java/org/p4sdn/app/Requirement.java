/**
 * ZT-XPN: An end-to-end Zero-Trust Architecture for Next Generation 
 * Programmable Networks
 * 
 * Authors:  Charalampos Katsis  (ckatsis@purdue.edu)
 *           Elisa Bertino       (bertino@purdue.edu)
 * =================================================================
 */


package org.p4sdn.app;

import org.p4sdn.app.net.Protocol;

public class Requirement {

    // The requirement is between unique names; not IP addresses
    // This allows entities to dynamically change IP addresses without impacting the policy graphs
    private String srcName;
    private String dstName;
    private Protocol protocol;

    // public Requirement() {}
    
    // public Requirement(String srcHostIp, String dstHostIp, Protocol protocol) {
    //     this.srcHostIp = srcHostIp;
    //     this.dstHostIp = dstHostIp;
    //     this.protocol = protocol;
    // }

    public Requirement(String srcName, String dstName, Protocol protocol) {
        this.srcName = srcName;
        this.dstName = dstName;
        this.protocol = protocol;
    }

    public String getSrcName() {
        return srcName;
    }

    public String getDstName() {
        return dstName;
    }

    public Protocol getProtocol() {
        return protocol;
    }

    public void setSrcName(String srcHostIp) {
        this.srcName = srcHostIp;
    }

    public void setDstName(String dstHostIp) {
        this.dstName = dstHostIp;
    }

    public void setProtocol(Protocol protocol) {
        this.protocol = protocol;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((srcName == null) ? 0 : srcName.hashCode());
        result = prime * result + ((dstName == null) ? 0 : dstName.hashCode());
        result = prime * result + ((protocol == null) ? 0 : protocol.hashCode());
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
        Requirement other = (Requirement) obj;
        if (srcName == null) {
            if (other.srcName != null)
                return false;
        } else if (!srcName.equals(other.srcName))
            return false;
        if (dstName == null) {
            if (other.dstName != null)
                return false;
        } else if (!dstName.equals(other.dstName))
            return false;
        if (protocol == null) {
            if (other.protocol != null)
                return false;
        } else if (!protocol.equals(other.protocol))
            return false;
        return true;
    }
}
