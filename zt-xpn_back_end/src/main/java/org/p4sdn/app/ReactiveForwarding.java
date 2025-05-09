/**
 * ZT-XPN: An end-to-end Zero-Trust Architecture for Next Generation 
 * Programmable Networks
 * 
 * Authors:  Charalampos Katsis  (ckatsis@purdue.edu)
 *           Elisa Bertino       (bertino@purdue.edu)
 * =================================================================
 */


package org.p4sdn.app;

import static java.util.concurrent.Executors.newSingleThreadExecutor;
import static org.onlab.util.Tools.groupedThreads;
import static org.p4sdn.app.OsgiPropertyConstants.FLOW_PRIORITY;
import static org.p4sdn.app.OsgiPropertyConstants.FLOW_PRIORITY_DEFAULT;
import static org.p4sdn.app.OsgiPropertyConstants.FLOW_TIMEOUT;
import static org.p4sdn.app.OsgiPropertyConstants.FLOW_TIMEOUT_DEFAULT;
import static org.p4sdn.app.OsgiPropertyConstants.IGNORE_IPV4_MCAST_PACKETS;
import static org.p4sdn.app.OsgiPropertyConstants.IGNORE_IPV4_MCAST_PACKETS_DEFAULT;
import static org.p4sdn.app.OsgiPropertyConstants.INHERIT_FLOW_TREATMENT;
import static org.p4sdn.app.OsgiPropertyConstants.INHERIT_FLOW_TREATMENT_DEFAULT;
import static org.p4sdn.app.OsgiPropertyConstants.IPV6_FORWARDING;
import static org.p4sdn.app.OsgiPropertyConstants.IPV6_FORWARDING_DEFAULT;
import static org.p4sdn.app.OsgiPropertyConstants.MATCH_DST_MAC_ONLY;
import static org.p4sdn.app.OsgiPropertyConstants.MATCH_DST_MAC_ONLY_DEFAULT;
import static org.p4sdn.app.OsgiPropertyConstants.MATCH_ICMP_FIELDS;
import static org.p4sdn.app.OsgiPropertyConstants.MATCH_ICMP_FIELDS_DEFAULT;
import static org.p4sdn.app.OsgiPropertyConstants.MATCH_IPV4_ADDRESS;
import static org.p4sdn.app.OsgiPropertyConstants.MATCH_IPV4_ADDRESS_DEFAULT;
import static org.p4sdn.app.OsgiPropertyConstants.MATCH_IPV4_DSCP;
import static org.p4sdn.app.OsgiPropertyConstants.MATCH_IPV4_DSCP_DEFAULT;
import static org.p4sdn.app.OsgiPropertyConstants.MATCH_IPV6_ADDRESS;
import static org.p4sdn.app.OsgiPropertyConstants.MATCH_IPV6_ADDRESS_DEFAULT;
import static org.p4sdn.app.OsgiPropertyConstants.MATCH_IPV6_FLOW_LABEL;
import static org.p4sdn.app.OsgiPropertyConstants.MATCH_IPV6_FLOW_LABEL_DEFAULT;
import static org.p4sdn.app.OsgiPropertyConstants.MATCH_TCP_UDP_PORTS;
import static org.p4sdn.app.OsgiPropertyConstants.MATCH_TCP_UDP_PORTS_DEFAULT;
import static org.p4sdn.app.OsgiPropertyConstants.MATCH_VLAN_ID;
import static org.p4sdn.app.OsgiPropertyConstants.MATCH_VLAN_ID_DEFAULT;
import static org.p4sdn.app.OsgiPropertyConstants.PACKET_OUT_OFPP_TABLE;
import static org.p4sdn.app.OsgiPropertyConstants.PACKET_OUT_OFPP_TABLE_DEFAULT;
import static org.p4sdn.app.OsgiPropertyConstants.PACKET_OUT_ONLY;
import static org.p4sdn.app.OsgiPropertyConstants.PACKET_OUT_ONLY_DEFAULT;
import static org.p4sdn.app.OsgiPropertyConstants.RECORD_METRICS;
import static org.p4sdn.app.OsgiPropertyConstants.RECORD_METRICS_DEFAULT;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.Collections;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicLong;

import org.onlab.packet.Ethernet;
import org.onlab.packet.ICMP;
import org.onlab.packet.ICMP6;
import org.onlab.packet.IPv4;
import org.onlab.packet.IPv6;
import org.onlab.packet.Ip4Prefix;
import org.onlab.packet.Ip6Prefix;
import org.onlab.packet.MacAddress;
import org.onlab.packet.TCP;
import org.onlab.packet.TpPort;
import org.onlab.packet.UDP;
import org.onlab.packet.VlanId;
import org.onlab.util.Tools;
import org.onosproject.cfg.ComponentConfigService;
import org.onosproject.core.ApplicationId;
import org.onosproject.core.CoreService;
import org.onosproject.event.Event;
import org.onosproject.net.ConnectPoint;
import org.onosproject.net.DeviceId;
import org.onosproject.net.Host;
import org.onosproject.net.HostId;
import org.onosproject.net.Link;
import org.onosproject.net.Path;
import org.onosproject.net.PortNumber;
import org.onosproject.net.flow.DefaultTrafficSelector;
import org.onosproject.net.flow.DefaultTrafficTreatment;
import org.onosproject.net.flow.FlowEntry;
import org.onosproject.net.flow.FlowRule;
import org.onosproject.net.flow.FlowRuleService;
import org.onosproject.net.flow.TrafficSelector;
import org.onosproject.net.flow.TrafficTreatment;
import org.onosproject.net.flow.criteria.Criterion;
import org.onosproject.net.flow.criteria.EthCriterion;
import org.onosproject.net.flow.instructions.Instruction;
import org.onosproject.net.flow.instructions.Instructions;
import org.onosproject.net.flowobjective.DefaultForwardingObjective;
import org.onosproject.net.flowobjective.FlowObjectiveService;
import org.onosproject.net.flowobjective.ForwardingObjective;
import org.onosproject.net.host.HostEvent;
import org.onosproject.net.host.HostListener;
import org.onosproject.net.host.HostService;
import org.onosproject.net.link.LinkEvent;
import org.onosproject.net.packet.InboundPacket;
import org.onosproject.net.packet.PacketContext;
import org.onosproject.net.packet.PacketPriority;
import org.onosproject.net.packet.PacketProcessor;
import org.onosproject.net.packet.PacketService;
import org.onosproject.net.topology.TopologyEvent;
import org.onosproject.net.topology.TopologyListener;
import org.onosproject.net.topology.TopologyService;
import org.onosproject.store.service.StorageService;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.p4sdn.app.exceptions.ProtocolEncapsulationException;
import org.p4sdn.app.flow.FlowTrafficTreatment;
import org.p4sdn.app.net.Protocol;
import org.p4sdn.app.pipeline.BMV2Pipeline;
import org.p4sdn.app.policyEngine.PolicyEngine;
import org.slf4j.Logger;

