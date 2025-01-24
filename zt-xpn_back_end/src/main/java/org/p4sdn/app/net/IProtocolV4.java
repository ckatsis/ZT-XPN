package org.p4sdn.app.net;

import java.util.Arrays;

import org.onlab.packet.Ip4Prefix;
import org.onosproject.net.flow.TrafficSelector.Builder;
import org.onosproject.net.flow.criteria.Criterion;
import org.p4sdn.app.pipeline.components.Header;
import org.p4sdn.app.pipeline.components.Variable;

import java.util.HashMap;
import java.util.Map;

public class IProtocolV4 extends Protocol {

    public static final String VER_HDR_VAR = "version";
    public static final int VER_HDR_VAR_SIZE = 4;
    public static final String IHL_HDR_VAR = "ihl";
    public static final int IHL_HDR_VAR_SIZE = 4;
    public static final String DSCP_HDR_VAR = "dscp";
    public static final int DSCP_HDR_VAR_SIZE = 6;
    public static final String ECN_HDR_VAR = "ecn";
    public static final int ECN_HDR_VAR_SIZE = 2;
    public static final String TLEN_HDR_VAR = "totallen";
    public static final int TLEN_HDR_VAR_SIZE = 16;
    public static final String ID_HDR_VAR = "identification";
    public static final int ID_HDR_VAR_SIZE = 16;
    public static final String FLAGS_HDR_VAR = "flags";
    public static final int FLAGS_HDR_VAR_SIZE = 3;
    public static final String FRAGO_HDR_VAR = "fragoff";
    public static final int FRAGO_HDR_VAR_SIZE = 13;
    public static final String TTL_HDR_VAR = "ttl";
    public static final int TTL_HDR_VAR_SIZE = 8;
    public static final String PROTO_HDR_VAR = "proto";
    public static final int PROTO_HDR_VAR_SIZE = 8;
    public static final String CHKSUM_HDR_VAR = "hdr_checksum";
    public static final int CHKSUM_HDR_VAR_SIZE = 16;
    public static final String SADDR_HDR_VAR = "sourceAddress";
    public static final int SADDR_HDR_VAR_SIZE = 32;
    public static final String DADDR_HDR_VAR = "destinationAddress";
    public static final int DADDR_HDR_VAR_SIZE = 32;

    public static final String PARENT_TRANSITION_VALUE = "0x800";
    public static final String TRANSITION_CONDITION_SUFFIX = PROTO_HDR_VAR;

    public static final String IP_PROTOCOL = "IP";
    public static final String IP_FEATURES = "destinationAddress;chksum;diffserv;dscp;ecn;flags;" + 
                "fragoff;hdrlen;identification;options;proto;sourceAddress;totallen;ttl;" +
                "version;truncated;";

    // Static map to hold the mapping between property names and Criterion.Type
    private static final Map<String, Criterion.Type> propertyToCriterionTypeMap = new HashMap<>();

    static {
        // Populate the map with the required mappings
        propertyToCriterionTypeMap.put(DSCP_HDR_VAR, Criterion.Type.IP_DSCP);
        propertyToCriterionTypeMap.put(ECN_HDR_VAR, Criterion.Type.IP_ECN);
        propertyToCriterionTypeMap.put(PROTO_HDR_VAR, Criterion.Type.IP_PROTO);
        propertyToCriterionTypeMap.put(SADDR_HDR_VAR, Criterion.Type.IPV4_SRC);
        propertyToCriterionTypeMap.put(DADDR_HDR_VAR, Criterion.Type.IPV4_DST);
        // Add other mappings if needed
    }

    private int destinationAddress = -1;
    private short chksum = -1;  
    private byte diffserv = -1;
    private byte dscp = -1;
    private byte ecn = -1;
    private byte flags = -1;
    private short fragoff = -1;
    private byte hdrlen = -1;
    private short identification = -1;
    private byte[] options = {-1};
    private byte proto = -1;
    private int sourceAddress = -1;
    private short totallen = -1;
    private byte ttl = -1;
    private byte version = -1;
    private boolean truncated = false;

