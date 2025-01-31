# Topology Orchestration Script

## Overview

This script **automatically orchestrates the data plane**, including:  
- **Client machines**  
- **TCP and UDP services**  
- **BMv2 programmable switches**  

It provides **topology orchestration scripts** for the network configurations discussed in our paper.

## Notes on Implementation

The script is **partially based on the BMv2 orchestration script** provided by **ONOS**, originally located at:  

```
onos/tools/dev/mininet/bmv2.py
```

However, the script was outdated, so we had to **update and fix** several issues. Key improvements include:

- **Fixed print statement errors**  
  - Updated `print "something"` to `print("something")` (Python 3 syntax).  
- **Resolved urllib2 import issues**  
  - Solution: [StackOverflow Reference](https://stackoverflow.com/questions/2792650/import-error-no-module-name-urllib2)  
- **Fixed URL request payload errors**  
  - Solution: [StackOverflow Reference](https://stackoverflow.com/questions/30760728/python-3-urllib-produces-typeerror-post-data-should-be-bytes-or-an-iterable-of)  

## Running the Script

To execute the script, run:

```sh
sudo python3 topo_creator.py
```

## Important Notes

Before running the script, ensure that:  

1. **ONOS is running**  
2. **All necessary ONOS applications are enabled** (via ONOS CLI)  
3. **ZT-XPN network application is installed on ONOS**  

## Output and Network Configuration

- The script **generates network configurations** for the switches.  
- These configurations are stored in:  

  ```
  /tmp/
  ```

### **Applying the Configuration to ONOS**

After generation, you must **push the network configurations to ONOS** using the **switch configuration pusher**.  

📌 **Refer to the** `switch_configuration_pusher/` **directory in this repository** for more details.  

### **What the Switch Configurations Include**

The switch configurations inform ONOS about:  
- **Switch characteristics** (e.g., number of ports)  
- **Switch management port**  
- **The P4 pipeline** to be installed on the switch  

📌 **The P4 pipeline is generated during the policy orchestration phase.**

---

For any issues or questions, feel free to reach out! 
