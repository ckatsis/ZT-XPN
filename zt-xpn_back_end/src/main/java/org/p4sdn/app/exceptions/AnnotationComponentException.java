/**
 * ZT-XPN: An end-to-end Zero-Trust Architecture for Next Generation 
 * Programmable Networks
 * 
 * Authors:  Charalampos Katsis  (ckatsis@purdue.edu)
 *           Elisa Bertino       (bertino@purdue.edu)
 * =================================================================
 */


package org.p4sdn.app.exceptions;


public class AnnotationComponentException extends Exception {

    public AnnotationComponentException(String annotation, Class<?> componentClass) {
        super("Annotation [" + annotation + "] cannot be assinged to component [" + componentClass.getSimpleName() + "]" );
    }
}
