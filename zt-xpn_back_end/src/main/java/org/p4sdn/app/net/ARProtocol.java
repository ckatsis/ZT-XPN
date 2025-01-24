package org.p4sdn.app.net;

import java.util.Arrays;

import org.onlab.packet.Ip4Address;
import org.onlab.packet.MacAddress;
import org.onosproject.net.flow.TrafficSelector.Builder;
import org.onosproject.net.flow.criteria.Criterion;
import org.p4sdn.app.pipeline.components.Header;
import org.p4sdn.app.pipeline.components.Variable;

import java.util.HashMap;
import java.util.Map;

public class ARProtocol extends Protocol {

    public static final String HTYPE_HDR_VAR = "hardwareType";
    public static final int HTYPE_HDR_VAR_SIZE = 16;
    public static final String PTYPE_HDR_VAR = "protocolType";
    public static final int PTYPE_HDR_VAR_SIZE = 16;
    public static final String HADDLEN_HDR_VAR = "hardwareAddrLength";
    public static final int HADDLEN_HDR_VAR_SIZE = 8;
    public static final String PADDLEN_HDR_VAR = "protocolAddrLength";
    public static final int PADDLEN_HDR_VAR_SIZE = 8;
    public static final String OPCODE_HDR_VAR = "opCode";
    public static final int OPCODE_HDR_VAR_SIZE = 16;
    public static final String SHADDR_HDR_VAR = "senderHardwareAddress";
    public static final int SHADDR_HDR_VAR_SIZE = 48;
    public static final String SPADDR_HDR_VAR = "senderProtocolAddress";
    public static final int SPADDR_HDR_VAR_SIZE = 32;
    public static final String THADDR_HDR_VAR = "targetHardwareAddress";
    public static final int THADDR_HDR_VAR_SIZE = 48;
    public static final String TPADDR_HDR_VAR = "targetProtocolAddress";
    public static final int TPADDR_HDR_VAR_SIZE = 32;
    
    public static final String PARENT_TRANSITION_VALUE = "0x0806";

    public static final String ARP_PROTOCOL = "ARP";
    public static final String ARP_FEATURES = "hardwareAddressLength;hardwareType;opCode;protocolAddressLength;protocolType;" 
                + "senderHardwareAddress;senderProtocolAddress;targetHardwareAddress;targetProtocolAddress;IsGratuitous;associatedApp;";
    
    // Static map to hold the mapping between property names and Criterion.Type
    private static final Map<String, Criterion.Type> propertyToCriterionTypeMap = new HashMap<>();

    static {
        // Populate the map with the required mappings
        propertyToCriterionTypeMap.put(OPCODE_HDR_VAR, Criterion.Type.ARP_OP);
        propertyToCriterionTypeMap.put(SHADDR_HDR_VAR, Criterion.Type.ARP_SHA);
        propertyToCriterionTypeMap.put(SPADDR_HDR_VAR, Criterion.Type.ARP_SPA);
        propertyToCriterionTypeMap.put(THADDR_HDR_VAR, Criterion.Type.ARP_THA);
        propertyToCriterionTypeMap.put(TPADDR_HDR_VAR, Criterion.Type.ARP_TPA);
        // Add other mappings if needed
    }

    private byte hardwareAddressLength = -1;
    private short hardwareType = -1;
    private short opCode = -1;
    private byte protocolAddressLength = -1;
    private short protocolType = -1;
    private byte[]	senderHardwareAddress = {-1};
    private byte[]	senderProtocolAddress = {-1};
    private byte[]	targetHardwareAddress = {-1};
    private byte[]	targetProtocolAddress = {-1};
    private boolean	isGratuitous = false;


