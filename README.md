# ZT-XPN: An End-to-End Zero-Trust Architecture for Next-Generation Programmable Networks

Welcome to the **ZT-XPN** repository!

## Overview

**Zero-Trust Architecture (ZTA)** mandates strict internal and perimeter defenses, ensuring that communication occurs solely on a per-request and need-only basis. Achieving this requires **robust access control (AC) policies** enforced throughout the network.

**Software-Defined Networking (SDN)** and **programmable data planes** are crucial in implementing ZTA:

- **SDN's centralized management** streamlines access request authorization.
- **Data plane programmability** enables the direct execution of various security functions, including error checking and stateful AC enforcement.

### **Challenges and ZT-XPN Solution**

Despite these benefits, deploying ZTA in programmable networks presents challenges:

1. Defining a **comprehensive network-wide security policy** accommodating the communication needs of all endpoints (e.g., users, IoT, and services).
2. **Manually designing and deploying** data plane programs for policy enforcement.
3. Orchestrating the **control plane** operations for centralized policy management and monitoring.

**ZT-XPN** is the first **end-to-end framework** designed to address these challenges. It consists of three key components:

1. **Graph-Based Policy Specification Tool**
   - Enables the precise and fine-grained definition of complex network-wide policies.
   - Supports endpoint and protocol-level control.

2. **Back-End Compiler Integrated with ONOS SDN Controller**
   - Processes policy requirements and **automatically generates** data plane programs.
   - Orchestrates the control plane to support these policies.

3. **Runtime Management System**
   - Handles policy enforcement, dynamic management, and runtime monitoring.

## Requirements

Ensure you have the following dependencies installed before running ZT-XPN:

| Dependency            | Version         |
|----------------------|---------------|
| Python              | 3.8.10         |
| Java (OpenJDK)      | 11.0.25        |
| Mininet             | 2.3.1b4        |
| ONOS                | 2.5.1          |
| OS Tested On        | Ubuntu 20.04.6 LTS |

## Installation

### Installing P4 Dependencies

ZT-XPN relies on **P4 programming** for data plane enforcement. Several installation scripts automate the process.

#### **Steps:**

1. Use the script from [P4-Guide](https://github.com/jafingerhut/p4-guide/blob/master/bin/README-install-troubleshooting.md):

   ```sh
   git clone https://github.com/jafingerhut/p4-guide.git
   cd p4-guide
   ./install-p4dev-v6.sh
   ```

> **Note:** The installation process takes approximately **2 hours**.

### Setting Up the ONOS Environment

ZT-XPN integrates with **ONOS** for SDN control plane orchestration.

#### **Steps:**

1. Clone the ONOS repository:

   ```sh
   git clone https://github.com/opennetworkinglab/onos.git
   cd onos
   ```

2. Install **Bazel**, the required build system for ONOS:

   ```sh
   sudo apt update
   sudo apt install curl gnupg
   curl -fsSL https://bazel.build/bazel-release.pub.gpg | gpg --dearmor > bazel-archive-keyring.gpg
   sudo mv bazel-archive-keyring.gpg /usr/share/keyrings/
   echo "deb [signed-by=/usr/share/keyrings/bazel-archive-keyring.gpg] https://storage.googleapis.com/bazel-apt stable jdk1.8" | sudo tee /etc/apt/sources.list.d/bazel.list
   sudo apt update && sudo apt install bazel
   ```

   If you encounter issues, refer to this [StackOverflow post](https://stackoverflow.com/questions/65656165/how-do-you-install-bazel-using-bazelisk).

## Repository Structure

The repository is structured as follows:

| Directory | Description |
|-----------|-------------|
| **Appendix** | Contains the online appendix for the paper submission (currently under IEEE conference review). |
| **bmv2_program_compilation** | Contains a bash script for compiling **BMv2 programs** automatically. |
| **mininet_topology_creator** | Includes a script to **orchestrate network topologies**, including end systems and BMv2 programmable switches. |
| **runtime_experiment_artifacts** | Stores **iperf execution results** for various experimented topologies. The summarized results appear in the paper. |
| **switch_configuration_pusher** | Contains a script to **push switch network configurations** to ONOS. |
| **zt-xpn_back_end** | Hosts the **ZT-XPN back-end compiler**, along with the runtime system. |

Each subdirectory contains its own `README.md` file with additional details.

## Usage

Once all dependencies are installed, you can start ONOS and deploy policies using ZT-XPN's **policy specification tool and compiler**.

```sh
cd onos
bazel build onos
bazel run onos-local
```

For Mininet topology testing:

```sh
sudo mn --topo tree,depth=2,fanout=2 --controller=remote,ip=<ONOS_IP>
```

## Contributing

We welcome contributions! To contribute:

1. **Fork** the repository.
2. **Create a feature branch** (`git checkout -b feature-branch`).
3. **Commit your changes** (`git commit -m "Description of changes"`).
4. **Push to your fork** (`git push origin feature-branch`).
5. **Submit a Pull Request** for review.

## License

This project is licensed under the **MIT License**. See the [LICENSE](LICENSE) file for details.
