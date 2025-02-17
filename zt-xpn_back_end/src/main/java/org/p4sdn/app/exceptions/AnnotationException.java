/**
 * ZT-XPN: An end-to-end Zero-Trust Architecture for Next Generation 
 * Programmable Networks
 * 
 * Authors:  Charalampos Katsis  (ckatsis@purdue.edu)
 *           Elisa Bertino       (bertino@purdue.edu)
 * =================================================================
 */


package org.p4sdn.app.exceptions;

public class AnnotationException extends Exception {

    public AnnotationException(String ann) {
        super("Annotation [" + ann + "] is not a supported P4 annotation");
    }
    
}
