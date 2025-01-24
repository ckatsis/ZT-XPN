package org.p4sdn.app.exceptions;

public class AnnotationException extends Exception {

    public AnnotationException(String ann) {
        super("Annotation [" + ann + "] is not a supported P4 annotation");
    }
    
}