import com.google.common.collect.ImmutableSet;

/**
 * Sample reactive forwarding application.
 */
@Component(
    immediate = true,
    service = ReactiveForwarding.class,
    property = {
        PACKET_OUT_ONLY + ":Boolean=" + PACKET_OUT_ONLY_DEFAULT,
        PACKET_OUT_OFPP_TABLE + ":Boolean=" + PACKET_OUT_OFPP_TABLE_DEFAULT,
        FLOW_TIMEOUT + ":Integer=" + FLOW_TIMEOUT_DEFAULT,
        FLOW_PRIORITY  + ":Integer=" + FLOW_PRIORITY_DEFAULT,
        IPV6_FORWARDING + ":Boolean=" + IPV6_FORWARDING_DEFAULT,
        MATCH_DST_MAC_ONLY + ":Boolean=" + MATCH_DST_MAC_ONLY_DEFAULT,
        MATCH_VLAN_ID + ":Boolean=" + MATCH_VLAN_ID_DEFAULT,
        MATCH_IPV4_ADDRESS + ":Boolean=" + MATCH_IPV4_ADDRESS_DEFAULT,
        MATCH_IPV4_DSCP + ":Boolean=" + MATCH_IPV4_DSCP_DEFAULT,
        MATCH_IPV6_ADDRESS + ":Boolean=" + MATCH_IPV6_ADDRESS_DEFAULT,
        MATCH_IPV6_FLOW_LABEL + ":Boolean=" + MATCH_IPV6_FLOW_LABEL_DEFAULT,
        MATCH_TCP_UDP_PORTS + ":Boolean=" + MATCH_TCP_UDP_PORTS_DEFAULT,
        MATCH_ICMP_FIELDS + ":Boolean=" + MATCH_ICMP_FIELDS_DEFAULT,
        IGNORE_IPV4_MCAST_PACKETS + ":Boolean=" + IGNORE_IPV4_MCAST_PACKETS_DEFAULT,
        RECORD_METRICS + ":Boolean=" + RECORD_METRICS_DEFAULT,
        INHERIT_FLOW_TREATMENT + ":Boolean=" + INHERIT_FLOW_TREATMENT_DEFAULT
    }
)
public class ReactiveForwarding {

    private final Logger log = getLogger(getClass());

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected TopologyService topologyService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected PacketService packetService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected HostService hostService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected FlowRuleService flowRuleService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected FlowObjectiveService flowObjectiveService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected CoreService coreService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected ComponentConfigService cfgService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected StorageService storageService;

    private ReactivePacketProcessor processor = new ReactivePacketProcessor();

    // private  EventuallyConsistentMap<MacAddress, ReactiveForwardMetrics> metrics;

    private ApplicationId appId;

    /** Enable packet-out only forwarding; default is false. */
    private boolean packetOutOnly = PACKET_OUT_ONLY_DEFAULT;

    /** Enable first packet forwarding using OFPP_TABLE port instead of PacketOut with actual port; default is false. */
    private boolean packetOutOfppTable = PACKET_OUT_OFPP_TABLE_DEFAULT;

    /** Configure Flow Timeout for installed flow rules; default is 10 sec. */
    private int flowTimeout = FLOW_TIMEOUT_DEFAULT;

    /** Configure Flow Priority for installed flow rules; default is 10. */
    private int flowPriority = FLOW_PRIORITY_DEFAULT;

    /** Enable IPv6 forwarding; default is false. */
    private boolean ipv6Forwarding = IPV6_FORWARDING_DEFAULT;

    /** Enable matching Dst Mac Only; default is false. */
    private boolean matchDstMacOnly = MATCH_DST_MAC_ONLY_DEFAULT;

    /** Enable matching Vlan ID; default is false. */
    private boolean matchVlanId = MATCH_VLAN_ID_DEFAULT;

    /** Enable matching IPv4 Addresses; default is false. */
    private boolean matchIpv4Address = MATCH_IPV4_ADDRESS_DEFAULT;

    /** Enable matching IPv4 DSCP and ECN; default is false. */
    private boolean matchIpv4Dscp = MATCH_IPV4_DSCP_DEFAULT;

    /** Enable matching IPv6 Addresses; default is false. */
    private boolean matchIpv6Address = MATCH_IPV6_ADDRESS_DEFAULT;

    /** Enable matching IPv6 FlowLabel; default is false. */
    private boolean matchIpv6FlowLabel = MATCH_IPV6_FLOW_LABEL_DEFAULT;

    /** Enable matching TCP/UDP ports; default is false. */
    private boolean matchTcpUdpPorts = MATCH_TCP_UDP_PORTS_DEFAULT;

    /** Enable matching ICMPv4 and ICMPv6 fields; default is false. */
    private boolean matchIcmpFields = MATCH_ICMP_FIELDS_DEFAULT;

    /** Ignore (do not forward) IPv4 multicast packets; default is false. */
    private boolean ignoreIPv4Multicast = IGNORE_IPV4_MCAST_PACKETS_DEFAULT;

    /** Enable record metrics for reactive forwarding. */
    private boolean recordMetrics = RECORD_METRICS_DEFAULT;

