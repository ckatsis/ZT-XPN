package org.p4sdn.app.net;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.onlab.packet.TpPort;
import org.onosproject.net.flow.TrafficSelector.Builder;
import org.onosproject.net.flow.criteria.Criterion;
import org.p4sdn.app.pipeline.components.Header;
import org.p4sdn.app.pipeline.components.Variable;

public class TCProtocol extends Protocol {

    public static final String SPORT_HDR_VAR = "sourcePort";
    public static final int SPORT_HDR_VAR_SIZE = 16;
    public static final String DPORT_HDR_VAR = "destinationPort";
    public static final int DPORT_HDR_VAR_SIZE = 16;
    public static final String SNO_HDR_VAR = "sequenceNo";
    public static final int SNO_HDR_VAR_SIZE = 32;
    public static final String ACK_HDR_VAR = "ack";
    public static final int ACK_HDR_VAR_SIZE = 32;
    public static final String DOFF_HDR_VAR = "dataOffset";
    public static final int DOFF_HDR_VAR_SIZE = 4;
    public static final String RES_HDR_VAR = "res";
    public static final int RES_HDR_VAR_SIZE = 3;
    public static final String ECN_HDR_VAR = "ecn";
    public static final int ECN_HDR_VAR_SIZE = 3;
    public static final String CTRL_HDR_VAR = "ctrl";
    public static final int CTRL_HDR_VAR_SIZE = 6;
    public static final String WIN_HDR_VAR = "windowSize";
    public static final int WIN_HDR_VAR_SIZE = 16;
    public static final String CSUM_HDR_VAR = "checksum";
    public static final int CSUM_HDR_VAR_SIZE = 16;
    public static final String UPTR_HDR_VAR = "urgentPointer";
    public static final int UPTR_HDR_VAR_SIZE = 16;
    public static final String TCP_PROTOCOL = "TCP";    
    public static final byte TCP_IP_ID = 0x06;
    public static HashSet<Byte> optionSet = new HashSet<>();

    public static final String PARENT_TRANSITION_VALUE = "6";

    // Static map to hold the mapping between property names and Criterion.Type
    private static final Map<String, Criterion.Type> propertyToCriterionTypeMap = new HashMap<>();

    static {
        // Populate the map with the required mappings
        propertyToCriterionTypeMap.put(SPORT_HDR_VAR, Criterion.Type.TCP_SRC);
        propertyToCriterionTypeMap.put(DPORT_HDR_VAR, Criterion.Type.TCP_DST);
        // Add other mappings if needed
    }


    private int	ack = -1;
    private short checksum = -1;
    private byte dataOffset = -1;
    private int destinationPort = -1;
    private short flags = -1;
    private byte[] options = {-1};
    private int sequenceNo = -1;
    private int	sourcePort = -1;
    private short urgentPointer = -1;
    private short windowSize = -1;
    private HashMap<Byte, Byte[]> opToValMap = new HashMap<>();

    public TCProtocol() {
        super(TCP_PROTOCOL);
        allowed_parent_proto.add(IProtocolV4.IP_PROTOCOL);
    }

    public TCProtocol(int ack, short checksum, byte dataOffset, int destinationPort, short flags, byte[] options,
            int sequence, int sourcePort, short urgentPointer, short windowSize) 
    {
        super(TCP_PROTOCOL);
        this.ack = ack;
        this.checksum = checksum;
        this.dataOffset = dataOffset;
        this.destinationPort = destinationPort;
        this.flags = flags;
        this.options = options;
        parseOptions(this.options);
        this.sequenceNo = sequence;
        this.sourcePort = sourcePort;
        this.urgentPointer = urgentPointer;
        this.windowSize = windowSize;
        allowed_parent_proto.add(IProtocolV4.IP_PROTOCOL);
    }

      // Method to get Criterion.Type based on property name
    public static Criterion.Type getCriterionTypeByPropertyName(String propertyName) {
        return propertyToCriterionTypeMap.get(propertyName);
    }

    private void parseOptions(byte[] options) {        
        for(int i = 0; i < options.length;)
        {
            if(options[i] == 0 || options[i] == 1) {
                optionSet.add(options[i]); // add the option to the set
                Byte[] arr = null;
                if ((arr = opToValMap.get(options[i])) == null) 
                {
                    arr = new Byte[1];
                    arr[0] = 1;
                    opToValMap.put(options[i], arr);
                } else {
                    arr[0]++;
                }
                i++;
            }
            else {
                byte tcp_op = options[i];
                int len = options[i+1];
                int val_len = len - 2; // excludes the tcp_op and # of bytes
                Byte[] arr = new Byte[val_len];
                // System.arraycopy(options, i + 2, arr, 0, val_len);
                for (int j = 0; j < val_len; j++){
                    arr[j] = options[i + 2 + j];
                }
                optionSet.add(tcp_op);
                opToValMap.put(tcp_op, arr);
                i += len; 
            }
        }
    }

    @Override
    public String printHeaders() {
            return "TCProtocol [ack=" + ack + ", checksum=" + checksum + ", dataOffset=" + dataOffset + ", destinationPort="
                + destinationPort + ", flags=" + flags + ", options=" + Arrays.toString(options) + ", sequence="
                + sequenceNo + ", sourcePort=" + sourcePort + ", urgentPointer=" + urgentPointer + ", windowSize="
                + windowSize + ", associatedApp= " + getApplication() + "]";
    }

    // @Override
    // public String toString() {
    //     return  ack + ";" + checksum + ";" + dataOffset + ";"
    //             + destinationPort + ";" + flags + ";" + Arrays.toString(options) + ";"
    //             + sequence + ";" + sourcePort + ";" + urgentPointer + ";"
    //             + windowSize;
    // }  

