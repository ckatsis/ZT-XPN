# ONOS Setup

## Running ONOS

1. Navigate to the ONOS directory:
   ```sh
   cd <ONOS_DIRECTORY>
   ```
2. Start ONOS:
   ```sh
   bazel run onos-local -- clean debug
   ```

## ONOS CLI Configuration

Activate the following services:
```sh
app activate org.onosproject.proxyarp
app activate org.onosproject.lldpprovider
app activate org.onosproject.hostprovider
app activate org.onosproject.drivers.bmv2
cfg set org.onosproject.net.flow.impl.FlowRuleManager fallbackFlowPollFrequency 15
```

# Front End Configuration

1. Use one of the example intermediate representations (IRs) or provide your own.
2. Copy the IR into the ONOS runtime file repository:
   ```sh
   cp network_req.json /tmp/onos-2.5.10-SNAPSHOT/apache-karaf-4.2.14/
   ```
3. Append the following lines to the JSON file:
   ```json
   "ANY": {
       "ANY": [
           {"fid": 1000, "protocol": "ETHERNET", "etherType": "ARP", "isStateful": false, "dependecyFid": -1},
           {"fid": 1001, "protocol": "ETHERNET", "etherType": "BDDP", "isStateful": false, "dependecyFid": -1},
           {"fid": 1002, "protocol": "ETHERNET", "etherType": "LLDP", "isStateful": false, "dependecyFid": -1}
       ]
   }
   ```
   This policy must be specified; otherwise, the data plane switches will drop any ARP, BDDP, and LLDP messages.

# P4-SDN Application (ZT-XPN Back-End)

## Compilation and Installation

1. Compile the P4-SDN App:
   ```sh
   mvn clean install -DskipTests
   ```
2. Install the app on the ONOS controller:
   ```sh
   onos-app localhost install! target/p4sdn-app-1.0-SNAPSHOT.oar
   ```
3. This will generate P4 programs for each switch in:
   ```sh
   /tmp/onos-2.5.10-SNAPSHOT/apache-karaf-4.2.14
   ```
4. Copy the generated programs:
   ```sh
   cp device:bmv2:s*.p4 ZT-XPN/bmv2_program_compilation
   ```
5. Navigate to the directory and compile the P4 programs:
   ```sh
   cd ZT-XPN/bmv2_program_compilation
   ./compile_p4.sh
   ```
6. Copy the compiled programs and files into:
   ```sh
   ~/p4sdn-app/src/main/resources
   ```

# Data Plane Configuration (mybmvtopo.py)

1. Navigate to the topology creator script:
   ```sh
   cd ZT-XPN/mininet_topology_creator
   ```
2. Ensure the data plane topology matches the one in `TopologyMap.java` in the P4-SDN app.
3. Run the topology script:
   ```sh
   sudo python3 mybmvtopo.py
   ```
   This will generate switch netcfg files in `/tmp`.
4. Copy the generated netcfg files:
   ```sh
   cp bmv2-s*-netcfg.json ZT-XPN/switch_configuration_pusher/
   ```
5. Navigate to the directory and push the configurations:
   ```sh
   cd ZT-XPN/switch_configuration_pusher
   ./push_configs.sh
   ```

## Cleanup

After completing the experiment, clean up Mininet:
```sh
sudo mn -c
```

# Experiment Logs

The experiment generates throughput log files for each host in `/tmp/logs`. Ensure that the `logs` directory exists before running the experiment.