    /** Enable use of builder from packet context to define flow treatment; default is false. */
    private boolean inheritFlowTreatment = INHERIT_FLOW_TREATMENT_DEFAULT;

    private final TopologyListener topologyListener = new InternalTopologyListener();

    private ExecutorService blackHoleExecutor;

    private final Set<DeviceId> edgeSwitches = Collections.newSetFromMap(new ConcurrentHashMap<>());
    private final HostListener hostListener = new InternalHostListener(); // When a host connects
    private final String RECIRCULATION_PORT = BMV2Pipeline.BMV2_RECIRCULATION_PORT;

    // int packetInInvocations = 0;
    private final AtomicLong packetInInvocations = new AtomicLong(0);
    // long totalPacketInExecTime = 0;
    private final AtomicLong totalPacketInExecTime = new AtomicLong(0);
    // long totalPolicySearchExecTime = 0;
    private final AtomicLong totalPolicySearchExecTime = new AtomicLong(0);
    // int policyInvocations = 0;
    private final AtomicLong policyInvocations = new AtomicLong(0);


    @Activate
    public void activate(ComponentContext context) {

        blackHoleExecutor = newSingleThreadExecutor(groupedThreads("onos/app/fwd",
                                                                   "black-hole-fixer",
                                                                   log));

        cfgService.registerProperties(getClass());
        appId = coreService.registerApplication("org.onosproject.fwd");

        packetService.addProcessor(processor, PacketProcessor.director(2));
        hostService.addListener(hostListener);
        topologyService.addListener(topologyListener);
        readComponentConfiguration(context);
        requestIntercepts();

        log.info("Started", appId.id());
    }

    @Deactivate
    public void deactivate() {
        cfgService.unregisterProperties(getClass(), false);
        withdrawIntercepts();
        flowRuleService.removeFlowRulesById(appId);
        packetService.removeProcessor(processor);
        topologyService.removeListener(topologyListener);
        blackHoleExecutor.shutdown();
        blackHoleExecutor = null;
        processor = null;

        log.info("Packet In Invocations = " + packetInInvocations.get());
        log.info("Total Packet In Exec Time = " + totalPacketInExecTime.get() + " nsec");
        // double avgPacketExecTime = (double) totalPacketInExecTime / packetInInvocations;
        double avgPacketExecTime = (double) totalPacketInExecTime.get() / packetInInvocations.get();
        log.info("Avg Packet In Exec Time = " + avgPacketExecTime + " nsec");
        log.info("Policy Invocations = " + policyInvocations.get());
        log.info("Total Policy Search Time = " + totalPolicySearchExecTime.get() + " nsec");
        // double avgPolicySearchTime = (double) totalPolicySearchExecTime / policyInvocations;
        double avgPolicySearchTime = (double) totalPolicySearchExecTime.get() / policyInvocations.get();
        log.info("Avg Policy Search Time = " + avgPolicySearchTime + "nsec");

        log.info("Stopped");
    }

    @Modified
    public void modified(ComponentContext context) {
        readComponentConfiguration(context);
        requestIntercepts();
    }

    /**
     * Request packet in via packet service.
     */
    private void requestIntercepts() {
        TrafficSelector.Builder selector = DefaultTrafficSelector.builder();
        selector.matchEthType(Ethernet.TYPE_IPV4);
        packetService.requestPackets(selector.build(), PacketPriority.REACTIVE, appId);

        selector.matchEthType(Ethernet.TYPE_IPV6);
        if (ipv6Forwarding) {
            packetService.requestPackets(selector.build(), PacketPriority.REACTIVE, appId);
        } else {
            packetService.cancelPackets(selector.build(), PacketPriority.REACTIVE, appId);
        }
    }

    /**
     * Cancel request for packet in via packet service.
     */
    private void withdrawIntercepts() {
        TrafficSelector.Builder selector = DefaultTrafficSelector.builder();
        selector.matchEthType(Ethernet.TYPE_IPV4);
        packetService.cancelPackets(selector.build(), PacketPriority.REACTIVE, appId);
        selector.matchEthType(Ethernet.TYPE_IPV6);
        packetService.cancelPackets(selector.build(), PacketPriority.REACTIVE, appId);
    }

