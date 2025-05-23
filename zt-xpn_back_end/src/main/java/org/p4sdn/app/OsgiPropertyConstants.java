/**
 * ZT-XPN: An end-to-end Zero-Trust Architecture for Next Generation 
 * Programmable Networks
 * 
 * Authors:  Charalampos Katsis  (ckatsis@purdue.edu)
 *           Elisa Bertino       (bertino@purdue.edu)
 * =================================================================
 */


package org.p4sdn.app;

public final class OsgiPropertyConstants {
    private OsgiPropertyConstants() {
    }

    static final String PACKET_OUT_ONLY = "packetOutOnly";
    static final boolean PACKET_OUT_ONLY_DEFAULT  = false;

    static final String  PACKET_OUT_OFPP_TABLE  = "packetOutOfppTable";
    static final boolean PACKET_OUT_OFPP_TABLE_DEFAULT = false;

    static final String FLOW_TIMEOUT = "flowTimeout";
    static final int FLOW_TIMEOUT_DEFAULT = 10;

    static final String FLOW_PRIORITY = "flowPriority";
    static final int FLOW_PRIORITY_DEFAULT = 10;

    static final String IPV6_FORWARDING = "ipv6Forwarding";
    static final boolean IPV6_FORWARDING_DEFAULT = false;

    static final String MATCH_DST_MAC_ONLY = "matchDstMacOnly";
    static final boolean MATCH_DST_MAC_ONLY_DEFAULT = false;

    static final String MATCH_VLAN_ID = "matchVlanId";
    static final boolean MATCH_VLAN_ID_DEFAULT = false;

    static final String MATCH_IPV4_ADDRESS = "matchIpv4Address";
    static final boolean MATCH_IPV4_ADDRESS_DEFAULT = false;

    static final String MATCH_IPV4_DSCP = "matchIpv4Dscp";
    static final boolean MATCH_IPV4_DSCP_DEFAULT = false;

    static final String MATCH_IPV6_ADDRESS = "matchIpv6Address";
    static final boolean MATCH_IPV6_ADDRESS_DEFAULT = false;

    static final String MATCH_IPV6_FLOW_LABEL = "matchIpv6FlowLabel";
    static final boolean MATCH_IPV6_FLOW_LABEL_DEFAULT = false;

    static final String MATCH_TCP_UDP_PORTS = "matchTcpUdpPorts";
    static final boolean MATCH_TCP_UDP_PORTS_DEFAULT = false;

    static final String MATCH_ICMP_FIELDS = "matchIcmpFields";
    static final boolean MATCH_ICMP_FIELDS_DEFAULT = false;

    static final String IGNORE_IPV4_MCAST_PACKETS = "ignoreIPv4Multicast";
    static final boolean IGNORE_IPV4_MCAST_PACKETS_DEFAULT = false;

    static final String RECORD_METRICS = "recordMetrics";
    static final boolean RECORD_METRICS_DEFAULT = false;

    static final String INHERIT_FLOW_TREATMENT = "inheritFlowTreatment";
    static final boolean INHERIT_FLOW_TREATMENT_DEFAULT = false;
}
