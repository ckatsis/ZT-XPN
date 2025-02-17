/**
 * ZT-XPN: An end-to-end Zero-Trust Architecture for Next Generation 
 * Programmable Networks
 * 
 * Authors:  Charalampos Katsis  (ckatsis@purdue.edu)
 *           Elisa Bertino       (bertino@purdue.edu)
 * =================================================================
 */


package org.p4sdn.app.net;

import org.p4sdn.app.pipeline.components.Header;
import org.p4sdn.app.pipeline.components.Variable;
import org.onlab.packet.MacAddress;
import org.onosproject.net.flow.TrafficSelector.Builder;
import org.onosproject.net.flow.criteria.Criterion;

import java.util.HashMap;
import java.util.Map;
import java.lang.reflect.Field;

public class EthernetProtocol extends Protocol {
    public static final String ETH_DST_MAC_HDR_VAR = "destinationMAC";
    public static final int ETH_DST_MAC_HDR_VAR_SIZE = 48;
    public static final String ETH_SRC_MAC_HDR_VAR = "sourceMAC";
    public static final int ETH_SRC_MAC_HDR_VAR_SIZE = 48;
    public static final String ETH_TYPE_HDR_VAR = "etherType";
    public static final int ETH_TYPE_HDR_VAR_SIZE = 16;

    public static final String TRANSITION_CONDITION_SUFFIX = ETH_TYPE_HDR_VAR;

    // ====================
    public static final String ETHERNET_PROTOCOL = "ETHERNET";
    public static final String ETHERNET_FEATURES = "timestamp;destinationMAC;" + 
                    "etherType;piorityCode;qinQTPID;qinQVID;sourceMAC;" +
                    "vlanID;isBroadcast;isMulticast;";
    public static final String ETHER_TYPE_IPV4 = "0x800";
    public static final String ETHER_TYPE_ARP = "0x806";
    public static final String ETHER_TYPE_LLDP = "0x88cc";
    public static final String ETHER_TYPE_BDDP = "0x8942";


    // Static map to hold the mapping between property names and Criterion.Type
    private static final Map<String, Criterion.Type> propertyToCriterionTypeMap = new HashMap<>();

    static {
        // Populate the map with the required mappings
        propertyToCriterionTypeMap.put(ETH_DST_MAC_HDR_VAR, Criterion.Type.ETH_DST);
        propertyToCriterionTypeMap.put(ETH_SRC_MAC_HDR_VAR, Criterion.Type.ETH_SRC);
        propertyToCriterionTypeMap.put(ETH_TYPE_HDR_VAR, Criterion.Type.ETH_TYPE);
        // Add other mappings if needed
    }

    private String destinationMAC = "-1";
    private String etherType = "-1";
    private byte piorityCode = -1;
    private short qinQTPID = -1;
    private short qinQVID = -1;
    private String sourceMAC = "-1";
    private short vlanID = -1;;
    private boolean isBroadcast = false;
    private boolean isMulticast = false;
    private long timestamp = -1;
    
    public EthernetProtocol() {
        super(ETHERNET_PROTOCOL);
    }

    public EthernetProtocol(String destinationMAC, String etherType, byte piorityCode, short qinQTPID, short qinQVID,
            String sourceMAC, short vlanID, boolean isBroadcast, boolean isMulticast, long timestamp) {
        super(ETHERNET_PROTOCOL);
        this.destinationMAC = destinationMAC;
        this.etherType = etherType;
        this.piorityCode = piorityCode;
        this.qinQTPID = qinQTPID;
        this.qinQVID = qinQVID;
        this.sourceMAC = sourceMAC;
        this.vlanID = vlanID;
        this.isBroadcast = isBroadcast;
        this.isMulticast = isMulticast;
        this.timestamp = timestamp;
    }

    // Method to get Criterion.Type based on property name
    public static Criterion.Type getCriterionTypeByPropertyName(String propertyName) {
        return propertyToCriterionTypeMap.get(propertyName);
    }
    

