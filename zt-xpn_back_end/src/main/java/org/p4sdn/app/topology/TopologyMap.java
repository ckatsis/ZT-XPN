package org.p4sdn.app.topology;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.p4sdn.app.net.CoreSwitch;
import org.p4sdn.app.net.EdgeSwitch;
import org.p4sdn.app.net.Switch;
import org.p4sdn.app.net.Host;

public class TopologyMap {

    private HashMap<String, String> userToIpMap = new HashMap<>();
    private HashSet<Switch> switches = new HashSet<>();
    private HashSet<Host> hosts = new HashSet<>();
    private HashMap<Host, EdgeSwitch> hostNetConnections = new HashMap<>();
    private HashMap<String, Host> nameToHost = new HashMap<>();


    public TopologyMap(boolean create_example_map) {
        if(create_example_map) {
            // setUpExampleMap();
            // exampleTopo2();
            // exampleTopo3();
            // exampleTopo4();
            fwdComparisonTopo();
            // referenceFWComparisonTopo();
        }
    }

    public void exampleTopo2() {
        EdgeSwitch sw1 = new EdgeSwitch();
        CoreSwitch sw2 = new CoreSwitch();
        EdgeSwitch sw3 = new EdgeSwitch();
        addSwitch(sw1);
        addSwitch(sw2);
        addSwitch(sw3);

        // Host machines
        Host h1 = new Host("Alice", "10.0.0.1", sw1);
        Host h2 = new Host("TCP service", "10.0.0.2", sw3);
        addHost(h1);
        addHost(h2);
        // Adding links
        addHostToEdgeLink(h1, sw1);
        addHostToEdgeLink(h2, sw3);
    }

    public void exampleTopo3() {
        EdgeSwitch sw1 = new EdgeSwitch();
        CoreSwitch sw2 = new CoreSwitch();
        EdgeSwitch sw3 = new EdgeSwitch();
        addSwitch(sw1);
        addSwitch(sw2);
        addSwitch(sw3);

        // Host machines
        Host h1 = new Host("Alice", "10.0.0.1", sw1);
        Host h2 = new Host("Bob", "10.0.1.2", sw1);
        Host h3 = new Host("Penny", "10.0.2.3", sw1);

        Host h4 = new Host("TCP service_1", "10.0.3.4", sw3);
        Host h5 = new Host("UDP service", "10.0.4.5", sw3);
        Host h6 = new Host("TCP service_2", "10.0.4.6", sw3);
        addHost(h1);
        addHost(h2);
        addHost(h3);
        addHost(h4);
        addHost(h5);
        addHost(h6);
        // Adding links
        addHostToEdgeLink(h1, sw1);
        addHostToEdgeLink(h2, sw1);
        addHostToEdgeLink(h3, sw1);
        addHostToEdgeLink(h4, sw3);
        addHostToEdgeLink(h5, sw3);
        addHostToEdgeLink(h6, sw3);
    }

    public void exampleTopo4() {

        int numSwitches = 5;
        EdgeSwitch swClients = new EdgeSwitch();
        addSwitch(swClients);

        for(int i = 0; i < numSwitches - 2; i++){
            addSwitch(new CoreSwitch());
        }

        EdgeSwitch swServers = new EdgeSwitch();
        addSwitch(swServers);

        // Host machines
        int totalUsersInRoles = 10; 
        int ipIndex = 1;
        int net1 = 0;
        for(int i = 0; i < totalUsersInRoles; i++ ){
            
            if (ipIndex == 254) {
                net1++;
                ipIndex = 1;
            }
            
            Host host = new Host("AllUsers_" + i, "10.0." + net1 + "." + ipIndex, swClients);
            addHost(host);
            addHostToEdgeLink(host, swClients);
            ipIndex++;
        }

        for(int i = 0; i < totalUsersInRoles; i++ ){
            
            if (ipIndex == 254) {
                net1++;
                ipIndex = 1;
            }

            Host host = new Host("Role2_" + i, "10.0." + net1 + "." + ipIndex, swClients);
            addHost(host);
            addHostToEdgeLink(host, swClients);
            ipIndex++;
        }

        
        for(int i = 0; i < totalUsersInRoles; i++ ){

            if (ipIndex == 254) {
                net1++;
                ipIndex = 1;
            }

            Host host = new Host("Role3_" + i, "10.0." + net1 + "." + ipIndex, swClients);
            addHost(host);
            addHostToEdgeLink(host, swClients);
            ipIndex++;
        }

        ipIndex = 1;
        net1 += 1;
        Host h4 = new Host("TCP service_1", "10.0." + net1 + "." + ipIndex, swServers);
        ipIndex++;
        Host h5 = new Host("UDP service", "10.0." + net1 + "." + ipIndex, swServers);
        ipIndex++;
        Host h6 = new Host("TCP service_2", "10.0." + net1 + "." + ipIndex, swServers);
        ipIndex++;

        addHost(h4);
        addHost(h5);
        addHost(h6);
        // Adding links
        addHostToEdgeLink(h4, swServers);
        addHostToEdgeLink(h5, swServers);
        addHostToEdgeLink(h6, swServers);

    }

