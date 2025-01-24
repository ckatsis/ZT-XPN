package org.p4sdn.app.exceptions;

public class ProtocolEncapsulationException extends Exception {

    public ProtocolEncapsulationException(String child, String parrent) {
        super("Protocol header [" + child + "] cannot be contained within [" + parrent + "] header");
    }
}