    public IProtocolV4() {
        super(IP_PROTOCOL);
        allowed_parent_proto.add(EthernetProtocol.ETHERNET_PROTOCOL); 
        allowed_parent_proto.add(PIMProtocol.PIM_PROTOCOL);
    }

    public IProtocolV4(int destinationAddress, short chksum, byte diffserv, byte dscp, byte ecn, byte flags,
            short fragoff, byte hdrlen, short identification, byte[] options, byte proto, int sourceAddress,
            short totallen, byte ttl, byte version, boolean truncated) {
        super(IP_PROTOCOL);
        this.destinationAddress = destinationAddress;
        this.chksum = chksum;
        this.diffserv = diffserv;
        this.dscp = dscp;
        this.ecn = ecn;
        this.flags = flags;
        this.fragoff = fragoff;
        this.hdrlen = hdrlen;
        this.identification = identification;
        this.options = options;
        this.proto = proto;
        this.sourceAddress = sourceAddress;
        this.totallen = totallen;
        this.ttl = ttl;
        this.version = version;
        this.truncated = truncated;

        allowed_parent_proto.add(EthernetProtocol.ETHERNET_PROTOCOL); 
        allowed_parent_proto.add(PIMProtocol.PIM_PROTOCOL);
    }

    // Method to get Criterion.Type based on property name
    public static Criterion.Type getCriterionTypeByPropertyName(String propertyName) {
        return propertyToCriterionTypeMap.get(propertyName);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + destinationAddress;
        result = prime * result + chksum;
        result = prime * result + diffserv;
        result = prime * result + dscp;
        result = prime * result + ecn;
        result = prime * result + flags;
        result = prime * result + fragoff;
        result = prime * result + hdrlen;
        result = prime * result + identification;
        result = prime * result + Arrays.hashCode(options);
        result = prime * result + proto;
        result = prime * result + sourceAddress;
        result = prime * result + totallen;
        result = prime * result + ttl;
        result = prime * result + version;
        result = prime * result + (truncated ? 1231 : 1237);
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
        IProtocolV4 other = (IProtocolV4) obj;
        if (destinationAddress != other.destinationAddress)
            return false;
        if (chksum != other.chksum)
            return false;
        if (diffserv != other.diffserv)
            return false;
        if (dscp != other.dscp)
            return false;
        if (ecn != other.ecn)
            return false;
        if (flags != other.flags)
            return false;
        if (fragoff != other.fragoff)
            return false;
        if (hdrlen != other.hdrlen)
            return false;
        if (identification != other.identification)
            return false;
        if (!Arrays.equals(options, other.options))
            return false;
        if (proto != other.proto)
            return false;
        if (sourceAddress != other.sourceAddress)
            return false;
        if (totallen != other.totallen)
            return false;
        if (ttl != other.ttl)
            return false;
        if (version != other.version)
            return false;
        if (truncated != other.truncated)
            return false;
        return true;
    }

    @Override
    public String printHeaders() {
        return "IProtocolV4 [destinationAddress=" + destinationAddress + ", chksum=" + chksum + ", diffserv=" + diffserv
                + ", dscp=" + dscp + ", ecn=" + ecn + ", flags=" + flags + ", fragoff=" + fragoff + ", hdrlen=" + hdrlen
                + ", identification=" + identification + ", options=" + Arrays.toString(options) + ", proto=" + proto
                + ", sourceAddress=" + sourceAddress + ", totallen=" + totallen + ", ttl=" + ttl + ", version="
                + version + ", truncated=" + truncated + "]";
    }
    

    @Override
    public String toString() {
        return destinationAddress + ";" + chksum + ";" + diffserv
                + ";" + dscp + ";" + ecn + ";" + flags + ";" + fragoff + ";" + hdrlen
                + ";" + identification + ";" + Arrays.toString(options) + ";" + proto
                + ";" + sourceAddress + ";" + totallen + ";" + ttl + ";"
                + version + ";" + truncated + ";";
    }