    /**
     * Extracts properties from the component configuration context.
     *
     * @param context the component context
     */
    private void readComponentConfiguration(ComponentContext context) {
        Dictionary<?, ?> properties = context.getProperties();

        Boolean packetOutOnlyEnabled =
                Tools.isPropertyEnabled(properties, PACKET_OUT_ONLY);
        if (packetOutOnlyEnabled == null) {
            log.info("Packet-out is not configured, " +
                     "using current value of {}", packetOutOnly);
        } else {
            packetOutOnly = packetOutOnlyEnabled;
            log.info("Configured. Packet-out only forwarding is {}",
                    packetOutOnly ? "enabled" : "disabled");
        }

        Boolean packetOutOfppTableEnabled =
                Tools.isPropertyEnabled(properties, PACKET_OUT_OFPP_TABLE);
        if (packetOutOfppTableEnabled == null) {
            log.info("OFPP_TABLE port is not configured, " +
                     "using current value of {}", packetOutOfppTable);
        } else {
            packetOutOfppTable = packetOutOfppTableEnabled;
            log.info("Configured. Forwarding using OFPP_TABLE port is {}",
                    packetOutOfppTable ? "enabled" : "disabled");
        }

        Boolean ipv6ForwardingEnabled =
                Tools.isPropertyEnabled(properties, IPV6_FORWARDING);
        if (ipv6ForwardingEnabled == null) {
            log.info("IPv6 forwarding is not configured, " +
                     "using current value of {}", ipv6Forwarding);
        } else {
            ipv6Forwarding = ipv6ForwardingEnabled;
            log.info("Configured. IPv6 forwarding is {}",
                    ipv6Forwarding ? "enabled" : "disabled");
        }

        Boolean matchDstMacOnlyEnabled =
                Tools.isPropertyEnabled(properties, MATCH_DST_MAC_ONLY);
        if (matchDstMacOnlyEnabled == null) {
            log.info("Match Dst MAC is not configured, " +
                     "using current value of {}", matchDstMacOnly);
        } else {
            matchDstMacOnly = matchDstMacOnlyEnabled;
            log.info("Configured. Match Dst MAC Only is {}",
                    matchDstMacOnly ? "enabled" : "disabled");
        }

        Boolean matchVlanIdEnabled =
                Tools.isPropertyEnabled(properties, MATCH_VLAN_ID);
        if (matchVlanIdEnabled == null) {
            log.info("Matching Vlan ID is not configured, " +
                     "using current value of {}", matchVlanId);
        } else {
            matchVlanId = matchVlanIdEnabled;
            log.info("Configured. Matching Vlan ID is {}",
                    matchVlanId ? "enabled" : "disabled");
        }

        Boolean matchIpv4AddressEnabled =
                Tools.isPropertyEnabled(properties, MATCH_IPV4_ADDRESS);
        if (matchIpv4AddressEnabled == null) {
            log.info("Matching IPv4 Address is not configured, " +
                     "using current value of {}", matchIpv4Address);
        } else {
            matchIpv4Address = matchIpv4AddressEnabled;
            log.info("Configured. Matching IPv4 Addresses is {}",
                    matchIpv4Address ? "enabled" : "disabled");
        }

        Boolean matchIpv4DscpEnabled =
                Tools.isPropertyEnabled(properties, MATCH_IPV4_DSCP);
        if (matchIpv4DscpEnabled == null) {
            log.info("Matching IPv4 DSCP and ECN is not configured, " +
                     "using current value of {}", matchIpv4Dscp);
        } else {
            matchIpv4Dscp = matchIpv4DscpEnabled;
            log.info("Configured. Matching IPv4 DSCP and ECN is {}",
                    matchIpv4Dscp ? "enabled" : "disabled");
        }

        Boolean matchIpv6AddressEnabled =
                Tools.isPropertyEnabled(properties, MATCH_IPV6_ADDRESS);
        if (matchIpv6AddressEnabled == null) {
            log.info("Matching IPv6 Address is not configured, " +
                     "using current value of {}", matchIpv6Address);
        } else {
            matchIpv6Address = matchIpv6AddressEnabled;
            log.info("Configured. Matching IPv6 Addresses is {}",
                    matchIpv6Address ? "enabled" : "disabled");
        }

        Boolean matchIpv6FlowLabelEnabled =
                Tools.isPropertyEnabled(properties, MATCH_IPV6_FLOW_LABEL);
        if (matchIpv6FlowLabelEnabled == null) {
            log.info("Matching IPv6 FlowLabel is not configured, " +
                     "using current value of {}", matchIpv6FlowLabel);
        } else {
            matchIpv6FlowLabel = matchIpv6FlowLabelEnabled;
            log.info("Configured. Matching IPv6 FlowLabel is {}",
                    matchIpv6FlowLabel ? "enabled" : "disabled");
        }

        Boolean matchTcpUdpPortsEnabled =
                Tools.isPropertyEnabled(properties, MATCH_TCP_UDP_PORTS);
        if (matchTcpUdpPortsEnabled == null) {
            log.info("Matching TCP/UDP fields is not configured, " +
                     "using current value of {}", matchTcpUdpPorts);
        } else {
            matchTcpUdpPorts = matchTcpUdpPortsEnabled;
            log.info("Configured. Matching TCP/UDP fields is {}",
                    matchTcpUdpPorts ? "enabled" : "disabled");
        }

        Boolean matchIcmpFieldsEnabled =
                Tools.isPropertyEnabled(properties, MATCH_ICMP_FIELDS);
        if (matchIcmpFieldsEnabled == null) {
            log.info("Matching ICMP (v4 and v6) fields is not configured, " +
                     "using current value of {}", matchIcmpFields);
        } else {
            matchIcmpFields = matchIcmpFieldsEnabled;
            log.info("Configured. Matching ICMP (v4 and v6) fields is {}",
                    matchIcmpFields ? "enabled" : "disabled");
        }

        Boolean ignoreIpv4McastPacketsEnabled =
                Tools.isPropertyEnabled(properties, IGNORE_IPV4_MCAST_PACKETS);
        if (ignoreIpv4McastPacketsEnabled == null) {
            log.info("Ignore IPv4 multi-cast packet is not configured, " +
                     "using current value of {}", ignoreIPv4Multicast);
        } else {
            ignoreIPv4Multicast = ignoreIpv4McastPacketsEnabled;
            log.info("Configured. Ignore IPv4 multicast packets is {}",
                    ignoreIPv4Multicast ? "enabled" : "disabled");
        }
        Boolean recordMetricsEnabled =
                Tools.isPropertyEnabled(properties, RECORD_METRICS);
        if (recordMetricsEnabled == null) {
            log.info("IConfigured. Ignore record metrics  is {} ," +
                    "using current value of {}", recordMetrics);
        } else {
            recordMetrics = recordMetricsEnabled;
            log.info("Configured. record metrics  is {}",
                    recordMetrics ? "enabled" : "disabled");
        }

        flowTimeout = Tools.getIntegerProperty(properties, FLOW_TIMEOUT, FLOW_TIMEOUT_DEFAULT);
        log.info("Configured. Flow Timeout is configured to {} seconds", flowTimeout);

        flowPriority = Tools.getIntegerProperty(properties, FLOW_PRIORITY, FLOW_PRIORITY_DEFAULT);
        log.info("Configured. Flow Priority is configured to {}", flowPriority);

        Boolean inheritFlowTreatmentEnabled =
                Tools.isPropertyEnabled(properties, INHERIT_FLOW_TREATMENT);
        if (inheritFlowTreatmentEnabled == null) {
            log.info("Inherit flow treatment is not configured, " +
                             "using current value of {}", inheritFlowTreatment);
        } else {
            inheritFlowTreatment = inheritFlowTreatmentEnabled;
            log.info("Configured. Inherit flow treatment is {}",
                     inheritFlowTreatment ? "enabled" : "disabled");
        }
    }

