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
struct parsed_headers_t { 
	cpu_in_header_t cpu_in_header;
	cpu_out_header_t cpu_out_header;
	ethernet_t ethernet;
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
	state parse_ethernet { 
		packet.extract ( parsed_headers.ethernet ) ;
		transition select  ( parsed_headers.ethernet.etherType )  { 
			0x88cc : accept;
			0x8942 : accept;
			0x806 : accept;
			default : accept;
		 } 
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
	direct_counter ( CounterType.packets_and_bytes ) l2_fwd_t_flow_counter;
	table l2_fwd_t { 
		key =  { 
			parsed_headers.ethernet.etherType : ternary;
			parsed_headers.ethernet.destinationMAC : ternary;
		 } 
		actions =  { 
			dropPacket;
			send_to_controller;
			set_out_port;
		 } 
		default_action = dropPacket (  ) ;
		counters = l2_fwd_t_flow_counter;
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

		l2_fwd_t.apply (  ) ;

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