    public ARProtocol(byte hardwareAddressLength, short hardwareType, short opCode, byte protocolAddressLength,
            short protocolType, byte[] senderHardwareAddress, byte[] senderProtocolAddress,
            byte[] targetHardwareAddress, byte[] targetProtocolAddress, boolean isGratuitous) {
        super(ARP_PROTOCOL);
        this.hardwareAddressLength = hardwareAddressLength;
        this.hardwareType = hardwareType;
        this.opCode = opCode;
        this.protocolAddressLength = protocolAddressLength;
        this.protocolType = protocolType;
        this.senderHardwareAddress = senderHardwareAddress;
        this.senderProtocolAddress = senderProtocolAddress;
        this.targetHardwareAddress = targetHardwareAddress;
        this.targetProtocolAddress = targetProtocolAddress;
        this.isGratuitous = isGratuitous;

        allowed_parent_proto.add(EthernetProtocol.ETHERNET_PROTOCOL);
    }

    // Method to get Criterion.Type based on property name
    public static Criterion.Type getCriterionTypeByPropertyName(String propertyName) {
        return propertyToCriterionTypeMap.get(propertyName);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + hardwareAddressLength;
        result = prime * result + hardwareType;
        result = prime * result + opCode;
        result = prime * result + protocolAddressLength;
        result = prime * result + protocolType;
        result = prime * result + Arrays.hashCode(senderHardwareAddress);
        result = prime * result + Arrays.hashCode(senderProtocolAddress);
        result = prime * result + Arrays.hashCode(targetHardwareAddress);
        result = prime * result + Arrays.hashCode(targetProtocolAddress);
        result = prime * result + (isGratuitous ? 1231 : 1237);
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
        ARProtocol other = (ARProtocol) obj;
        if (hardwareAddressLength != other.hardwareAddressLength)
            return false;
        if (hardwareType != other.hardwareType)
            return false;
        if (opCode != other.opCode)
            return false;
        if (protocolAddressLength != other.protocolAddressLength)
            return false;
        if (protocolType != other.protocolType)
            return false;
        if (!Arrays.equals(senderHardwareAddress, other.senderHardwareAddress))
            return false;
        if (!Arrays.equals(senderProtocolAddress, other.senderProtocolAddress))
            return false;
        if (!Arrays.equals(targetHardwareAddress, other.targetHardwareAddress))
            return false;
        if (!Arrays.equals(targetProtocolAddress, other.targetProtocolAddress))
            return false;
        if (isGratuitous != other.isGratuitous)
            return false;
        return true;
    }




    @Override
    public String printHeaders() {
        return "ARProtocol [hardwareAddressLength=" + hardwareAddressLength + ", hardwareType=" + hardwareType
                + ", opCode=" + opCode + ", protocolAddressLength=" + protocolAddressLength + ", protocolType="
                + protocolType + ", senderHardwareAddress=" + Arrays.toString(senderHardwareAddress)
                + ", senderProtocolAddress=" + Arrays.toString(senderProtocolAddress) + ", targetHardwareAddress="
                + Arrays.toString(targetHardwareAddress) + ", targetProtocolAddress="
                + Arrays.toString(targetProtocolAddress) + ", isGratuitous=" + isGratuitous + 
                ", associatedApp=" + getApplication() + "]";
    }

    @Override
    public String toString() {
        return hardwareAddressLength + ";" + hardwareType + ";" + opCode + ";" + protocolAddressLength + ";"
        + protocolType + ";" + Arrays.toString(senderHardwareAddress)
        + ";" + Arrays.toString(senderProtocolAddress) + ";" + Arrays.toString(targetHardwareAddress) + ";"
        + Arrays.toString(targetProtocolAddress) + ";" + isGratuitous + ";" + getApplication() + ";";
    }