     /**
     * Packet processor responsible for forwarding packets along their paths.
     */
    private class ReactivePacketProcessor implements PacketProcessor {

        @Override
        public void process(PacketContext context) {
            // Stop processing if the packet has been handled, since we
            // can't do any more to it.

            long packetInStartTime = System.nanoTime();

            if (context.isHandled()) {
                return;
            }

            InboundPacket pkt = context.inPacket();
            Ethernet ethPkt = pkt.parsed();

            if (ethPkt == null) {
                return;
            }

            // MacAddress macAddress = ethPkt.getSourceMAC();
            // ReactiveForwardMetrics macMetrics = null;
            // macMetrics = createCounter(macAddress);
            // inPacket(macMetrics);

            // Bail if this is deemed to be a control packet.
            if (isControlPacket(ethPkt)) {
                // droppedPacket(macMetrics);
                return;
            }

            // Skip IPv6 multicast packet when IPv6 forward is disabled.
            if (!ipv6Forwarding && isIpv6Multicast(ethPkt)) {
                // droppedPacket(macMetrics);
                return;
            }

            HostId id = HostId.hostId(ethPkt.getDestinationMAC(), VlanId.vlanId(ethPkt.getVlanID()));

            // Do not process LLDP MAC address in any way.
            if (id.mac().isLldp()) {
                // droppedPacket(macMetrics);
                return;
            }

            // Do not process IPv4 multicast packets, let mfwd handle them
            if (ignoreIPv4Multicast && ethPkt.getEtherType() == Ethernet.TYPE_IPV4) {
                if (id.mac().isMulticast()) {
                    return;
                }
            }

            // Do we know who this is for? If not, flood and bail.
            Host dst = hostService.getHost(id);
            if (dst == null) {
                flood(context);
                return;
            }

            DeviceId pcktInSwitch = pkt.receivedFrom().deviceId();
            Protocol parsedPacket = null;
            Protocol policy = null;
            boolean simpleFwd = false;

            log.info("Parsing Packet-In request at " + pcktInSwitch.toString());


            if (edgeSwitches.contains(pcktInSwitch)) { // Received from an edge switch
                simpleFwd = false;

                try {
                    parsedPacket = Protocol.processEthernetPacket(ethPkt);
                } catch (ProtocolEncapsulationException e) {
                    log.error("Parsing failed: ", e);
                    return;
                }

                // Query the processed packet
                long policySearchStartTime = System.nanoTime();

                if ((policy = PolicyEngine.matchPacket(parsedPacket)) == null) {
                    log.error("Packet not matching policy -- Dropping...");
                    context.block();
                    return;
                }
                long policySearchEndTime = System.nanoTime();
                long policySearchExecutionTime = (policySearchEndTime - policySearchStartTime); // / 1000000; // Convert to milliseconds
                // totalPolicySearchExecTime += policySearchExecutionTime;
                totalPolicySearchExecTime.addAndGet(policySearchExecutionTime);
                // policyInvocations++;
                policyInvocations.incrementAndGet();
            } else {
                simpleFwd = true;
            }


            // Are we on an edge switch that our destination is on? If so,
            // simply forward out to the destination and bail.
            if (pkt.receivedFrom().deviceId().equals(dst.location().deviceId())) {
                if (!context.inPacket().receivedFrom().port().equals(dst.location().port())) {
                    if (simpleFwd) {
                        installRule(context, dst.location().port());
                    } else  {
                        installRule(context, policy, pcktInSwitch, dst.location().port());
                    }
                }
                
                // packetInInvocations++;
                packetInInvocations.incrementAndGet();
                long packetInEndTime = System.nanoTime();
                long packetInExecutionTime = (packetInEndTime - packetInStartTime); // / 1000000; // Convert to milliseconds
                // totalPacketInExecTime += packetInExecutionTime;
                totalPacketInExecTime.addAndGet(packetInExecutionTime);
                return;
            }

            // Otherwise, get a set of paths that lead from here to the
            // destination edge switch.
            Set<Path> paths =
                    topologyService.getPaths(topologyService.currentTopology(),
                                             pkt.receivedFrom().deviceId(),
                                             dst.location().deviceId());
            if (paths.isEmpty()) {
                // If there are no paths, flood and bail.
                flood(context);
                return;
            }

            // Otherwise, pick a path that does not lead back to where we
            // came from; if no such path, flood and bail.
            Path path = pickForwardPathIfPossible(paths, pkt.receivedFrom().port());
            if (path == null) {
                log.warn("Don't know where to go from here {} for {} -> {}",
                         pkt.receivedFrom(), ethPkt.getSourceMAC(), ethPkt.getDestinationMAC());
                flood(context);
                return;
            }

            if (simpleFwd) {
                // Forward and be done with it.
                // installRule(context, dst.location().port());
                installRule(context, path.src().port());
            } else {
                installRule(context, policy, pcktInSwitch, path.src().port());
            }

            // packetInInvocations++;
            packetInInvocations.incrementAndGet();
            long packetInEndTime = System.nanoTime();
            long packetInExecutionTime = (packetInEndTime - packetInStartTime); // / 1000000; // Convert to milliseconds
            // totalPacketInExecTime += packetInExecutionTime;
            totalPacketInExecTime.addAndGet(packetInExecutionTime);
        }

    }

    // Indicates whether this is a control packet, e.g. LLDP, BDDP
    private boolean isControlPacket(Ethernet eth) {
        short type = eth.getEtherType();
        return type == Ethernet.TYPE_LLDP || type == Ethernet.TYPE_BSN;
    }

    // Indicated whether this is an IPv6 multicast packet.
    private boolean isIpv6Multicast(Ethernet eth) {
        return eth.getEtherType() == Ethernet.TYPE_IPV6 && eth.isMulticast();
    }

    // Selects a path from the given set that does not lead back to the
    // specified port if possible.
    private Path pickForwardPathIfPossible(Set<Path> paths, PortNumber notToPort) {
        for (Path path : paths) {
            if (!path.src().port().equals(notToPort)) {
                return path;
            }
        }
        return null;
    }

