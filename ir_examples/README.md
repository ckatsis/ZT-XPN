# Intermediate Representation (IR) JSON Files

## Overview

The **ZT-XPN** front-end is currently not open-sourced due to copyright ownership by the **U.S. Army Research Lab**. However, we plan to release it as open-source by **2026**. However, both the ZT-XPN back-end compiler and the runtime system are open-sourced. 

In the meantime, we are making available the **intermediate representations (IR)**, which are the output of the front-end and essential for understanding how ZT-XPN processes policies. These IR files provide structured representations of network security policies derived from **ZT-XPN's policy graph**.

If you require additional IR files, please feel free to contact me at **[ckatsis@purdue.edu](mailto:ckatsis@purdue.edu)**.

## Available IR Files

This directory contains the **IR requirements** generated from the **policy graph in Figure 8**, as described in **Section F of the appendix**. Specifically, we provide:

- **`IR_example_30.json`** – IR for **30 users per role**.
- **`IR_example_60.json`** – IR for **60 users per role**.

These files serve as structured representations of access control policies for different user scales. If you have any questions or need more IR files, don’t hesitate to reach out.

---

Stay tuned for future updates as we work toward open-sourcing the complete ZT-XPN front-end.