    public static String printTcpHeaders()
    {
        String res = "ack;checksum;dataOffset;destinationPort;flags;";
        Byte [] optns = new Byte[optionSet.size()];
        optionSet.toArray(optns);

        byte[] to_bytes = new byte[optns.length];

        int j=0;
        for(Byte b: optns)
            to_bytes[j++] = b.byteValue();

        Arrays.sort(to_bytes);

        if(to_bytes.length > 0) {
            for (int i = 0; i <= to_bytes[to_bytes.length-1] ; i++)
                res = res + "Tcp_option_" + Integer.toString(i) + ";";
        }
        res = res + "sequence;sourcePort;urgentPointer;windowSize;associatedApp;";
        return res;
    }

    

    public static String printOptions(TCProtocol tcp_pckt) {
        
        Byte [] optns = new Byte[optionSet.size()];
        optionSet.toArray(optns);
        byte[] to_bytes = new byte[optns.length];
        int j=0;

        for(Byte b: optns)
            to_bytes[j++] = b.byteValue();

        Arrays.sort(to_bytes);

        String res = "";
        if(to_bytes.length > 0) 
        {
            HashMap<Byte, Byte[]> opToValMap = tcp_pckt.opToValMap;

            for (byte i = 0; i <= to_bytes[to_bytes.length-1]; i++)
            {
                Byte[] arr = null;
                if((arr = opToValMap.get(i)) == null) {
                    res += "null;";
                } else {
                    int k = 0;
                    byte[] bytes = new byte[arr.length];
                    
                    for(Byte b: arr)
                        bytes[k++] = b.byteValue();

                    res += Arrays.toString(bytes);
                    res = res.replace(",", "").replace("[", "").replace("]", "");
                    res += ";";
                }
            }
        }
        return res;
    }

    @Override
    public String toString() {
        return  ack + ";" + checksum + ";" + dataOffset + ";"
                + destinationPort + ";" + flags + ";" + printOptions(this) + 
                + sequenceNo + ";" + sourcePort + ";" + urgentPointer + ";"
                + windowSize + ";" + getApplication() + ";";
    }

    @Override
    public String getFeatures() {
        return printTcpHeaders();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ack;
        result = prime * result + checksum;
        result = prime * result + dataOffset;
        result = prime * result + destinationPort;
        result = prime * result + flags;
        result = prime * result + Arrays.hashCode(options);
        result = prime * result + sequenceNo;
        result = prime * result + sourcePort;
        result = prime * result + urgentPointer;
        result = prime * result + windowSize;
        result = prime * result + ((opToValMap == null) ? 0 : opToValMap.hashCode());
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
        TCProtocol other = (TCProtocol) obj;
        if (ack != other.ack)
            return false;
        if (checksum != other.checksum)
            return false;
        if (dataOffset != other.dataOffset)
            return false;
        if (destinationPort != other.destinationPort)
            return false;
        if (flags != other.flags)
            return false;
        if (!Arrays.equals(options, other.options))
            return false;
        if (sequenceNo != other.sequenceNo)
            return false;
        if (sourcePort != other.sourcePort)
            return false;
        if (urgentPointer != other.urgentPointer)
            return false;
        if (windowSize != other.windowSize)
            return false;
        if (opToValMap == null) {
            if (other.opToValMap != null)
                return false;
        } else if (!opToValMap.equals(other.opToValMap))
            return false;
        return true;
    }

    public void setDestinationPort(int destinationPort) {
        this.destinationPort = destinationPort;
    }

    public void setSourcePort(int sourcePort) {
        this.sourcePort = sourcePort;
    }

    @Override
    public Header generateP4Header(String name) {
        return generateDefaultTCPHeader(name);
    }  

    public static Header generateDefaultTCPHeader(String name) {
        Header tcpHeader = new Header(name);

        Variable src_port = new Variable(SPORT_HDR_VAR, SPORT_HDR_VAR_SIZE);
        tcpHeader.addVariable(src_port);
        Variable dst_port = new Variable(DPORT_HDR_VAR, DPORT_HDR_VAR_SIZE);
        tcpHeader.addVariable(dst_port);
        Variable seq_no = new Variable(SNO_HDR_VAR, SNO_HDR_VAR_SIZE);
        tcpHeader.addVariable(seq_no);
        Variable ack_no = new Variable(ACK_HDR_VAR, ACK_HDR_VAR_SIZE);
        tcpHeader.addVariable(ack_no);
        Variable data_offset = new Variable(DOFF_HDR_VAR, DOFF_HDR_VAR_SIZE);
        tcpHeader.addVariable(data_offset);
        Variable res = new Variable(RES_HDR_VAR, RES_HDR_VAR_SIZE);
        tcpHeader.addVariable(res);
        Variable ecn = new Variable(ECN_HDR_VAR, ECN_HDR_VAR_SIZE);
        tcpHeader.addVariable(ecn);
        Variable ctrl = new Variable(CTRL_HDR_VAR, CTRL_HDR_VAR_SIZE);
        tcpHeader.addVariable(ctrl);
        Variable window = new Variable(WIN_HDR_VAR, WIN_HDR_VAR_SIZE);
        tcpHeader.addVariable(window);
        Variable checksum = new Variable(CSUM_HDR_VAR, CSUM_HDR_VAR_SIZE);
        tcpHeader.addVariable(checksum);
        Variable urgent_ptr = new Variable(UPTR_HDR_VAR, UPTR_HDR_VAR_SIZE);
        tcpHeader.addVariable(urgent_ptr);
        
        return tcpHeader;
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
            selector.matchTcpSrc(TpPort.tpPort(sourcePort));
        }

        if (destinationPort != -1) {
            selector.matchTcpDst(TpPort.tpPort(destinationPort));
        }
    }

}
