# BMv2 Switch Runtime Configurations

## Overview

This directory contains **JSON files** that serve as examples of **BMv2 switch runtime configurations**.  
These configurations are **automatically generated** when running the **topology orchestration script** in:  

```
ZT-XPN/mininet_topology_creator/
```

For more details, refer to the **mininet_topology_creator** directory.

## Applying the Configurations

To automatically **push all network configurations to ONOS**, use the provided **Bash script**:

```sh
./push_configs.sh
```

### **Usage Instructions**

1. **Ensure all JSON configuration files** are placed in the **same directory** as `push_configs.sh`.
2. **Run the script** to deploy the configurations to ONOS.

This will update the network settings in ONOS based on the generated BMv2 configurations.

---

For any additional setup details, refer to the documentation in `mininet_topology_creator/`.