    // public String toBinaryString() {
    //     return 
    //     String.format("%" + Byte.SIZE + "s", Integer.toBinaryString(hardwareAddressLength & 0xFF)).replace(' ', '0').replaceAll(".(?!$)", "$0;") + 
    //     ";" + 
    //     String.format("%" + Short.SIZE + "s", Integer.toBinaryString(hardwareType & 0xFF)).replace(' ', '0').replaceAll(".(?!$)", "$0;") +  
    //     ";" + 
    //     String.format("%" + Short.SIZE + "s", Integer.toBinaryString(opCode & 0xFF)).replace(' ', '0').replaceAll(".(?!$)", "$0;") +  
    //     ";" + 
    //     String.format("%" + Byte.SIZE + "s", Integer.toBinaryString(protocolAddressLength & 0xFF)).replace(' ', '0').replaceAll(".(?!$)", "$0;") + 
    //     ";"+
    //     String.format("%" + Short.SIZE + "s", Integer.toBinaryString(protocolType & 0xFF)).replace(' ', '0').replaceAll(".(?!$)", "$0;") +  
    //     ";" + 
    //     String.format("%48s", new BigInteger(senderHardwareAddress).toString(2).replace(' ', '0').replaceAll(".(?!$)", "$0;")) +  
    //     ";" + Arrays.toString(senderProtocolAddress) + ";" + Arrays.toString(targetHardwareAddress) + ";"
    //     + Arrays.toString(targetProtocolAddress) + ";" + isGratuitous + ";" + getApplication() + ";";
    // }


    @Override
    public String getFeatures() {
        return ARP_FEATURES;
    }

    @Override
    public Header generateP4Header(String name) {
        return generateDefaultARPHeader(name);
    }

    public static Header generateDefaultARPHeader(String name) {
        Header arpHeader = new Header(name);

        Variable hardwareType = new Variable(HTYPE_HDR_VAR, HTYPE_HDR_VAR_SIZE);
        arpHeader.addVariable(hardwareType);
        Variable protocolType = new Variable(PTYPE_HDR_VAR, PTYPE_HDR_VAR_SIZE);
        arpHeader.addVariable(protocolType);
        Variable hardwareAddrLength = new Variable(HADDLEN_HDR_VAR, HADDLEN_HDR_VAR_SIZE);
        arpHeader.addVariable(hardwareAddrLength);
        Variable protocolAddrLength = new Variable(PADDLEN_HDR_VAR, PADDLEN_HDR_VAR_SIZE);
        arpHeader.addVariable(protocolAddrLength);
        Variable opcode = new Variable(OPCODE_HDR_VAR, OPCODE_HDR_VAR_SIZE);
        arpHeader.addVariable(opcode);
        Variable senderHardwareAddr = new Variable(SHADDR_HDR_VAR, SHADDR_HDR_VAR_SIZE);
        arpHeader.addVariable(senderHardwareAddr);
        Variable senderProtocolAddr = new Variable(SPADDR_HDR_VAR, SPADDR_HDR_VAR_SIZE);
        arpHeader.addVariable(senderProtocolAddr);
        Variable targetHardwareAddr = new Variable(THADDR_HDR_VAR, THADDR_HDR_VAR_SIZE);
        arpHeader.addVariable(targetHardwareAddr);
        Variable targetProtocolAddr = new Variable(TPADDR_HDR_VAR, TPADDR_HDR_VAR_SIZE);
        arpHeader.addVariable(targetProtocolAddr);
        
        return arpHeader;
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

        if (opCode != -1) {
            selector.matchArpOp(opCode);
        }
        
        if (senderHardwareAddress.length != 1 && senderHardwareAddress[0] != -1) {
            selector.matchArpSha(MacAddress.valueOf(senderHardwareAddress));
        }

        if (senderProtocolAddress.length != 1 && senderProtocolAddress[0] != -1) {
            selector.matchArpSpa(Ip4Address.valueOf(senderProtocolAddress));
        }

        if (targetHardwareAddress.length != 1 && targetHardwareAddress[0] != -1) {
            selector.matchArpTha(MacAddress.valueOf(targetHardwareAddress));            
        }

        if (targetProtocolAddress.length != -1 && targetProtocolAddress[0] != -1) {
            selector.matchArpTpa(Ip4Address.valueOf(targetProtocolAddress));
        }
    }

}
