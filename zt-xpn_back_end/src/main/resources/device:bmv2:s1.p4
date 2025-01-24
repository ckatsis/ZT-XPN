#include<core.p4>
#include<v1model.p4>
const bit<9> CPU_PORT = 255;
@controller_header ( "packet_in" ) 
header cpu_in_header_t { 
	bit<9> ingress_port;
	bit<7> padding;
 } 
@controller_header ( "packet_out" ) 
header cpu_out_header_t { 
	bit<9> egress_port;
	bit<7> padding;
 } 

header ethernet_t { 
	bit<48> destinationMAC;
	bit<48> sourceMAC;
	bit<16> etherType;
 } 

header ip_t { 
	bit<4> version;
	bit<4> ihl;
	bit<6> dscp;
	bit<2> ecn;
	bit<16> totallen;
	bit<16> identification;
	bit<3> flags;
	bit<13> fragoff;
	bit<8> ttl;
	bit<8> proto;
	bit<16> hdr_checksum;
	bit<32> sourceAddress;
	bit<32> destinationAddress;
 } 

header tcp_t { 
	bit<16> sourcePort;
	bit<16> destinationPort;
	bit<32> sequenceNo;
	bit<32> ack;
	bit<4> dataOffset;
	bit<3> res;
	bit<3> ecn;
	bit<6> ctrl;
	bit<16> windowSize;
	bit<16> checksum;
	bit<16> urgentPointer;
 } 
struct parsed_headers_t { 
	cpu_in_header_t cpu_in_header;
	cpu_out_header_t cpu_out_header;
	ethernet_t ethernet;
	ip_t ip;
	tcp_t tcp;
 } 
struct local_metadata_t { 
 } 
parser ParserImpl ( packet_in packet, out parsed_headers_t parsed_headers, inout local_metadata_t local_metadata, inout standard_metadata_t standard_metadata )  { 
	state start { 
		transition select  ( standard_metadata.ingress_port )  { 
			CPU_PORT : parse_packet_out;
			default : parse_ethernet;
		 } 
	 } 
	state parse_packet_out { 
		packet.extract ( parsed_headers.cpu_out_header ) ;
		transition parse_ethernet;
	 } 
	state error_unsupported_encapsulation { 
		verify ( false, error.NoMatch ) ;
		transition accept;
	 } 
	state parse_ethernet { 
		packet.extract ( parsed_headers.ethernet ) ;
		transition select  ( parsed_headers.ethernet.etherType )  { 
			0x88cc : accept;
			0x8942 : accept;
			0x800 : parse_ip;
			0x806 : accept;
			default : error_unsupported_encapsulation;
		 } 
	 } 
	state parse_ip { 
		packet.extract ( parsed_headers.ip ) ;
		transition select  ( parsed_headers.ip.proto )  { 
			6 : parse_tcp;
			default : error_unsupported_encapsulation;
		 } 
	 } 
	state parse_tcp { 
		packet.extract ( parsed_headers.tcp ) ;
		transition accept;
	 } 
 } 
control IngressCheckSumCheck ( inout parsed_headers_t parsed_headers, inout local_metadata_t local_metadata )  { 
	apply  { 
	 } 
 } 
control IngressControl ( inout parsed_headers_t parsed_headers, inout local_metadata_t local_metadata, inout standard_metadata_t standard_metadata )  { 
	action dropPacket (  )  { 
		mark_to_drop ( standard_metadata ) ;
	 } 
	action send_to_controller (  )  { 
		standard_metadata.egress_spec = CPU_PORT;
		parsed_headers.cpu_in_header.setValid (  ) ;
		parsed_headers.cpu_in_header.ingress_port = standard_metadata.ingress_port;
	 } 
	action set_out_port ( bit<9> out_port )  { 
		standard_metadata.egress_spec = out_port;
	 } 
	direct_counter ( CounterType.packets_and_bytes ) acl_t_flow_counter;
	table acl_t { 
		key =  { 
			parsed_headers.ethernet.etherType : ternary;
			parsed_headers.ip.destinationAddress : ternary;
			parsed_headers.ip.proto : ternary;
			parsed_headers.ip.sourceAddress : ternary;
			parsed_headers.tcp.destinationPort : ternary;
		 } 
		actions =  { 
			dropPacket;
			send_to_controller;
			set_out_port;
		 } 
		default_action = dropPacket (  ) ;
		counters = acl_t_flow_counter;
	 } 
	apply  { 
		if  ( standard_metadata.parser_error != error.NoError )  { 
			dropPacket (  ) ;
			return;
		 } 

		if  ( parsed_headers.cpu_out_header.isValid (  )  )  { 
			set_out_port ( parsed_headers.cpu_out_header.egress_port ) ;
			parsed_headers.cpu_out_header.setInvalid (  ) ;
			return;
		 } 

		acl_t.apply (  ) ;

	 } 
 } 
control EgressControl ( inout parsed_headers_t parsed_headers, inout local_metadata_t local_metadata, inout standard_metadata_t standard_metadata )  { 
	apply  { 
	 } 
 } 
control EgressComputeChecksum ( inout parsed_headers_t parsed_headers, inout local_metadata_t local_metadata )  { 
	apply  { 
	 } 
 } 
control DeparserImpl ( packet_out packet, in parsed_headers_t parsed_headers )  { 
	apply  { 
		packet.emit ( parsed_headers.cpu_in_header ) ;
		packet.emit ( parsed_headers.cpu_out_header ) ;
		packet.emit ( parsed_headers.ethernet ) ;
		packet.emit ( parsed_headers.ip ) ;
		packet.emit ( parsed_headers.tcp ) ;

	 } 
 } 

V1Switch ( 
ParserImpl (  ) , 
IngressCheckSumCheck (  ) , 
IngressControl (  ) , 
EgressControl (  ) , 
EgressComputeChecksum (  ) , 
DeparserImpl (  ) 
 )  main;