    // Floods the specified packet if permissible.
    private void flood(PacketContext context) {
        if (topologyService.isBroadcastPoint(topologyService.currentTopology(),
                                             context.inPacket().receivedFrom())) {
            packetOut(context, PortNumber.FLOOD);
        } else {
            context.block();
        }
    }

    // Sends a packet out the specified port.
    private void packetOut(PacketContext context, PortNumber portNumber) {
        // replyPacket(macMetrics);
        context.treatmentBuilder().setOutput(portNumber);
        context.send();
    }

    private void installRule(PacketContext context, Protocol policy, DeviceId pcktInSwitch, PortNumber portNumber) {
        //
        // We don't support (yet) buffer IDs in the Flow Service so
        // packet out first.
        //
        Ethernet inPkt = context.inPacket().parsed();

        // If PacketOutOnly or ARP packet than forward directly to output port
        if (packetOutOnly || inPkt.getEtherType() == Ethernet.TYPE_ARP) {
            packetOut(context, portNumber);
            return;
        }

        TrafficSelector.Builder selectorBuilder = Protocol.generateProtcolSelector(policy);
        TrafficTreatment treatment;

        if (inheritFlowTreatment) {
            treatment = context.treatmentBuilder()
                    .setOutput(portNumber)
                    .build();
        } else {
            FlowTrafficTreatment.Builder ftt = (FlowTrafficTreatment.Builder) FlowTrafficTreatment.builder();
            // ftt.setOutput(portNumber);

            // log.info("PacketIn Sw: " + pcktInSwitch.toString());            

            if ((policy.isKeepState() || policy.getDependencyProtocol() != null) && 
                policy.getStateEnforcementSwitch().getName().equals(pcktInSwitch.toString())    
            ) {
                boolean keepState = false;
                int stateId = -1;
                boolean isDependent = false;
                int dependencyStateId = -1;

                if (policy.isKeepState()) {
                    // ftt.setStateId(policy.getStateId());
                    keepState = true;
                    stateId = policy.getStateId();
                } 
                // else {
                //     ftt.setStateId(-1);
                // }

                if (policy.getDependencyProtocol() != null) {
                    // ftt.setDependencyStateId(policy.getDependencyProtocol().getStateId());
                    isDependent = true;
                    dependencyStateId = policy.getDependencyProtocol().getStateId();
                } 
                // else {
                //     ftt.setDependencyStateId(-1);
                // }

                ftt.statefulProcessing(keepState, stateId, isDependent, dependencyStateId, portNumber.toLong());
            } else {
                ftt.setOutput(portNumber);
            }

            treatment = ftt.build();
            // treatment =  FlowTrafficTreatment.builder()
            //         .setOutput(portNumber)
            //         .build();
        }

        // FlowTrafficTreatment.Builder ftt = (FlowTrafficTreatment.Builder) FlowTrafficTreatment.builder();
        // ftt.setOutput(portNumber).setStateId(FLOW_TIMEOUT_DEFAULT);
        // treatment = ftt.build();

        /**
         * I want to do something like:
         *      treatemnt = context.treatmentBuilder()
         *                  .setStateful((bool) true)
         *                  .setStateId((int) state_id)
         *                  .setDependencyStateId((int) state_id)
         *                  .setOutput((PortNumber) port) // as is implemented
         */

        ForwardingObjective forwardingObjective = DefaultForwardingObjective.builder()
                .withSelector(selectorBuilder.build())
                .withTreatment(treatment)
                .withPriority(flowPriority)
                .withFlag(ForwardingObjective.Flag.VERSATILE)
                .fromApp(appId)
                // .makeTemporary(flowTimeout)
                .makePermanent()
                .add();

        flowObjectiveService.forward(context.inPacket().receivedFrom().deviceId(),
                                     forwardingObjective);
        //
        // If packetOutOfppTable
        //  Send packet back to the OpenFlow pipeline to match installed flow
        // Else
        //  Send packet direction on the appropriate port
        //
        // if (packetOutOfppTable) {
        //     packetOut(context, PortNumber.TABLE);
        // } else {
        //     packetOut(context, portNumber);
        // }

        if (policy.isKeepState() || (policy.getDependencyProtocol() != null)) {
            packetOut(context, PortNumber.portNumber(RECIRCULATION_PORT));    
        } else {
            packetOut(context, portNumber);
        } 
    }