    public void referenceFWComparisonTopo() {

        int numSwitches = 5;
        EdgeSwitch swClients = new EdgeSwitch();
        addSwitch(swClients);

        for(int i = 0; i < numSwitches - 2; i++){
            addSwitch(new CoreSwitch());
        }

        EdgeSwitch swServers = new EdgeSwitch();
        addSwitch(swServers);

        // Host machines
        int totalUsersInRoles = 30; 
        int ipIndex = 1;
        int net1 = 0;
        for(int i = 0; i < totalUsersInRoles; i++ ){
            
            if (ipIndex == 254) {
                net1++;
                ipIndex = 1;
            }
            
            Host host = new Host("AllUsers_" + i, "10.0." + net1 + "." + ipIndex, swClients);
            addHost(host);
            addHostToEdgeLink(host, swClients);
            ipIndex++;
        }

        ipIndex = 1;
        net1 += 1;
        Host h4 = new Host("TCP service_1", "10.0." + net1 + "." + ipIndex, swServers);
        ipIndex++;
        Host h5 = new Host("UDP service", "10.0." + net1 + "." + ipIndex, swServers);
        ipIndex++;
        Host h6 = new Host("TCP service_2", "10.0." + net1 + "." + ipIndex, swServers);
        ipIndex++;

        addHost(h4);
        addHost(h5);
        addHost(h6);
        // Adding links
        addHostToEdgeLink(h4, swServers);
        addHostToEdgeLink(h5, swServers);
        addHostToEdgeLink(h6, swServers);

    }

    public void fwdComparisonTopo() {

        int numSwitches = 5;
        EdgeSwitch swClients = new EdgeSwitch();
        addSwitch(swClients);

        for(int i = 0; i < numSwitches - 2; i++){
            addSwitch(new CoreSwitch());
        }

        EdgeSwitch swServers = new EdgeSwitch();
        addSwitch(swServers);

        // Host machines
        int totalUsersInRoles = 30; 
        int ipIndex = 1;
        int net1 = 0;
        
        for(int i = 0; i < totalUsersInRoles; i++ ){

            if (ipIndex == 254) {
                net1++;
                ipIndex = 1;
            }

            Host host = new Host("Role3_" + i, "10.0." + net1 + "." + ipIndex, swClients);
            addHost(host);
            addHostToEdgeLink(host, swClients);
            ipIndex++;
        }

        ipIndex = 1;
        net1 += 1;
        Host h6 = new Host("TCP service_2", "10.0." + net1 + "." + ipIndex, swServers);
        ipIndex++;
        Host h4 = new Host("TCP service_1", "10.0." + net1 + "." + ipIndex, swServers);
        ipIndex++;
        Host h5 = new Host("UDP service", "10.0." + net1 + "." + ipIndex, swServers);
        // ipIndex++;
        

        addHost(h4);
        addHost(h5);
        addHost(h6);
        // Adding links
        addHostToEdgeLink(h4, swServers);
        addHostToEdgeLink(h5, swServers);
        addHostToEdgeLink(h6, swServers);

    }


    public void setUpExampleMap() {
        // Edge switch
        EdgeSwitch sw = new EdgeSwitch();
        addSwitch(sw);
        // Host machines
        Host h1 = new Host("Alice", "10.0.0.1", sw);
        Host h2 = new Host("TCP service", "10.0.0.2", sw);
        addHost(h1);
        addHost(h2);
        // Adding links
        addHostToEdgeLink(h1, sw);
        addHostToEdgeLink(h2, sw);
    }

    public void addHost(Host h) {
        hosts.add(h);
        addUserToIpMapping(h.getName(), h.getIpv4());
        addNameToHost(h);
    }

    public void addNameToHost(Host h) {
        nameToHost.put(h.getName(), h);
    }
    
    public Host getHostByName(String name) {
        return nameToHost.get(name);
    }

    public void addSwitch(Switch sw) {
        switches.add(sw);
    }

    public void addHostToEdgeLink(Host h, EdgeSwitch sw) {
        hostNetConnections.put(h, sw);
        sw.addHost(h);
    }

    // Method to add a mapping of user ID to IP address
    public void addUserToIpMapping(String userId, String ipAddress) {
        userToIpMap.put(userId, ipAddress);
    }

    // Method to get the IP address corresponding to a user ID
    public String getIpAddress(String userId) {
        return userToIpMap.get(userId);
    }

    // Method to check if a user ID exists in the topology
    public boolean containsUserId(String userId) {
        return userToIpMap.containsKey(userId);
    }

    // Method to remove a mapping of user ID to IP address
    public void removeUserToIpMapping(String userId) {
        userToIpMap.remove(userId);
    }

    // Method to get all user IDs in the topology
    public Set<String> getAllUserIds() {
        return userToIpMap.keySet();
    }

    // Method to get all IP addresses in the topology
    public Collection<String> getAllIpAddresses() {
        return userToIpMap.values();
    }

    public HashSet<Switch> getSwitches() {
        return switches;
    }
}
