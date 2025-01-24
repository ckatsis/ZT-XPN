package org.p4sdn.app.net;

import java.lang.reflect.Field;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.onlab.packet.ARP;
import org.onlab.packet.Ethernet;
import org.onlab.packet.ICMP;
import org.onlab.packet.IPv4;
import org.onlab.packet.PIM;
import org.onlab.packet.TCP;
import org.onlab.packet.UDP;
import org.onosproject.net.flow.DefaultTrafficSelector;
import org.onosproject.net.flow.TrafficSelector;
import org.onosproject.net.flow.criteria.Criterion;


import org.p4sdn.app.exceptions.ProtocolEncapsulationException;
import org.p4sdn.app.pipeline.components.Header;
import org.slf4j.Logger;

public abstract class Protocol {

    private String name;
    public HashSet<String> allowed_parent_proto = new HashSet<>();
    private String associated_application = null;
    private Protocol encapsulated_proto = null;
    private boolean keepState = false;              // Set at the policy orchestration time
    private Protocol dependencyProtocol = null;     // Set at the policy orchestration time
    private int stateId = -1;                       // Set at the dataplane requirements processing
    private int fid = -1;                           // Set at the policy orchestration time
    private Switch stateEnforcementSwitch = null;   // Set at the policy orchestration time

    public Protocol(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public abstract String printHeaders();

    @Override
    public String toString() {
        return "Protocol [name=" + name + "]";
    }

    public abstract String getFeatures();

    public void setApplication(String app) {
        this.associated_application = app;
    }

    public String getApplication() {
        return this.associated_application;
    }

    public Protocol getEncapsulated_proto() {
        return encapsulated_proto;
    }

    public boolean isKeepState() {
        return keepState;
    }

    public void setKeepState(boolean keepState) {
        this.keepState = keepState;
    }

    public Protocol getDependencyProtocol() {
        return dependencyProtocol;
    }

    // public int getDependencyStateId() {
    //     return dependencyStateId;
    // }

    // public void setDependencyStateId(int dependencyStateId) {
    //     this.dependencyStateId = dependencyStateId;
    // }

    public void setDependencyProtocol(Protocol dependencyProtocol) {
        this.dependencyProtocol = dependencyProtocol;
    }

    public Switch getStateEnforcementSwitch() {
        return stateEnforcementSwitch;
    }

    public void setStateEnforcementSwitch(Switch stateEnforcementSwitch) {
        this.stateEnforcementSwitch = stateEnforcementSwitch;
    }

    public int getStateId() {
        return stateId;
    }

    public void setStateId(int stateId) {
        this.stateId = stateId;
    }

    public int getFid() {
        return fid;
    }

    public void setFid(int fid) {
        this.fid = fid;
    }

    public boolean isEncapsulationAllowed(String parent) {
        if(allowed_parent_proto.contains(parent))
            return true;
        else
            return false;
    }

    public void setEncapsulated_proto(Protocol child_proto) 
            throws ProtocolEncapsulationException {
        if(child_proto.isEncapsulationAllowed(this.name))
            this.encapsulated_proto = child_proto;
    }    

    public abstract Header generateP4Header(String name);

    public abstract String getTransitionConditionSuffix();
    
    // public abstract String getExtractSuffix();
    public String getExtractSuffix() {
        StringBuilder extract = new StringBuilder();
        extract.append(getName().toLowerCase());
        return extract.toString();
    }

    public abstract String getParentTransitionValue();

    public abstract String getTransitionPropertyValue();

    public static Protocol generateFromArguments(String protocolName, int srcIp, int dstIp, int[] ports) 
                throws ProtocolEncapsulationException {
        Protocol protocol = new EthernetProtocol();

        if(protocolName.equals("TCP")) {
            ((EthernetProtocol) protocol).setEtherType("IPV4");
            IProtocolV4 ipv4 = new IProtocolV4();
            // ipv4.setSourceAddress(strToIntIpConverter(srcIp));
            ipv4.setSourceAddress(srcIp);
            // ipv4.setDestinationAddress(strToIntIpConverter(dstIp));
            ipv4.setDestinationAddress(dstIp);
            ((EthernetProtocol) protocol).setEncapsulated_proto(ipv4);
            TCProtocol tcp = new TCProtocol();
            // TODO: This needs to be fixed. I am not sure why do we need an array here. I am doing this because of neutron output for now.
            tcp.setDestinationPort(ports[1]);
            tcp.setSourcePort(ports[0]);
            ipv4.setEncapsulated_proto(tcp);
            ipv4.setProto(TCProtocol.TCP_IP_ID);
        } else if (protocolName.equals("UDP")) {
            ((EthernetProtocol) protocol).setEtherType("IPV4");
            IProtocolV4 ipv4 = new IProtocolV4();
            ipv4.setSourceAddress(srcIp);
            ipv4.setDestinationAddress(dstIp);
            ((EthernetProtocol) protocol).setEncapsulated_proto(ipv4);
            UDProtocol udp = new UDProtocol();
            udp.setDestinationPort(ports[1]);
            udp.setSourcePort(ports[0]);
            ipv4.setEncapsulated_proto(udp);
            ipv4.setProto(UDProtocol.UDP_IP_ID);            
        } else {
            protocol = null;
        }

        return protocol;
    }

    public static String shortToHexStringConv(short shortValue) {
        String hexString = "0x" + Integer.toHexString(shortValue & 0xFFFF);
        return hexString;
    }

    public static short hexStringToShort(String hexString) {
        // Check if the string starts with "0x" and remove it if present
        if (hexString.startsWith("0x") || hexString.startsWith("0X")) {
            hexString = hexString.substring(2);
        }
        
        // Parse the string as a short using radix 16
        return (short) Integer.parseInt(hexString, 16);
    }

    public static Protocol processEthernetPacket(Ethernet ethPkt) 
            throws ProtocolEncapsulationException {

        Protocol proto = new EthernetProtocol(ethPkt.getDestinationMAC().toString(),
                        shortToHexStringConv(ethPkt.getEtherType()), ethPkt.getPriorityCode(), ethPkt.getQinQTPID(), 
                        ethPkt.getQinQVID(), ethPkt.getSourceMAC().toString(), ethPkt.getVlanID(),  
                        ethPkt.isBroadcast(), ethPkt.isMulticast(), -1);

        if (ethPkt.getEtherType() == Ethernet.TYPE_IPV4) {
            processIpV4Packet(proto, (IPv4) ethPkt.getPayload());
        } else if (ethPkt.getEtherType() == Ethernet.TYPE_ARP || ethPkt.getEtherType() == Ethernet.TYPE_RARP) {
            processARP(proto, (ARP) ethPkt.getPayload());
        } else {
            throw new ProtocolEncapsulationException(String.valueOf(ethPkt.getEtherType()), proto.getName());
        }

        return proto;
    }

    public static void processIpV4Packet(
        Protocol parent_proto,
        IPv4 ipv4Pkt) throws ProtocolEncapsulationException {
        
        Protocol proto = new IProtocolV4(ipv4Pkt.getDestinationAddress(), ipv4Pkt.getChecksum(), 
                ipv4Pkt.getDiffServ(), ipv4Pkt.getDscp(), ipv4Pkt.getEcn(), ipv4Pkt.getFlags(), 
                ipv4Pkt.getFragmentOffset(), ipv4Pkt.getHeaderLength(), ipv4Pkt.getIdentification(), ipv4Pkt.getOptions(), 
                ipv4Pkt.getProtocol(), ipv4Pkt.getSourceAddress(), ipv4Pkt.getTotalLength(), 
                ipv4Pkt.getTtl(), ipv4Pkt.getVersion(), ipv4Pkt.isTruncated());
        
        parent_proto.setEncapsulated_proto(proto);

        if(ipv4Pkt.getProtocol() == IPv4.PROTOCOL_TCP) {
            processTcpPacket(proto, (TCP)ipv4Pkt.getPayload());
        } else if (ipv4Pkt.getProtocol() == IPv4.PROTOCOL_UDP) {
            processUdpPacket(proto, (UDP)ipv4Pkt.getPayload());
        } else if (ipv4Pkt.getProtocol() == IPv4.PROTOCOL_ICMP) {
            processIcmpPacket(proto, (ICMP)ipv4Pkt.getPayload());
        }else if (ipv4Pkt.getProtocol() == IPv4.PROTOCOL_PIM) {
            processPIM(proto, (PIM)ipv4Pkt.getPayload());
        } else {
            throw new ProtocolEncapsulationException(String.valueOf(ipv4Pkt.getProtocol()), proto.getName());
        }
    }

    public static void  processTcpPacket(
        Protocol parent_proto,
        TCP tcpPckt
        ) throws ProtocolEncapsulationException 
    {
        Protocol proto =  new TCProtocol(tcpPckt.getAcknowledge(), tcpPckt.getChecksum(), tcpPckt.getDataOffset(), 
            tcpPckt.getDestinationPort(), tcpPckt.getFlags(), tcpPckt.getOptions(), tcpPckt.getSequence(), 
            tcpPckt.getSourcePort(), tcpPckt.getUrgentPointer(), tcpPckt.getWindowSize());
        
        parent_proto.setEncapsulated_proto(proto);
    }

    public static void  processIcmpPacket(
        Protocol parent_proto,
        ICMP icmpPckt) throws ProtocolEncapsulationException 
    {
        Protocol proto = new ICMProtocol(icmpPckt.getChecksum(), 
                icmpPckt.getIcmpCode(), icmpPckt.getIcmpType());
        parent_proto.setEncapsulated_proto(proto);
    }

    public static void  processUdpPacket(
        Protocol parent_proto,
        UDP udpPckt) throws ProtocolEncapsulationException 
    {
        Protocol proto = new UDProtocol(udpPckt.getChecksum(), udpPckt.getDestinationPort(), 
            udpPckt.getLength(), udpPckt.getSourcePort());
        parent_proto.setEncapsulated_proto(proto);
    }

    public static void processARP (
        Protocol parent_proto,
        ARP arpPckt) throws ProtocolEncapsulationException 
        {
            Protocol proto = new ARProtocol(arpPckt.getHardwareAddressLength(), 
                arpPckt.getHardwareType(), arpPckt.getOpCode(), arpPckt.getProtocolAddressLength(), 
                arpPckt.getProtocolType(), arpPckt.getSenderHardwareAddress(), arpPckt.getSenderProtocolAddress(), 
                arpPckt.getSenderHardwareAddress(), arpPckt.getTargetProtocolAddress(), arpPckt.isGratuitous());
            parent_proto.setEncapsulated_proto(proto);
        }
    
    public static void processPIM (
        Protocol parent_proto,
        PIM pimPckt) throws ProtocolEncapsulationException 
    {
        Protocol proto = new PIMProtocol(pimPckt.getChecksum(), pimPckt.getPimMsgType(), pimPckt.getReserved(), 
                pimPckt.getVersion());
        parent_proto.setEncapsulated_proto(proto);
    }

    public static Protocol generateFromArguments(String protocolName, String etherType, Logger l) 
                throws ProtocolEncapsulationException {
        Protocol protocol = new EthernetProtocol();

        if(protocolName.equals("ETHERNET")) {
            // l.info("Setting ether type to " + etherType);
            ((EthernetProtocol) protocol).setEtherType(etherType);
            String val = ((EthernetProtocol) protocol).getTransitionPropertyValue();
            // l.info("Eth type is " + etherType + " Val is " + val);
        } else {
            protocol = null;
        }

        return protocol;
    }

    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((allowed_parent_proto == null) ? 0 : allowed_parent_proto.hashCode());
        result = prime * result + ((associated_application == null) ? 0 : associated_application.hashCode());
        result = prime * result + ((encapsulated_proto == null) ? 0 : encapsulated_proto.hashCode());
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
        Protocol other = (Protocol) obj;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        if (allowed_parent_proto == null) {
            if (other.allowed_parent_proto != null)
                return false;
        } else if (!allowed_parent_proto.equals(other.allowed_parent_proto))
            return false;
        if (associated_application == null) {
            if (other.associated_application != null)
                return false;
        } else if (!associated_application.equals(other.associated_application))
            return false;
        if (encapsulated_proto == null) {
            if (other.encapsulated_proto != null)
                return false;
        } else if (!encapsulated_proto.equals(other.encapsulated_proto))
            return false;
        return true;
    }

