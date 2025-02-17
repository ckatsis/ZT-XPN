/**
 * ZT-XPN: An end-to-end Zero-Trust Architecture for Next Generation 
 * Programmable Networks
 * 
 * Authors:  Charalampos Katsis  (ckatsis@purdue.edu)
 *           Elisa Bertino       (bertino@purdue.edu)
 * =================================================================
 */


package org.p4sdn.app.pipeline.components;

import org.p4sdn.app.exceptions.AnnotationComponentException;
import org.p4sdn.app.exceptions.AnnotationException;

public final class ControllerHeader extends Header {

    public static final String PACKET_IN_ANNOTATION = "packet_in";
    public static final String PACKET_OUT_ANNOTATION = "packet_out";
    public static final String PACKET_IN_HDR = "cpu_in_header";
    public static final String PACKET_OUT_HDR = "cpu_out_header";
    public static final String PACKET_IN_INGRESS_PORT = "ingress_port";
    public static final String PACKET_OUT_EGRESS_PORT = "egress_port";

    private ControllerHeader(String name) {
        super(name);
    }

    public static Header generatePacketInHeader() {
        Header pcktInHeader = new Header(PACKET_IN_HDR);
        Variable portNum = new Variable(PACKET_IN_INGRESS_PORT, 9);
        pcktInHeader.addVariable(portNum);
        Variable padding = new Variable("padding", 7);
        pcktInHeader.addVariable(padding);

        try {
            Annotation annotation = new Annotation(PACKET_IN_ANNOTATION, Annotation.CONTROL_PLANE_ANNOTATION, pcktInHeader);
            pcktInHeader.addAnnotation(annotation);
        } catch (AnnotationException e) {
            pcktInHeader = null;
            e.printStackTrace();
        } catch (AnnotationComponentException e) {
            pcktInHeader = null;
            e.printStackTrace();
        }

        return pcktInHeader;
    }

    public static Header generatePacketOutHeader() {
        Header pcktOutHeader = new Header(PACKET_OUT_HDR);
        Variable portNum = new Variable(PACKET_OUT_EGRESS_PORT, 9);
        pcktOutHeader.addVariable(portNum);
        Variable padding = new Variable("padding", 7);
        pcktOutHeader.addVariable(padding);

        try {
            Annotation annotation = new Annotation(PACKET_OUT_ANNOTATION, Annotation.CONTROL_PLANE_ANNOTATION, pcktOutHeader);
            pcktOutHeader.addAnnotation(annotation);
        } catch (AnnotationException e) {
            pcktOutHeader = null;
            e.printStackTrace();
        } catch (AnnotationComponentException e) {
            pcktOutHeader = null;
            e.printStackTrace();
        }

        return pcktOutHeader;
    }
}
