import re
import pandas as pd

# Log string -- Replace with iperf tool output
log = '''------------------------------------------------------------
Client connecting to 10.0.4.4, TCP port 5050
TCP window size: 85.3 KByte (default)
------------------------------------------------------------
[  3] local 10.0.1.10 port 57182 connected with 10.0.4.4 port 5050
[ ID] Interval       Transfer     Bandwidth
[  3]  0.0-30.6 sec  4.88 MBytes  1.34 Mbits/sec
------------------------------------------------------------
Client connecting to 10.0.4.4, TCP port 5050
TCP window size: 85.3 KByte (default)
------------------------------------------------------------
[  3] local 10.0.1.11 port 40964 connected with 10.0.4.4 port 5050
[ ID] Interval       Transfer     Bandwidth
[  3]  0.0-31.7 sec  3.25 MBytes   860 Kbits/sec
------------------------------------------------------------
Client connecting to 10.0.4.4, TCP port 5050
TCP window size: 85.3 KByte (default)
------------------------------------------------------------
[  3] local 10.0.1.12 port 52618 connected with 10.0.4.4 port 5050
[ ID] Interval       Transfer     Bandwidth
[  3]  0.0-33.9 sec  5.38 MBytes  1.33 Mbits/sec
------------------------------------------------------------
Client connecting to 10.0.4.4, TCP port 5050
TCP window size: 85.3 KByte (default)
------------------------------------------------------------
[  3] local 10.0.1.13 port 58340 connected with 10.0.4.4 port 5050
[ ID] Interval       Transfer     Bandwidth
[  3]  0.0-36.1 sec  4.25 MBytes   989 Kbits/sec
------------------------------------------------------------
Client connecting to 10.0.4.4, TCP port 5050
TCP window size: 85.3 KByte (default)
------------------------------------------------------------
[  3] local 10.0.1.14 port 55566 connected with 10.0.4.4 port 5050
[ ID] Interval       Transfer     Bandwidth
[  3]  0.0-31.3 sec  3.50 MBytes   938 Kbits/sec
------------------------------------------------------------
Client connecting to 10.0.4.4, TCP port 5050
TCP window size: 85.3 KByte (default)
------------------------------------------------------------
[  3] local 10.0.1.15 port 56052 connected with 10.0.4.4 port 5050
[ ID] Interval       Transfer     Bandwidth
[  3]  0.0-34.1 sec  7.12 MBytes  1.75 Mbits/sec
------------------------------------------------------------
Client connecting to 10.0.4.4, TCP port 5050
TCP window size: 85.3 KByte (default)
------------------------------------------------------------
[  3] local 10.0.1.16 port 42874 connected with 10.0.4.4 port 5050
[ ID] Interval       Transfer     Bandwidth
[  3]  0.0-35.8 sec  6.00 MBytes  1.41 Mbits/sec
------------------------------------------------------------
Client connecting to 10.0.4.4, TCP port 5050
TCP window size: 85.3 KByte (default)
------------------------------------------------------------
[  3] local 10.0.1.17 port 42644 connected with 10.0.4.4 port 5050
[ ID] Interval       Transfer     Bandwidth
[  3]  0.0-35.5 sec  4.00 MBytes   946 Kbits/sec
------------------------------------------------------------
Client connecting to 10.0.4.4, TCP port 5050
TCP window size: 85.3 KByte (default)
------------------------------------------------------------
[  3] local 10.0.1.18 port 56726 connected with 10.0.4.4 port 5050
[ ID] Interval       Transfer     Bandwidth
[  3]  0.0-31.4 sec  6.38 MBytes  1.70 Mbits/sec
------------------------------------------------------------
Client connecting to 10.0.4.4, TCP port 5050
TCP window size: 85.3 KByte (default)
------------------------------------------------------------
[  3] local 10.0.1.19 port 42704 connected with 10.0.4.4 port 5050
[ ID] Interval       Transfer     Bandwidth
[  3]  0.0-34.3 sec  4.00 MBytes   978 Kbits/sec
------------------------------------------------------------
Client connecting to 10.0.4.4, TCP port 5050
TCP window size: 85.3 KByte (default)
------------------------------------------------------------
[  3] local 10.0.1.1 port 43174 connected with 10.0.4.4 port 5050
[ ID] Interval       Transfer     Bandwidth
[  3]  0.0-31.1 sec  24.5 MBytes  6.61 Mbits/sec
------------------------------------------------------------
Client connecting to 10.0.4.4, TCP port 5050
TCP window size: 85.3 KByte (default)
------------------------------------------------------------
[  3] local 10.0.1.20 port 52770 connected with 10.0.4.4 port 5050
[ ID] Interval       Transfer     Bandwidth
[  3]  0.0-33.9 sec  6.62 MBytes  1.64 Mbits/sec
------------------------------------------------------------
Client connecting to 10.0.4.4, TCP port 5050
TCP window size: 85.3 KByte (default)
------------------------------------------------------------
[  3] local 10.0.1.21 port 43194 connected with 10.0.4.4 port 5050
[ ID] Interval       Transfer     Bandwidth
[  3]  0.0-31.0 sec  5.25 MBytes  1.42 Mbits/sec
------------------------------------------------------------
Client connecting to 10.0.4.4, TCP port 5050
TCP window size: 85.3 KByte (default)
------------------------------------------------------------
[  3] local 10.0.1.22 port 58556 connected with 10.0.4.4 port 5050
[ ID] Interval       Transfer     Bandwidth
[  3]  0.0-30.2 sec  4.25 MBytes  1.18 Mbits/sec
------------------------------------------------------------
Client connecting to 10.0.4.4, TCP port 5050
TCP window size: 85.3 KByte (default)
------------------------------------------------------------
[  3] local 10.0.1.23 port 51166 connected with 10.0.4.4 port 5050
[ ID] Interval       Transfer     Bandwidth
[  3]  0.0-33.8 sec  4.50 MBytes  1.12 Mbits/sec
------------------------------------------------------------
Client connecting to 10.0.4.4, TCP port 5050
TCP window size: 85.3 KByte (default)
------------------------------------------------------------
[  3] local 10.0.1.24 port 35938 connected with 10.0.4.4 port 5050
[ ID] Interval       Transfer     Bandwidth
[  3]  0.0-31.3 sec  4.25 MBytes  1.14 Mbits/sec
------------------------------------------------------------
Client connecting to 10.0.4.4, TCP port 5050
TCP window size: 85.3 KByte (default)
------------------------------------------------------------
[  3] local 10.0.1.25 port 36388 connected with 10.0.4.4 port 5050
[ ID] Interval       Transfer     Bandwidth
[  3]  0.0-34.8 sec  4.75 MBytes  1.14 Mbits/sec
------------------------------------------------------------
Client connecting to 10.0.4.4, TCP port 5050
TCP window size: 85.3 KByte (default)
------------------------------------------------------------
[  3] local 10.0.1.26 port 50418 connected with 10.0.4.4 port 5050
[ ID] Interval       Transfer     Bandwidth
[  3]  0.0-30.6 sec  4.00 MBytes  1.10 Mbits/sec
------------------------------------------------------------
Client connecting to 10.0.4.4, TCP port 5050
TCP window size: 85.3 KByte (default)
------------------------------------------------------------
[  3] local 10.0.1.27 port 44166 connected with 10.0.4.4 port 5050
[ ID] Interval       Transfer     Bandwidth
[  3]  0.0-33.1 sec  4.25 MBytes  1.08 Mbits/sec
------------------------------------------------------------
Client connecting to 10.0.4.4, TCP port 5050
TCP window size: 85.3 KByte (default)
------------------------------------------------------------
[  3] local 10.0.1.28 port 59648 connected with 10.0.4.4 port 5050
[ ID] Interval       Transfer     Bandwidth
[  3]  0.0-36.3 sec  4.88 MBytes  1.13 Mbits/sec
------------------------------------------------------------
Client connecting to 10.0.4.4, TCP port 5050
TCP window size: 85.3 KByte (default)
------------------------------------------------------------
[  3] local 10.0.1.29 port 46864 connected with 10.0.4.4 port 5050
[ ID] Interval       Transfer     Bandwidth
[  3]  0.0-33.8 sec  9.25 MBytes  2.30 Mbits/sec
------------------------------------------------------------
Client connecting to 10.0.4.4, TCP port 5050
TCP window size: 85.3 KByte (default)
------------------------------------------------------------
[  3] local 10.0.1.2 port 48736 connected with 10.0.4.4 port 5050
[ ID] Interval       Transfer     Bandwidth
[  3]  0.0-33.2 sec  13.0 MBytes  3.28 Mbits/sec
------------------------------------------------------------
Client connecting to 10.0.4.4, TCP port 5050
TCP window size: 85.3 KByte (default)
------------------------------------------------------------
[  3] local 10.0.1.30 port 42658 connected with 10.0.4.4 port 5050
[ ID] Interval       Transfer     Bandwidth
[  3]  0.0-35.8 sec  6.38 MBytes  1.50 Mbits/sec
------------------------------------------------------------
Client connecting to 10.0.4.4, TCP port 5050
TCP window size: 85.3 KByte (default)
------------------------------------------------------------
[  3] local 10.0.1.31 port 59738 connected with 10.0.4.4 port 5050
[ ID] Interval       Transfer     Bandwidth
[  3]  0.0-32.3 sec  3.50 MBytes   909 Kbits/sec
------------------------------------------------------------
Client connecting to 10.0.4.4, TCP port 5050
TCP window size: 85.3 KByte (default)
------------------------------------------------------------
[  3] local 10.0.1.32 port 58010 connected with 10.0.4.4 port 5050
[ ID] Interval       Transfer     Bandwidth
[  3]  0.0-30.3 sec  3.62 MBytes  1.00 Mbits/sec
------------------------------------------------------------
Client connecting to 10.0.4.4, TCP port 5050
TCP window size: 85.3 KByte (default)
------------------------------------------------------------
[  3] local 10.0.1.33 port 53074 connected with 10.0.4.4 port 5050
[ ID] Interval       Transfer     Bandwidth
[  3]  0.0-34.0 sec  5.75 MBytes  1.42 Mbits/sec
------------------------------------------------------------
Client connecting to 10.0.4.4, TCP port 5050
TCP window size: 85.3 KByte (default)
------------------------------------------------------------
[  3] local 10.0.1.34 port 36832 connected with 10.0.4.4 port 5050
[ ID] Interval       Transfer     Bandwidth
[  3]  0.0-33.5 sec  4.25 MBytes  1.06 Mbits/sec
------------------------------------------------------------
Client connecting to 10.0.4.4, TCP port 5050
TCP window size: 85.3 KByte (default)
------------------------------------------------------------
[  3] local 10.0.1.35 port 41228 connected with 10.0.4.4 port 5050
[ ID] Interval       Transfer     Bandwidth
[  3]  0.0-30.7 sec  5.00 MBytes  1.37 Mbits/sec
------------------------------------------------------------
Client connecting to 10.0.4.4, TCP port 5050
TCP window size: 85.3 KByte (default)
------------------------------------------------------------
[  3] local 10.0.1.36 port 51298 connected with 10.0.4.4 port 5050
[ ID] Interval       Transfer     Bandwidth
[  3]  0.0-30.9 sec  3.88 MBytes  1.05 Mbits/sec
------------------------------------------------------------
Client connecting to 10.0.4.4, TCP port 5050
TCP window size: 85.3 KByte (default)
------------------------------------------------------------
[  3] local 10.0.1.37 port 35530 connected with 10.0.4.4 port 5050
[ ID] Interval       Transfer     Bandwidth
[  3]  0.0-30.4 sec  3.62 MBytes  1.00 Mbits/sec
------------------------------------------------------------
Client connecting to 10.0.4.4, TCP port 5050
TCP window size: 85.3 KByte (default)
------------------------------------------------------------
[  3] local 10.0.1.38 port 33920 connected with 10.0.4.4 port 5050
[ ID] Interval       Transfer     Bandwidth
[  3]  0.0-35.7 sec  5.50 MBytes  1.29 Mbits/sec
------------------------------------------------------------
Client connecting to 10.0.4.4, TCP port 5050
TCP window size: 85.3 KByte (default)
------------------------------------------------------------
[  3] local 10.0.1.39 port 46314 connected with 10.0.4.4 port 5050
[ ID] Interval       Transfer     Bandwidth
[  3]  0.0-34.2 sec  6.25 MBytes  1.53 Mbits/sec
------------------------------------------------------------
Client connecting to 10.0.4.4, TCP port 5050
TCP window size: 85.3 KByte (default)
------------------------------------------------------------
[  3] local 10.0.1.3 port 32880 connected with 10.0.4.4 port 5050
[ ID] Interval       Transfer     Bandwidth
[  3]  0.0-35.2 sec  8.50 MBytes  2.03 Mbits/sec
------------------------------------------------------------
Client connecting to 10.0.4.4, TCP port 5050
TCP window size: 85.3 KByte (default)
------------------------------------------------------------
[  3] local 10.0.1.40 port 42012 connected with 10.0.4.4 port 5050
[ ID] Interval       Transfer     Bandwidth
[  3]  0.0-32.3 sec  3.25 MBytes   844 Kbits/sec
------------------------------------------------------------
Client connecting to 10.0.4.4, TCP port 5050
TCP window size: 85.3 KByte (default)
------------------------------------------------------------
[  3] local 10.0.1.41 port 42040 connected with 10.0.4.4 port 5050
[ ID] Interval       Transfer     Bandwidth
[  3]  0.0-33.9 sec  6.50 MBytes  1.61 Mbits/sec
------------------------------------------------------------
Client connecting to 10.0.4.4, TCP port 5050
TCP window size: 85.3 KByte (default)
------------------------------------------------------------
[  3] local 10.0.1.42 port 38656 connected with 10.0.4.4 port 5050
[ ID] Interval       Transfer     Bandwidth
[  3]  0.0-32.1 sec  7.88 MBytes  2.06 Mbits/sec
------------------------------------------------------------
Client connecting to 10.0.4.4, TCP port 5050
TCP window size: 85.3 KByte (default)
------------------------------------------------------------
[  3] local 10.0.1.43 port 50204 connected with 10.0.4.4 port 5050
[ ID] Interval       Transfer     Bandwidth
[  3]  0.0-33.1 sec  4.50 MBytes  1.14 Mbits/sec
------------------------------------------------------------
Client connecting to 10.0.4.4, TCP port 5050
TCP window size: 85.3 KByte (default)
------------------------------------------------------------
[  3] local 10.0.1.44 port 50456 connected with 10.0.4.4 port 5050
[ ID] Interval       Transfer     Bandwidth
[  3]  0.0-30.2 sec  4.25 MBytes  1.18 Mbits/sec
------------------------------------------------------------
Client connecting to 10.0.4.4, TCP port 5050
TCP window size: 85.3 KByte (default)
------------------------------------------------------------
[  3] local 10.0.1.45 port 39552 connected with 10.0.4.4 port 5050
[ ID] Interval       Transfer     Bandwidth
[  3]  0.0-34.6 sec  4.12 MBytes  1.00 Mbits/sec
------------------------------------------------------------
Client connecting to 10.0.4.4, TCP port 5050
TCP window size: 85.3 KByte (default)
------------------------------------------------------------
[  3] local 10.0.1.46 port 35424 connected with 10.0.4.4 port 5050
[ ID] Interval       Transfer     Bandwidth
[  3]  0.0-31.7 sec  4.75 MBytes  1.26 Mbits/sec
------------------------------------------------------------
Client connecting to 10.0.4.4, TCP port 5050
TCP window size: 85.3 KByte (default)
------------------------------------------------------------
[  3] local 10.0.1.47 port 48114 connected with 10.0.4.4 port 5050
[ ID] Interval       Transfer     Bandwidth
[  3]  0.0-31.7 sec  4.25 MBytes  1.13 Mbits/sec
------------------------------------------------------------
Client connecting to 10.0.4.4, TCP port 5050
TCP window size: 85.3 KByte (default)
------------------------------------------------------------
[  3] local 10.0.1.48 port 33014 connected with 10.0.4.4 port 5050
[ ID] Interval       Transfer     Bandwidth
[  3]  0.0-32.0 sec  5.38 MBytes  1.41 Mbits/sec
------------------------------------------------------------
Client connecting to 10.0.4.4, TCP port 5050
TCP window size: 85.3 KByte (default)
------------------------------------------------------------
[  3] local 10.0.1.49 port 60830 connected with 10.0.4.4 port 5050
[ ID] Interval       Transfer     Bandwidth
[  3]  0.0-32.8 sec  4.12 MBytes  1.06 Mbits/sec
------------------------------------------------------------
Client connecting to 10.0.4.4, TCP port 5050
TCP window size: 85.3 KByte (default)
------------------------------------------------------------
[  3] local 10.0.1.4 port 51596 connected with 10.0.4.4 port 5050
[ ID] Interval       Transfer     Bandwidth
[  3]  0.0-34.1 sec  6.88 MBytes  1.69 Mbits/sec
------------------------------------------------------------
Client connecting to 10.0.4.4, TCP port 5050
TCP window size: 85.3 KByte (default)
------------------------------------------------------------
[  3] local 10.0.1.50 port 60444 connected with 10.0.4.4 port 5050
[ ID] Interval       Transfer     Bandwidth
[  3]  0.0-31.5 sec  4.12 MBytes  1.10 Mbits/sec
------------------------------------------------------------
Client connecting to 10.0.4.4, TCP port 5050
TCP window size: 85.3 KByte (default)
------------------------------------------------------------
[  3] local 10.0.1.51 port 57554 connected with 10.0.4.4 port 5050
[ ID] Interval       Transfer     Bandwidth
[  3]  0.0-31.1 sec  4.25 MBytes  1.14 Mbits/sec
------------------------------------------------------------
Client connecting to 10.0.4.4, TCP port 5050
TCP window size: 85.3 KByte (default)
------------------------------------------------------------
[  3] local 10.0.1.52 port 53938 connected with 10.0.4.4 port 5050
[ ID] Interval       Transfer     Bandwidth
[  3]  0.0-32.7 sec  7.00 MBytes  1.79 Mbits/sec
------------------------------------------------------------
Client connecting to 10.0.4.4, TCP port 5050
TCP window size: 85.3 KByte (default)
------------------------------------------------------------
[  3] local 10.0.1.53 port 58072 connected with 10.0.4.4 port 5050
[ ID] Interval       Transfer     Bandwidth
[  3]  0.0-32.6 sec  5.00 MBytes  1.29 Mbits/sec
------------------------------------------------------------
Client connecting to 10.0.4.4, TCP port 5050
TCP window size: 85.3 KByte (default)
------------------------------------------------------------
[  3] local 10.0.1.54 port 38280 connected with 10.0.4.4 port 5050
[ ID] Interval       Transfer     Bandwidth
[  3]  0.0-34.5 sec  4.50 MBytes  1.09 Mbits/sec
------------------------------------------------------------
Client connecting to 10.0.4.4, TCP port 5050
TCP window size: 85.3 KByte (default)
------------------------------------------------------------
[  3] local 10.0.1.55 port 57034 connected with 10.0.4.4 port 5050
[ ID] Interval       Transfer     Bandwidth
[  3]  0.0-31.4 sec  5.62 MBytes  1.50 Mbits/sec
------------------------------------------------------------
Client connecting to 10.0.4.4, TCP port 5050
TCP window size: 85.3 KByte (default)
------------------------------------------------------------
[  3] local 10.0.1.56 port 51992 connected with 10.0.4.4 port 5050
[ ID] Interval       Transfer     Bandwidth
[  3]  0.0-30.1 sec  5.00 MBytes  1.39 Mbits/sec
------------------------------------------------------------
Client connecting to 10.0.4.4, TCP port 5050
TCP window size: 85.3 KByte (default)
------------------------------------------------------------
[  3] local 10.0.1.57 port 37034 connected with 10.0.4.4 port 5050
[ ID] Interval       Transfer     Bandwidth
[  3]  0.0-31.9 sec  3.88 MBytes  1.02 Mbits/sec
------------------------------------------------------------
Client connecting to 10.0.4.4, TCP port 5050
TCP window size: 85.3 KByte (default)
------------------------------------------------------------
[  3] local 10.0.1.58 port 37054 connected with 10.0.4.4 port 5050
[ ID] Interval       Transfer     Bandwidth
[  3]  0.0-33.8 sec  3.25 MBytes   807 Kbits/sec
------------------------------------------------------------
Client connecting to 10.0.4.4, TCP port 5050
TCP window size: 85.3 KByte (default)
------------------------------------------------------------
[  3] local 10.0.1.59 port 54530 connected with 10.0.4.4 port 5050
[ ID] Interval       Transfer     Bandwidth
[  3]  0.0-31.2 sec  5.50 MBytes  1.48 Mbits/sec
------------------------------------------------------------
Client connecting to 10.0.4.4, TCP port 5050
TCP window size: 85.3 KByte (default)
------------------------------------------------------------
[  3] local 10.0.1.5 port 36946 connected with 10.0.4.4 port 5050
[ ID] Interval       Transfer     Bandwidth
[  3]  0.0-38.6 sec  5.62 MBytes  1.22 Mbits/sec
------------------------------------------------------------
Client connecting to 10.0.4.4, TCP port 5050
TCP window size: 85.3 KByte (default)
------------------------------------------------------------
[  3] local 10.0.1.60 port 35382 connected with 10.0.4.4 port 5050
[ ID] Interval       Transfer     Bandwidth
[  3]  0.0-36.5 sec  5.00 MBytes  1.15 Mbits/sec
------------------------------------------------------------
Client connecting to 10.0.4.4, TCP port 5050
TCP window size: 85.3 KByte (default)
------------------------------------------------------------
[  3] local 10.0.1.61 port 54482 connected with 10.0.4.4 port 5050
[ ID] Interval       Transfer     Bandwidth
[  3]  0.0-36.6 sec  5.62 MBytes  1.29 Mbits/sec
------------------------------------------------------------
Client connecting to 10.0.4.4, TCP port 5050
TCP window size: 85.3 KByte (default)
------------------------------------------------------------
[  3] local 10.0.1.62 port 59730 connected with 10.0.4.4 port 5050
[ ID] Interval       Transfer     Bandwidth
[  3]  0.0-30.4 sec  7.38 MBytes  2.04 Mbits/sec
------------------------------------------------------------
Client connecting to 10.0.4.4, TCP port 5050
TCP window size: 85.3 KByte (default)
------------------------------------------------------------
[  3] local 10.0.1.63 port 42934 connected with 10.0.4.4 port 5050
[ ID] Interval       Transfer     Bandwidth
[  3]  0.0-36.2 sec  6.50 MBytes  1.51 Mbits/sec
------------------------------------------------------------
Client connecting to 10.0.4.4, TCP port 5050
TCP window size: 85.3 KByte (default)
------------------------------------------------------------
[  3] local 10.0.1.64 port 44630 connected with 10.0.4.4 port 5050
[ ID] Interval       Transfer     Bandwidth
[  3]  0.0-32.0 sec  7.88 MBytes  2.07 Mbits/sec
------------------------------------------------------------
Client connecting to 10.0.4.4, TCP port 5050
TCP window size: 85.3 KByte (default)
------------------------------------------------------------
[  3] local 10.0.1.65 port 41912 connected with 10.0.4.4 port 5050
[ ID] Interval       Transfer     Bandwidth
[  3]  0.0-32.5 sec  4.12 MBytes  1.06 Mbits/sec
------------------------------------------------------------
Client connecting to 10.0.4.4, TCP port 5050
TCP window size: 85.3 KByte (default)
------------------------------------------------------------
[  3] local 10.0.1.66 port 52496 connected with 10.0.4.4 port 5050
[ ID] Interval       Transfer     Bandwidth
[  3]  0.0-31.3 sec  3.88 MBytes  1.04 Mbits/sec
------------------------------------------------------------
Client connecting to 10.0.4.4, TCP port 5050
TCP window size: 85.3 KByte (default)
------------------------------------------------------------
[  3] local 10.0.1.67 port 52136 connected with 10.0.4.4 port 5050
[ ID] Interval       Transfer     Bandwidth
[  3]  0.0-31.8 sec  3.50 MBytes   924 Kbits/sec
------------------------------------------------------------
Client connecting to 10.0.4.4, TCP port 5050
TCP window size: 85.3 KByte (default)
------------------------------------------------------------
[  3] local 10.0.1.68 port 35196 connected with 10.0.4.4 port 5050
[ ID] Interval       Transfer     Bandwidth
[  3]  0.0-32.5 sec  3.50 MBytes   903 Kbits/sec
------------------------------------------------------------
Client connecting to 10.0.4.4, TCP port 5050
TCP window size: 85.3 KByte (default)
------------------------------------------------------------
[  3] local 10.0.1.69 port 53694 connected with 10.0.4.4 port 5050
[ ID] Interval       Transfer     Bandwidth
[  3]  0.0-31.8 sec  3.50 MBytes   924 Kbits/sec
------------------------------------------------------------
Client connecting to 10.0.4.4, TCP port 5050
TCP window size: 85.3 KByte (default)
------------------------------------------------------------
[  3] local 10.0.1.6 port 42512 connected with 10.0.4.4 port 5050
[ ID] Interval       Transfer     Bandwidth
[  3]  0.0-31.2 sec  5.38 MBytes  1.44 Mbits/sec
------------------------------------------------------------
Client connecting to 10.0.4.4, TCP port 5050
TCP window size: 85.3 KByte (default)
------------------------------------------------------------
[  3] local 10.0.1.70 port 40310 connected with 10.0.4.4 port 5050
[ ID] Interval       Transfer     Bandwidth
[  3]  0.0-30.2 sec  4.75 MBytes  1.32 Mbits/sec
------------------------------------------------------------
Client connecting to 10.0.4.4, TCP port 5050
TCP window size: 85.3 KByte (default)
------------------------------------------------------------
[  3] local 10.0.1.71 port 35222 connected with 10.0.4.4 port 5050
[ ID] Interval       Transfer     Bandwidth
[  3]  0.0-30.6 sec  4.62 MBytes  1.27 Mbits/sec
------------------------------------------------------------
Client connecting to 10.0.4.4, TCP port 5050
TCP window size: 85.3 KByte (default)
------------------------------------------------------------
[  3] local 10.0.1.72 port 44820 connected with 10.0.4.4 port 5050
[ ID] Interval       Transfer     Bandwidth
[  3]  0.0-32.7 sec  4.50 MBytes  1.15 Mbits/sec
------------------------------------------------------------
Client connecting to 10.0.4.4, TCP port 5050
TCP window size: 85.3 KByte (default)
------------------------------------------------------------
[  3] local 10.0.1.73 port 57282 connected with 10.0.4.4 port 5050
[ ID] Interval       Transfer     Bandwidth
[  3]  0.0-32.1 sec  3.25 MBytes   850 Kbits/sec
------------------------------------------------------------
Client connecting to 10.0.4.4, TCP port 5050
TCP window size: 85.3 KByte (default)
------------------------------------------------------------
[  3] local 10.0.1.74 port 51382 connected with 10.0.4.4 port 5050
[ ID] Interval       Transfer     Bandwidth
[  3]  0.0-37.3 sec  11.4 MBytes  2.56 Mbits/sec
------------------------------------------------------------
Client connecting to 10.0.4.4, TCP port 5050
TCP window size: 85.3 KByte (default)
------------------------------------------------------------
[  3] local 10.0.1.75 port 47462 connected with 10.0.4.4 port 5050
[ ID] Interval       Transfer     Bandwidth
[  3]  0.0-31.2 sec  4.25 MBytes  1.14 Mbits/sec
------------------------------------------------------------
Client connecting to 10.0.4.4, TCP port 5050
TCP window size: 85.3 KByte (default)
------------------------------------------------------------
[  3] local 10.0.1.76 port 40354 connected with 10.0.4.4 port 5050
[ ID] Interval       Transfer     Bandwidth
[  3]  0.0-32.6 sec  3.25 MBytes   837 Kbits/sec
------------------------------------------------------------
Client connecting to 10.0.4.4, TCP port 5050
TCP window size: 85.3 KByte (default)
------------------------------------------------------------
[  3] local 10.0.1.77 port 37344 connected with 10.0.4.4 port 5050
[ ID] Interval       Transfer     Bandwidth
[  3]  0.0-31.3 sec  5.50 MBytes  1.47 Mbits/sec
------------------------------------------------------------
Client connecting to 10.0.4.4, TCP port 5050
TCP window size: 85.3 KByte (default)
------------------------------------------------------------
[  3] local 10.0.1.78 port 47374 connected with 10.0.4.4 port 5050
[ ID] Interval       Transfer     Bandwidth
[  3]  0.0-30.9 sec  10.4 MBytes  2.82 Mbits/sec
------------------------------------------------------------
Client connecting to 10.0.4.4, TCP port 5050
TCP window size: 85.3 KByte (default)
------------------------------------------------------------
[  3] local 10.0.1.79 port 57592 connected with 10.0.4.4 port 5050
[ ID] Interval       Transfer     Bandwidth
[  3]  0.0-31.1 sec  6.12 MBytes  1.65 Mbits/sec
------------------------------------------------------------
Client connecting to 10.0.4.4, TCP port 5050
TCP window size: 85.3 KByte (default)
------------------------------------------------------------
[  3] local 10.0.1.7 port 45732 connected with 10.0.4.4 port 5050
[ ID] Interval       Transfer     Bandwidth
[  3]  0.0-31.8 sec  4.12 MBytes  1.09 Mbits/sec
------------------------------------------------------------
Client connecting to 10.0.4.4, TCP port 5050
TCP window size: 85.3 KByte (default)
------------------------------------------------------------
[  3] local 10.0.1.80 port 51544 connected with 10.0.4.4 port 5050
[ ID] Interval       Transfer     Bandwidth
[  3]  0.0-31.7 sec  7.38 MBytes  1.95 Mbits/sec
------------------------------------------------------------
Client connecting to 10.0.4.4, TCP port 5050
TCP window size: 85.3 KByte (default)
------------------------------------------------------------
[  3] local 10.0.1.8 port 44258 connected with 10.0.4.4 port 5050
[ ID] Interval       Transfer     Bandwidth
[  3]  0.0-35.4 sec  3.88 MBytes   918 Kbits/sec
------------------------------------------------------------
Client connecting to 10.0.4.4, TCP port 5050
TCP window size: 85.3 KByte (default)
------------------------------------------------------------
[  3] local 10.0.1.9 port 37852 connected with 10.0.4.4 port 5050
[ ID] Interval       Transfer     Bandwidth
[  3]  0.0-32.5 sec  7.62 MBytes  1.97 Mbits/sec'''

