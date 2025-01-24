package org.p4sdn.app.net;

import org.onosproject.net.flow.TrafficSelector.Builder;
import org.onosproject.net.flow.criteria.Criterion;
import org.p4sdn.app.pipeline.components.Header;
import org.p4sdn.app.pipeline.components.Variable;

import java.util.HashMap;
import java.util.Map;

public class ICMProtocol extends Protocol{

    public static final String TYPE_HDR_VAR = "type";
    public static final int TYPE_HDR_VAR_SIZE = 8;
    public static final String ICODE_HDR_VAR = "icmpCode";
    public static final int ICODE_HDR_VAR_SIZE = 8;
    public static final String CSUM_HDR_VAR = "checksum";
    public static final int CSUM_HDR_VAR_SIZE = 16;
    public static final String ID_HDR_VAR = "identifier";
    public static final int ID_HDR_VAR_SIZE = 16;
    public static final String SNO_HDR_VAR = "sequence_number";
    public static final int SNO_HDR_VAR_SIZE = 16;
    public static final String TSTMP_HDR_VAR = "timestamp";
    public static final int TSTMP_HDR_VAR_SIZE = 64;

    public static final String ICMP_PROTOCOL = "ICMP";
    public static final String ICMP_FEATURES = "checksum;icmpCode;icmpType;associatedApp;";

    public static final String PARENT_TRANSITION_VALUE = "1";

    // Static map to hold the mapping between property names and Criterion.Type
    private static final Map<String, Criterion.Type> propertyToCriterionTypeMap = new HashMap<>();

    static {
        // Populate the map with the required mappings
        propertyToCriterionTypeMap.put(TYPE_HDR_VAR, Criterion.Type.ICMPV4_TYPE);
        propertyToCriterionTypeMap.put(ICODE_HDR_VAR, Criterion.Type.ICMPV4_CODE);
        // Add other mappings if needed
    }

    private short	checksum = -1;
    private byte	icmpCode = -1;
    private byte	icmpType = -1;

    public ICMProtocol(short checksum, byte icmpCode, byte icmpType) {
        super(ICMP_PROTOCOL);
        this.checksum = checksum;
        this.icmpCode = icmpCode;
        this.icmpType = icmpType;

        allowed_parent_proto.add(IProtocolV4.IP_PROTOCOL);
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
        result = prime * result + icmpCode;
        result = prime * result + icmpType;
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
        ICMProtocol other = (ICMProtocol) obj;
        if (checksum != other.checksum)
            return false;
        if (icmpCode != other.icmpCode)
            return false;
        if (icmpType != other.icmpType)
            return false;
        return true;
    }

    @Override
    public String printHeaders() {
        return "ICMProtocol [checksum=" + checksum + ", icmpCode=" + icmpCode + ", icmpType=" + icmpType + 
            ", associatedApp=" + getApplication() + "]";
    }

    @Override
    public String toString() {
        return checksum + ";" + icmpCode + ";" + icmpType + ";" + getApplication() + ";";
    }

    @Override
    public String getFeatures() {
        return ICMP_FEATURES;
    }

    @Override
    public Header generateP4Header(String name) {
        return generateDefaultICMPHeader(name);
    }

    public static Header generateDefaultICMPHeader(String name) {
        Header icmpHeader = new Header(name);

        Variable type = new Variable(TYPE_HDR_VAR, TYPE_HDR_VAR_SIZE);
        icmpHeader.addVariable(type);
        Variable icmp_code = new Variable(ICODE_HDR_VAR, ICODE_HDR_VAR_SIZE);
        icmpHeader.addVariable(icmp_code);
        Variable checksum = new Variable(CSUM_HDR_VAR, CSUM_HDR_VAR_SIZE);
        icmpHeader.addVariable(checksum);
        Variable identifier = new Variable(ID_HDR_VAR, ID_HDR_VAR_SIZE);
        icmpHeader.addVariable(identifier);
        Variable sequence_number = new Variable(SNO_HDR_VAR, SNO_HDR_VAR_SIZE);
        icmpHeader.addVariable(sequence_number);
        Variable timestamp = new Variable(TSTMP_HDR_VAR, TSTMP_HDR_VAR_SIZE);
        icmpHeader.addVariable(timestamp);
        
        return icmpHeader;
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

        if (icmpType != -1) {
            selector.matchIcmpType(icmpType);
        }

        if (icmpCode != -1) {
            selector.matchIcmpCode(icmpCode);
        }
    }
}
