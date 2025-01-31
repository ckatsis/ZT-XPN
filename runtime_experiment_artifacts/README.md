# Runtime System Performance Artifacts

## Overview

This directory contains the **experiment artifacts** for evaluating the **runtime system performance** of **ZT-XPN**.  
These artifacts correspond to the results **reported in the paper** and include performance metrics from bandwidth testing using `iperf`.

## Artifacts Directory Structure

The directory contains **host runtime performance logs** generated when executing `iperf` in **bandwidth testing mode** across different network configurations.  
Each log file follows this naming convention:

```
<system>_<topology>_[stateful]_<proactive/reactive>.log
```

### **Naming Convention Explanation**

#### **1. `<system>` (System Used)**
Indicates the network system under evaluation:

- **`fwd`** – ONOS' **reactive forwarding application**  
  - **Control plane app**: [ReactiveForwarding.java](https://github.com/opennetworkinglab/onos/blob/master/apps/fwd/src/main/java/org/onosproject/fwd/ReactiveForwarding.java)  
  - **Data plane program**: [mytunnel.p4](https://github.com/opennetworkinglab/onos/blob/master/apps/p4-tutorial/pipeconf/src/main/resources/mytunnel.p4)  

- **`reference_fwd`** – Baseline reference **firewall** from **P4Lang**  
  - **Reference firewall**: [firewall.p4](https://github.com/p4lang/tutorials/blob/master/exercises/firewall/solution/firewall.p4)  

- **`zt-xpn`** – Results when **ZT-XPN-generated** programs are deployed in the data plane  
  - **If `stateful` appears in the file name**, the log pertains to the **stateful firewall implementation**; otherwise, it represents a **stateless** deployment.

#### **2. `<topology>` (Topology Configuration)**
Expressed in the format:

```
CxS
```

Where:
- **C** = Number of **client machines**  
- **S** = Number of **switches** in the path between each client and the server  

#### **3. `<proactive/reactive>` (Policy Deployment Strategy)**
Indicates how policies are enforced:

- **`proactive`** – All policies are **pre-installed** on every switch (**no control plane intervention**).  
- **`reactive`** – Policies are **dynamically installed** at the time of a client's request (**control plane intervention required**).  

📌 **Note:** The **reference firewall does not support reactive policy deployment**, as it lacks support for **stateful policy enforcement and revocation**.

---

## Included Scripts

### **1. `iperf_result_parser.py`**  
A helper script to analyze log files in this directory.  

**Usage:**  
Insert the contents of any **log file** from the artifacts directory, and the script will compute:  

- **Average throughput** across all hosts  
- **Total data transmitted**  

This script helps to efficiently summarize the network performance under different configurations.

---

For further questions or additional data requests, feel free to reach out! 🚀