# Extracting Transfer and Bandwidth data
transfer_data = re.findall(r"(\d+\.?\d*)\s(MBytes|KBytes)", log)
bandwidth_data = re.findall(r"(\d+\.?\d*)\s(Mbits/sec|Kbits/sec)", log)

# Convert all to MBytes and Mbits/sec
transfers_in_mbytes = [float(val) if unit == "MBytes" else float(val) / 1000 for val, unit in transfer_data]
bandwidths_in_mbits = [float(val) if unit == "Mbits/sec" else float(val) / 1000 for val, unit in bandwidth_data]

# Create the addition string for both transfer and bandwidth
transfer_addition_str = " + ".join(map(str, transfers_in_mbytes))
bandwidth_addition_str = " + ".join(map(str, bandwidths_in_mbits))

# Calculate total and average
total_transfer = sum(transfers_in_mbytes)
total_bandwidth = sum(bandwidths_in_mbits)
print(len(transfers_in_mbytes))
average_transfer = total_transfer / len(transfers_in_mbytes)
average_bandwidth = total_bandwidth / len(bandwidths_in_mbits)

# Prepare result
result = {
    "Transfer Addition String": transfer_addition_str,
    "Total Transfer (MBytes)": total_transfer,
    "Average Transfer (MBytes)": average_transfer,
    "Bandwidth Addition String": bandwidth_addition_str,
    "Total Bandwidth (Mbits/sec)": total_bandwidth,
    "Average Bandwidth (Mbits/sec)": average_bandwidth,
}

print(result)