    // Install a rule forwarding the packet to the specified port.
    private void installRule(PacketContext context, PortNumber portNumber) {
        //
        // We don't support (yet) buffer IDs in the Flow Service so
        // packet out first.
        //
        Ethernet inPkt = context.inPacket().parsed();
        TrafficSelector.Builder selectorBuilder = DefaultTrafficSelector.builder();

        // If PacketOutOnly or ARP packet than forward directly to output port
        if (packetOutOnly || inPkt.getEtherType() == Ethernet.TYPE_ARP) {
            packetOut(context, portNumber);
            return;
        }

        //
        // If matchDstMacOnly
        //    Create flows matching dstMac only
        // Else
        //    Create flows with default matching and include configured fields
        //
        if (matchDstMacOnly) {
            selectorBuilder.matchEthDst(inPkt.getDestinationMAC());
        } else {
            selectorBuilder.matchInPort(context.inPacket().receivedFrom().port())
                    .matchEthSrc(inPkt.getSourceMAC())
                    .matchEthDst(inPkt.getDestinationMAC());

            // If configured Match Vlan ID
            if (matchVlanId && inPkt.getVlanID() != Ethernet.VLAN_UNTAGGED) {
                selectorBuilder.matchVlanId(VlanId.vlanId(inPkt.getVlanID()));
            }

            //
            // If configured and EtherType is IPv4 - Match IPv4 and
            // TCP/UDP/ICMP fields
            //
            if (matchIpv4Address && inPkt.getEtherType() == Ethernet.TYPE_IPV4) {
                IPv4 ipv4Packet = (IPv4) inPkt.getPayload();
                byte ipv4Protocol = ipv4Packet.getProtocol();
                Ip4Prefix matchIp4SrcPrefix =
                        Ip4Prefix.valueOf(ipv4Packet.getSourceAddress(),
                                          Ip4Prefix.MAX_MASK_LENGTH);
                Ip4Prefix matchIp4DstPrefix =
                        Ip4Prefix.valueOf(ipv4Packet.getDestinationAddress(),
                                          Ip4Prefix.MAX_MASK_LENGTH);
                selectorBuilder.matchEthType(Ethernet.TYPE_IPV4)
                        .matchIPSrc(matchIp4SrcPrefix)
                        .matchIPDst(matchIp4DstPrefix);

                if (matchIpv4Dscp) {
                    byte dscp = ipv4Packet.getDscp();
                    byte ecn = ipv4Packet.getEcn();
                    selectorBuilder.matchIPDscp(dscp).matchIPEcn(ecn);
                }

                if (matchTcpUdpPorts && ipv4Protocol == IPv4.PROTOCOL_TCP) {
                    TCP tcpPacket = (TCP) ipv4Packet.getPayload();
                    selectorBuilder.matchIPProtocol(ipv4Protocol)
                            .matchTcpSrc(TpPort.tpPort(tcpPacket.getSourcePort()))
                            .matchTcpDst(TpPort.tpPort(tcpPacket.getDestinationPort()));
                }
                if (matchTcpUdpPorts && ipv4Protocol == IPv4.PROTOCOL_UDP) {
                    UDP udpPacket = (UDP) ipv4Packet.getPayload();
                    selectorBuilder.matchIPProtocol(ipv4Protocol)
                            .matchUdpSrc(TpPort.tpPort(udpPacket.getSourcePort()))
                            .matchUdpDst(TpPort.tpPort(udpPacket.getDestinationPort()));
                }
                if (matchIcmpFields && ipv4Protocol == IPv4.PROTOCOL_ICMP) {
                    ICMP icmpPacket = (ICMP) ipv4Packet.getPayload();
                    selectorBuilder.matchIPProtocol(ipv4Protocol)
                            .matchIcmpType(icmpPacket.getIcmpType())
                            .matchIcmpCode(icmpPacket.getIcmpCode());
                }
            }

            //
            // If configured and EtherType is IPv6 - Match IPv6 and
            // TCP/UDP/ICMP fields
            //
            if (matchIpv6Address && inPkt.getEtherType() == Ethernet.TYPE_IPV6) {
                IPv6 ipv6Packet = (IPv6) inPkt.getPayload();
                byte ipv6NextHeader = ipv6Packet.getNextHeader();
                Ip6Prefix matchIp6SrcPrefix =
                        Ip6Prefix.valueOf(ipv6Packet.getSourceAddress(),
                                          Ip6Prefix.MAX_MASK_LENGTH);
                Ip6Prefix matchIp6DstPrefix =
                        Ip6Prefix.valueOf(ipv6Packet.getDestinationAddress(),
                                          Ip6Prefix.MAX_MASK_LENGTH);
                selectorBuilder.matchEthType(Ethernet.TYPE_IPV6)
                        .matchIPv6Src(matchIp6SrcPrefix)
                        .matchIPv6Dst(matchIp6DstPrefix);

                if (matchIpv6FlowLabel) {
                    selectorBuilder.matchIPv6FlowLabel(ipv6Packet.getFlowLabel());
                }

                if (matchTcpUdpPorts && ipv6NextHeader == IPv6.PROTOCOL_TCP) {
                    TCP tcpPacket = (TCP) ipv6Packet.getPayload();
                    selectorBuilder.matchIPProtocol(ipv6NextHeader)
                            .matchTcpSrc(TpPort.tpPort(tcpPacket.getSourcePort()))
                            .matchTcpDst(TpPort.tpPort(tcpPacket.getDestinationPort()));
                }
                if (matchTcpUdpPorts && ipv6NextHeader == IPv6.PROTOCOL_UDP) {
                    UDP udpPacket = (UDP) ipv6Packet.getPayload();
                    selectorBuilder.matchIPProtocol(ipv6NextHeader)
                            .matchUdpSrc(TpPort.tpPort(udpPacket.getSourcePort()))
                            .matchUdpDst(TpPort.tpPort(udpPacket.getDestinationPort()));
                }
                if (matchIcmpFields && ipv6NextHeader == IPv6.PROTOCOL_ICMP6) {
                    ICMP6 icmp6Packet = (ICMP6) ipv6Packet.getPayload();
                    selectorBuilder.matchIPProtocol(ipv6NextHeader)
                            .matchIcmpv6Type(icmp6Packet.getIcmpType())
                            .matchIcmpv6Code(icmp6Packet.getIcmpCode());
                }
            }
        }
        TrafficTreatment treatment;
        if (inheritFlowTreatment) {
            treatment = context.treatmentBuilder()
                    .setOutput(portNumber)
                    .build();
        } else {
            treatment = DefaultTrafficTreatment.builder()
                    .setOutput(portNumber)
                    .build();
        }

        ForwardingObjective forwardingObjective = DefaultForwardingObjective.builder()
                .withSelector(selectorBuilder.build())
                .withTreatment(treatment)
                .withPriority(flowPriority)
                .withFlag(ForwardingObjective.Flag.VERSATILE)
                .fromApp(appId)
                // .makeTemporary(flowTimeout)
                .makePermanent()
                .add();

        flowObjectiveService.forward(context.inPacket().receivedFrom().deviceId(),
                                     forwardingObjective);
        //
        // If packetOutOfppTable
        //  Send packet back to the OpenFlow pipeline to match installed flow
        // Else
        //  Send packet direction on the appropriate port
        //
        if (packetOutOfppTable) {
            packetOut(context, PortNumber.TABLE);
        } else {
            packetOut(context, portNumber);
        }
    }


    private class InternalTopologyListener implements TopologyListener {
        @Override
        public void event(TopologyEvent event) {
            List<Event> reasons = event.reasons();
            if (reasons != null) {
                reasons.forEach(re -> {
                    if (re instanceof LinkEvent) {
                        LinkEvent le = (LinkEvent) re;
                        if (le.type() == LinkEvent.Type.LINK_REMOVED && blackHoleExecutor != null) {
                            blackHoleExecutor.submit(() -> fixBlackhole(le.subject().src()));
                        }
                    }
                });
            }
        }
    }