    @Override
    public String printHeaders() {
        return "EthernetProtocol [timestamp= " + timestamp + ", destinationMAC=" + destinationMAC + ", etherType=" + etherType + ", piorityCode="
                + piorityCode + ", qinQTPID=" + qinQTPID + ", qinQVID=" + qinQVID + ", sourceMAC=" + sourceMAC
                + ", vlanID=" + vlanID + ", isBroadcast=" + isBroadcast + ", isMulticast=" + isMulticast + "]";
    }   
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((destinationMAC == null) ? 0 : destinationMAC.hashCode());
        result = prime * result + ((etherType == null) ? 0 : etherType.hashCode());
        result = prime * result + piorityCode;
        result = prime * result + qinQTPID;
        result = prime * result + qinQVID;
        result = prime * result + ((sourceMAC == null) ? 0 : sourceMAC.hashCode());
        result = prime * result + vlanID;
        result = prime * result + (isBroadcast ? 1231 : 1237);
        result = prime * result + (isMulticast ? 1231 : 1237);
        result = prime * result + (int) (timestamp ^ (timestamp >>> 32));
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
        EthernetProtocol other = (EthernetProtocol) obj;
        if (destinationMAC == null) {
            if (other.destinationMAC != null)
                return false;
        } else if (!destinationMAC.equals(other.destinationMAC))
            return false;
        if (etherType == null) {
            if (other.etherType != null)
                return false;
        } else if (!etherType.equals(other.etherType))
            return false;
        if (piorityCode != other.piorityCode)
            return false;
        if (qinQTPID != other.qinQTPID)
            return false;
        if (qinQVID != other.qinQVID)
            return false;
        if (sourceMAC == null) {
            if (other.sourceMAC != null)
                return false;
        } else if (!sourceMAC.equals(other.sourceMAC))
            return false;
        if (vlanID != other.vlanID)
            return false;
        if (isBroadcast != other.isBroadcast)
            return false;
        if (isMulticast != other.isMulticast)
            return false;
        if (timestamp != other.timestamp)
            return false;
        return true;
    }

    @Override
    public String toString() {
        return timestamp + ";" + destinationMAC + ";" + etherType + ";"
                + piorityCode + ";" + qinQTPID + ";" + qinQVID + ";" + sourceMAC
                + ";" + vlanID + ";" + isBroadcast + ";" + isMulticast + ";";
    }

    public void setDestinationAddress(String destinationMAC) {
        this.destinationMAC = destinationMAC;
    }

    @Override
    public String getFeatures() {
        return ETHERNET_FEATURES;
    }

    // public void setEtherType(String etherType) {
    //     this.etherType = etherType;
    // }

    public void setEtherType(String etherType) {
        
        String fieldName = "ETHER_TYPE_" + etherType.toUpperCase();

        Class<?> clazz = EthernetProtocol.class;
        // Get the Field object for the field name
        try {
            Field field = clazz.getField(fieldName);
            
            Object value = field.get(null);
            this.etherType = (String) value;
            
        } catch (NoSuchFieldException | SecurityException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Override
    public Header generateP4Header(String name) {
        return generateDefaultEthernetHeader(name);
    }       
    
    public static Header generateDefaultEthernetHeader(String name) {
        Header ethHeader = new Header(name);

        Variable destMac = new Variable(ETH_DST_MAC_HDR_VAR, ETH_DST_MAC_HDR_VAR_SIZE);
        ethHeader.addVariable(destMac);
        Variable srcMac = new Variable(ETH_SRC_MAC_HDR_VAR, ETH_SRC_MAC_HDR_VAR_SIZE);
        ethHeader.addVariable(srcMac);
        Variable etherType = new Variable(ETH_TYPE_HDR_VAR, ETH_TYPE_HDR_VAR_SIZE);
        ethHeader.addVariable(etherType);
        
        return ethHeader;
    }

    @Override
    public String getTransitionConditionSuffix() {
        return TRANSITION_CONDITION_SUFFIX;
    }

    @Override
    public String getParentTransitionValue() {
        return null;
    }

    @Override
    public String getTransitionPropertyValue() {
        return etherType;
    }

    @Override
    public void generateMatchingSelector(Builder selector) {
        if (selector == null) {
            return;
        }

        if (!destinationMAC.equals("-1")) {
            selector.matchEthDst(MacAddress.valueOf(destinationMAC));
        } 

        if (!sourceMAC.equals("-1")) {
            selector.matchEthSrc(MacAddress.valueOf(sourceMAC));
        }

        if (!etherType.equals("-1")) {
            selector.matchEthType(hexStringToShort(etherType));
        }
    }

}
