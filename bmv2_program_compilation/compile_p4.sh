#!/bin/bash

# Directory containing the P4 files
P4_DIR="./"

# Loop through each P4 file following the naming convention device:bmv2:s_.p4
for p4_file in "$P4_DIR"device:bmv2:s*.p4; do
    # Extract the switch number from the file name
    switch_num=$(echo "$p4_file" | sed -E 's/.*s([0-9]+)\.p4/\1/')

    # Define output JSON and P4Runtime files based on the switch number
    json_output="device:bmv2:s${switch_num}.json"
    p4runtime_output="device:bmv2:s${switch_num}.txt"

    # Compile the P4 program
    p4c-bm2-ss --arch v1model -o "$json_output" --p4runtime-files "$p4runtime_output" "$p4_file"

    # Check if the compilation was successful
    if [ $? -eq 0 ]; then
        echo "Compilation of device:bmv2:s${switch_num}.p4 successful."
    else
        echo "Compilation of device:bmv2:s${switch_num}.p4 failed."
    fi
done