    private void fixBlackhole(ConnectPoint egress) {
        Set<FlowEntry> rules = getFlowRulesFrom(egress);
        Set<SrcDstPair> pairs = findSrcDstPairs(rules);

        Map<DeviceId, Set<Path>> srcPaths = new HashMap<>();

        for (SrcDstPair sd : pairs) {
            // get the edge deviceID for the src host
            Host srcHost = hostService.getHost(HostId.hostId(sd.src));
            Host dstHost = hostService.getHost(HostId.hostId(sd.dst));
            if (srcHost != null && dstHost != null) {
                DeviceId srcId = srcHost.location().deviceId();
                DeviceId dstId = dstHost.location().deviceId();
                log.trace("SRC ID is {}, DST ID is {}", srcId, dstId);

                cleanFlowRules(sd, egress.deviceId());

                Set<Path> shortestPaths = srcPaths.get(srcId);
                if (shortestPaths == null) {
                    shortestPaths = topologyService.getPaths(topologyService.currentTopology(),
                            egress.deviceId(), srcId);
                    srcPaths.put(srcId, shortestPaths);
                }
                backTrackBadNodes(shortestPaths, dstId, sd);
            }
        }
    }

    // Backtracks from link down event to remove flows that lead to blackhole
    private void backTrackBadNodes(Set<Path> shortestPaths, DeviceId dstId, SrcDstPair sd) {
        for (Path p : shortestPaths) {
            List<Link> pathLinks = p.links();
            for (int i = 0; i < pathLinks.size(); i = i + 1) {
                Link curLink = pathLinks.get(i);
                DeviceId curDevice = curLink.src().deviceId();

                // skipping the first link because this link's src has already been pruned beforehand
                if (i != 0) {
                    cleanFlowRules(sd, curDevice);
                }

                Set<Path> pathsFromCurDevice =
                        topologyService.getPaths(topologyService.currentTopology(),
                                                 curDevice, dstId);
                if (pickForwardPathIfPossible(pathsFromCurDevice, curLink.src().port()) != null) {
                    break;
                } else {
                    if (i + 1 == pathLinks.size()) {
                        cleanFlowRules(sd, curLink.dst().deviceId());
                    }
                }
            }
        }
    }

    // Removes flow rules off specified device with specific SrcDstPair
    private void cleanFlowRules(SrcDstPair pair, DeviceId id) {
        log.trace("Searching for flow rules to remove from: {}", id);
        log.trace("Removing flows w/ SRC={}, DST={}", pair.src, pair.dst);
        for (FlowEntry r : flowRuleService.getFlowEntries(id)) {
            boolean matchesSrc = false, matchesDst = false;
            for (Instruction i : r.treatment().allInstructions()) {
                if (i.type() == Instruction.Type.OUTPUT) {
                    // if the flow has matching src and dst
                    for (Criterion cr : r.selector().criteria()) {
                        if (cr.type() == Criterion.Type.ETH_DST) {
                            if (((EthCriterion) cr).mac().equals(pair.dst)) {
                                matchesDst = true;
                            }
                        } else if (cr.type() == Criterion.Type.ETH_SRC) {
                            if (((EthCriterion) cr).mac().equals(pair.src)) {
                                matchesSrc = true;
                            }
                        }
                    }
                }
            }
            if (matchesDst && matchesSrc) {
                log.trace("Removed flow rule from device: {}", id);
                flowRuleService.removeFlowRules((FlowRule) r);
            }
        }

    }

    // Returns a set of src/dst MAC pairs extracted from the specified set of flow entries
    private Set<SrcDstPair> findSrcDstPairs(Set<FlowEntry> rules) {
        ImmutableSet.Builder<SrcDstPair> builder = ImmutableSet.builder();
        for (FlowEntry r : rules) {
            MacAddress src = null, dst = null;
            for (Criterion cr : r.selector().criteria()) {
                if (cr.type() == Criterion.Type.ETH_DST) {
                    dst = ((EthCriterion) cr).mac();
                } else if (cr.type() == Criterion.Type.ETH_SRC) {
                    src = ((EthCriterion) cr).mac();
                }
            }
            builder.add(new SrcDstPair(src, dst));
        }
        return builder.build();
    }

    private Set<FlowEntry> getFlowRulesFrom(ConnectPoint egress) {
        ImmutableSet.Builder<FlowEntry> builder = ImmutableSet.builder();
        flowRuleService.getFlowEntries(egress.deviceId()).forEach(r -> {
            if (r.appId() == appId.id()) {
                r.treatment().allInstructions().forEach(i -> {
                    if (i.type() == Instruction.Type.OUTPUT) {
                        if (((Instructions.OutputInstruction) i).port().equals(egress.port())) {
                            builder.add(r);
                        }
                    }
                });
            }
        });

        return builder.build();
    }

    // Wrapper class for a source and destination pair of MAC addresses
    private final class SrcDstPair {
        final MacAddress src;
        final MacAddress dst;

        private SrcDstPair(MacAddress src, MacAddress dst) {
            this.src = src;
            this.dst = dst;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            SrcDstPair that = (SrcDstPair) o;
            return Objects.equals(src, that.src) &&
                    Objects.equals(dst, that.dst);
        }

        @Override
        public int hashCode() {
            return Objects.hash(src, dst);
        }
    }

    private class InternalHostListener implements HostListener {

        @Override
        public void event(HostEvent event) {
            switch (event.type()) {
                case HOST_ADDED:
                case HOST_UPDATED:
                    DeviceId edgeSwitch = event.subject().location().deviceId();
                    edgeSwitches.add(edgeSwitch);
                    log.info("Host connected to new edge switch {}", edgeSwitch);

                    break;
                case HOST_REMOVED:
                    // No need to remove edge switches, as they may still have other hosts connected
                    break;
                default:
                    break;
            }
        }

    }

}