    // Convert byte array to integer
    private static int strToIntIpConverter(String ipString) {
        InetAddress ipAddress = null;
        try {
            ipAddress = InetAddress.getByName(ipString);
        } catch (UnknownHostException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        byte[] byteArray = ipAddress.getAddress();
        int result = 0;
        for (byte b : byteArray) {
            result = (result << 8) | (b & 0xFF);
        }
        return result;
    }

    public List<String> getAssignedProperties(String optionalPrefix) 
                throws IllegalArgumentException, IllegalAccessException {
                    
        List<String> assignedProperties = new ArrayList<>();
        Field[] fields = getClass().getDeclaredFields();
        String prefix = "";

        if(optionalPrefix == null)
            prefix = "";
        else
            prefix = optionalPrefix;

        for (Field field : fields) {
            if(!java.lang.reflect.Modifier.isStatic(field.getModifiers())) {
                if (field.getType() == int.class || field.getType() == byte.class 
                            || field.getType() == short.class) {
                    field.setAccessible(true);
                    int value = field.getInt(this);
                    if (value != -1) {
                        assignedProperties.add(prefix + field.getName());
                    }
                } else if (field.getType() == String.class) {
                    field.setAccessible(true);
                    String value = (String) field.get(this);
                    if (value != null && (!value.equals("-1"))) {
                        assignedProperties.add(prefix + field.getName());
                    }
                } else if (field.getType() == boolean.class) {
                    field.setAccessible(true);
                    boolean value = field.getBoolean(this);
                    if (value) {
                        assignedProperties.add(prefix + field.getName());
                    }
                } else if (field.getType() == byte[].class) {
                    field.setAccessible(true);
                    byte[] value = (byte[]) field.get(this);
                    if (value != null && value.length == 1) {
                        // assignedProperties.add(field.getName());
                        for (byte b : value) {
                            if (b != -1) {
                                assignedProperties.add(prefix + field.getName());
                            }
                        }
                    }
                }
            }
        }
        return assignedProperties;
    }

    public static TrafficSelector.Builder generateProtcolSelector(Protocol policy) {
        TrafficSelector.Builder selectorBuilder = DefaultTrafficSelector.builder();
        Protocol curr = policy;
        
        while (curr != null) {
            curr.generateMatchingSelector(selectorBuilder);
            curr = curr.getEncapsulated_proto();
        }

        return selectorBuilder;
    }

    public abstract void generateMatchingSelector(TrafficSelector.Builder selector);

    public List<String> getProtocolPropertyValues() {
        List<String> propertyValues = new ArrayList<>();
        
        // Get the class of the object
        Class<?> objClass = getClass();

        // Get all declared fields of the class
        Field[] fields = objClass.getDeclaredFields();

        for (Field field : fields) {
            if(!java.lang.reflect.Modifier.isStatic(field.getModifiers())) {
                field.setAccessible(true); // Make private fields accessible
                try {
                    // Get the value of the field
                    Object value = field.get(this);
                    // Convert the value to String and add to the list
                    if (value != null) {
                        if (field.getType() == byte[].class) {
                            byte[] byte_arr = (byte[]) value;

                            if (byte_arr.length == 1 && byte_arr[0] == -1) {
                                propertyValues.add("-1");
                            } else
                                propertyValues.add(Integer.toString(Arrays.hashCode(byte_arr)));
                                
                        } else if (field.getType() == HashMap.class) {
                            HashMap map = (HashMap) field.get(this);

                            if (map.keySet().size() == 0) {
                                propertyValues.add("-1"); 
                            } else 
                                propertyValues.add(Integer.toString(map.hashCode()));
                        } else
                            propertyValues.add(value.toString());
                    } 
                    else {
                        propertyValues.add("-1"); // null
                    }
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                    propertyValues.add("Error retrieving value");
                }
            }
        }

        return propertyValues;
    }


    public static Criterion.Type getCriterionType(String protocol, String protocolHeader) {
        protocol = protocol.toUpperCase();
        
        if(protocol.equals(EthernetProtocol.ETHERNET_PROTOCOL)) {
            return EthernetProtocol.getCriterionTypeByPropertyName(protocolHeader);
        } else if(protocol.equals(IProtocolV4.IP_PROTOCOL)) {
            return IProtocolV4.getCriterionTypeByPropertyName(protocolHeader);
        } else if(protocol.equals(TCProtocol.TCP_PROTOCOL)) {
            return TCProtocol.getCriterionTypeByPropertyName(protocolHeader);
        } else if(protocol.equals(UDProtocol.UDP_PROTOCOL)) {
            return UDProtocol.getCriterionTypeByPropertyName(protocolHeader);
        } else if(protocol.equals(ICMProtocol.ICMP_PROTOCOL)) {
            return ICMProtocol.getCriterionTypeByPropertyName(protocolHeader);
        } else if(protocol.equals(ARProtocol.ARP_PROTOCOL)) {
            return ARProtocol.getCriterionTypeByPropertyName(protocolHeader);
        } 

        return null;
    }
}
