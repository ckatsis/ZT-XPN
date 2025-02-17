/**
 * ZT-XPN: An end-to-end Zero-Trust Architecture for Next Generation 
 * Programmable Networks
 * 
 * Authors:  Charalampos Katsis  (ckatsis@purdue.edu)
 *           Elisa Bertino       (bertino@purdue.edu)
 * =================================================================
 */


package org.p4sdn.app.net;

import org.onlab.packet.TpPort;
import org.onosproject.net.flow.TrafficSelector.Builder;
import org.onosproject.net.flow.criteria.Criterion;
import org.p4sdn.app.pipeline.components.Header;
import org.p4sdn.app.pipeline.components.Variable;

import java.util.HashMap;
import java.util.Map;

public class UDProtocol extends Protocol {

    public static final String SPORT_HDR_VAR = "sourcePort";
    public static final int SPORT_HDR_VAR_SIZE = 16;
    public static final String DPORT_HDR_VAR = "destinationPort";
    public static final int DPORT_HDR_VAR_SIZE = 16;
    public static final String LEN_HDR_VAR = "length";
    public static final int LEN_HDR_VAR_SIZE = 16;
    public static final String CSUM_HDR_VAR = "checksum";
    public static final int CSUM_HDR_VAR_SIZE = 16;
    public static final byte UDP_IP_ID = 0x11;
    public static final String PARENT_TRANSITION_VALUE = "17";
    
    public static final String UDP_PROTOCOL = "UDP";
    public static final String UDP_FEATURES = "checksum;destinationPort;length;sourcePort;associatedApp;";

    // Static map to hold the mapping between property names and Criterion.Type
    private static final Map<String, Criterion.Type> propertyToCriterionTypeMap = new HashMap<>();

    static {
        // Populate the map with the required mappings
        propertyToCriterionTypeMap.put(SPORT_HDR_VAR, Criterion.Type.UDP_SRC);
        propertyToCriterionTypeMap.put(DPORT_HDR_VAR, Criterion.Type.UDP_DST);
        // Add other mappings if needed
    }

    private short checksum = -1;
    private int destinationPort = -1;	
    private short length = -1;
    private int	sourcePort = -1;

    public UDProtocol() {
        super(UDP_PROTOCOL);
        allowed_parent_proto.add(IProtocolV4.IP_PROTOCOL);
    }

    public UDProtocol(short checksum, int destinationPort, short length, int sourcePort) {
        super(UDP_PROTOCOL);
        this.checksum = checksum;
        this.destinationPort = destinationPort;
        this.length = length;
        this.sourcePort = sourcePort;
        allowed_parent_proto.add(IProtocolV4.IP_PROTOCOL);
    }

    public void setDestinationPort(int destinationPort) {
        this.destinationPort = destinationPort;
    }

    public void setSourcePort(int sourcePort) {
        this.sourcePort = sourcePort;
    }

    // Method to get Criterion.Type based on property name
    public static Criterion.Type getCriterionTypeByPropertyName(String propertyName) {
        return propertyToCriterionTypeMap.get(propertyName);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + checksum;
        result = prime * result + destinationPort;
        result = prime * result + length;
        result = prime * result + sourcePort;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        UDProtocol other = (UDProtocol) obj;
        if (checksum != other.checksum)
            return false;
        if (destinationPort != other.destinationPort)
            return false;
        if (length != other.length)
            return false;
        if (sourcePort != other.sourcePort)
            return false;
        return true;
    }

    @Override
    public String toString() {
        return checksum + ";" + destinationPort + ";" + length + ";" + sourcePort + ";" + getApplication() + ";";
    }

    @Override
    public String printHeaders() {
        return "UDProtocol [checksum=" + checksum + ", destinationPort=" + destinationPort + ", length=" + length
                + ", sourcePort=" + sourcePort + ", associatedApp=" + getApplication() + "]";
    }

    @Override
    public String getFeatures() {
        return UDP_FEATURES;
    }



    @Override
    public Header generateP4Header(String name) {
        return generateDefaultUDPHeader(name);
    }

    public static Header generateDefaultUDPHeader(String name) {
        Header udpHeader = new Header(name);

        Variable src_port = new Variable(SPORT_HDR_VAR, SPORT_HDR_VAR_SIZE);
        udpHeader.addVariable(src_port);
        Variable dst_port = new Variable(DPORT_HDR_VAR, DPORT_HDR_VAR_SIZE);
        udpHeader.addVariable(dst_port);
        Variable len = new Variable(LEN_HDR_VAR, LEN_HDR_VAR_SIZE);
        udpHeader.addVariable(len);
        Variable checksum = new Variable(CSUM_HDR_VAR, CSUM_HDR_VAR_SIZE);
        udpHeader.addVariable(checksum);
        
        return udpHeader;
    }

    @Override
    public String getTransitionConditionSuffix() {
        return null;
    }


    @Override
    public String getParentTransitionValue() {
        return PARENT_TRANSITION_VALUE;
    }

    @Override
    public String getTransitionPropertyValue() {
        return null;
    }

    @Override
    public void generateMatchingSelector(Builder selector) {
        if (selector == null) {
            return;
        }

        if (sourcePort != -1) {
            selector.matchUdpSrc(TpPort.tpPort(sourcePort));
        }

        if (destinationPort != -1) {
            selector.matchUdpDst(TpPort.tpPort(destinationPort));
        }
    }
}