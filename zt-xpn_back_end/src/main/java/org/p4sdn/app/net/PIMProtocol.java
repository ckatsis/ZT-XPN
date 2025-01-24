package org.p4sdn.app.net;

import org.onosproject.net.flow.TrafficSelector.Builder;
import org.p4sdn.app.pipeline.components.Header;

public class PIMProtocol extends Protocol{

    public static final String PIM_PROTOCOL = "PIM";
    public static final String PIM_FEATURES = "checksum;pimMsgType;reserved;version;";
    
    private short checksum = -1;
    private byte pimMsgType = -1;
    private byte reserved = -1;
    private byte version = -1;

    public PIMProtocol(short checksum, byte pimMsgType, byte reserved, byte version) {
        super(PIM_PROTOCOL);
        this.checksum = checksum;
        this.pimMsgType = pimMsgType;
        this.reserved = reserved;
        this.version = version;

        allowed_parent_proto.add(IProtocolV4.IP_PROTOCOL);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + checksum;
        result = prime * result + pimMsgType;
        result = prime * result + reserved;
        result = prime * result + version;
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
        PIMProtocol other = (PIMProtocol) obj;
        if (checksum != other.checksum)
            return false;
        if (pimMsgType != other.pimMsgType)
            return false;
        if (reserved != other.reserved)
            return false;
        if (version != other.version)
            return false;
        return true;
    }




    @Override
    public String toString() {
        return "PIMProtocol [checksum=" + checksum + ", pimMsgType=" + pimMsgType + ", reserved=" + reserved
                + ", version=" + version + "]";
    }


    @Override
    public String printHeaders() {
        return checksum + ";" + pimMsgType + ";" + reserved
                + ";" + version + ";";
    }

    @Override
    public String getFeatures() {
        return PIM_FEATURES;
    }




    @Override
    public Header generateP4Header(String name) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'generateP4Header'");
    }




    @Override
    public String getTransitionConditionSuffix() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getConditionSuffix'");
    }




    @Override
    public String getParentTransitionValue() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getParentTransitionValue'");
    }

    @Override
    public String getTransitionPropertyValue() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getTransitionPropertyValue'");
    }

    @Override
    public void generateMatchingSelector(Builder arg0) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getMatchingSelector'");
    }
}
