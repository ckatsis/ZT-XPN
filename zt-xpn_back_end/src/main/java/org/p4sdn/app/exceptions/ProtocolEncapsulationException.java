/**
 * ZT-XPN: An end-to-end Zero-Trust Architecture for Next Generation 
 * Programmable Networks
 * 
 * Authors:  Charalampos Katsis  (ckatsis@purdue.edu)
 *           Elisa Bertino       (bertino@purdue.edu)
 * =================================================================
 */


package org.p4sdn.app.exceptions;

public class ProtocolEncapsulationException extends Exception {

    public ProtocolEncapsulationException(String child, String parrent) {
        super("Protocol header [" + child + "] cannot be contained within [" + parrent + "] header");
    }
}