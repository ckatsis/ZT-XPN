# ZT-XPN: An end-to-end Zero-Trust Architecture for Next Generation
# Programmable Networks
# 
# Authors:  Charalampos Katsis  (ckatsis@purdue.edu)
#           Elisa Bertino       (bertino@purdue.edu)
# =================================================================



#!/bin/bash

# Directory where the JSON configuration files are located
CONFIG_DIR="./"

# Loop through all files matching the pattern bmv2-s*-netcfg.json
for file in ${CONFIG_DIR}bmv2-s*-netcfg.json; do
  if [[ -f "$file" ]]; then
    echo "Pushing configuration for $file..."
    curl --fail -sSL --user onos:rocks --noproxy localhost -X POST -H 'Content-Type:application/json' http://localhost:8181/onos/v1/network/configuration -d@"$file"
    
    if [[ $? -ne 0 ]]; then
      echo "Failed to push configuration for $file"
    else
      echo "Successfully pushed configuration for $file"
    fi
  fi
done