    @Override
    public String getFeatures() {
        return IP_FEATURES;
    }

    public void setDestinationAddress(int destinationAddress) {
        this.destinationAddress = destinationAddress;
    }

    public void setSourceAddress(int sourceAddress) {
        this.sourceAddress = sourceAddress;
    }

    public byte getProto() {
        return proto;
    }

    public void setProto(byte proto) {
        this.proto = proto;
    }

    @Override
    public Header generateP4Header(String name) {
        return generateDefaultIPv4Header(name);
    }

    public static Header generateDefaultIPv4Header(String name) {
        Header ipHeader = new Header(name);

        Variable version = new Variable(VER_HDR_VAR, VER_HDR_VAR_SIZE);
        ipHeader.addVariable(version);
        Variable ihl = new Variable(IHL_HDR_VAR, IHL_HDR_VAR_SIZE);
        ipHeader.addVariable(ihl);
        Variable dscp = new Variable(DSCP_HDR_VAR, DSCP_HDR_VAR_SIZE);
        ipHeader.addVariable(dscp);
        Variable ecn = new Variable(ECN_HDR_VAR, ECN_HDR_VAR_SIZE);
        ipHeader.addVariable(ecn);
        Variable total_len = new Variable(TLEN_HDR_VAR, TLEN_HDR_VAR_SIZE);
        ipHeader.addVariable(total_len);
        Variable identification = new Variable(ID_HDR_VAR, ID_HDR_VAR_SIZE);
        ipHeader.addVariable(identification);
        Variable flags = new Variable(FLAGS_HDR_VAR, FLAGS_HDR_VAR_SIZE);
        ipHeader.addVariable(flags);
        Variable frag_offset = new Variable(FRAGO_HDR_VAR, FRAGO_HDR_VAR_SIZE);
        ipHeader.addVariable(frag_offset);
        Variable ttl = new Variable(TTL_HDR_VAR, TTL_HDR_VAR_SIZE);
        ipHeader.addVariable(ttl);
        Variable protocol = new Variable(PROTO_HDR_VAR, PROTO_HDR_VAR_SIZE);
        ipHeader.addVariable(protocol);
        Variable hdr_checksum = new Variable(CHKSUM_HDR_VAR, CHKSUM_HDR_VAR_SIZE);
        ipHeader.addVariable(hdr_checksum);
        Variable src_addr = new Variable(SADDR_HDR_VAR, SADDR_HDR_VAR_SIZE);
        ipHeader.addVariable(src_addr);
        Variable dst_addr = new Variable(DADDR_HDR_VAR, DADDR_HDR_VAR_SIZE);
        ipHeader.addVariable(dst_addr);
        
        return ipHeader;
    }

    @Override
    public String getTransitionConditionSuffix() {
        return TRANSITION_CONDITION_SUFFIX;
    }

    @Override
    public String getParentTransitionValue() {
        return PARENT_TRANSITION_VALUE;
    }

    @Override
    public String getTransitionPropertyValue() {
        return String.valueOf(proto);
    }

    @Override
    public void generateMatchingSelector(Builder selector) {
        if (selector == null) {
            return;
        }

        if (dscp != -1) {
            selector.matchIPDscp(dscp);
        }

        if (ecn != -1) {
            selector.matchIPEcn(ecn);
        }

        if (proto != -1) {
            selector.matchIPProtocol(proto);
        }

        if (sourceAddress != -1) {
            selector.matchIPSrc(Ip4Prefix.valueOf(sourceAddress, Ip4Prefix.MAX_MASK_LENGTH));
        }

        if (destinationAddress != -1) {
            selector.matchIPDst(Ip4Prefix.valueOf(destinationAddress, Ip4Prefix.MAX_MASK_LENGTH));         
        }
    }
}