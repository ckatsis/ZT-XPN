package org.p4sdn.app.exceptions;


public class AnnotationComponentException extends Exception {

    public AnnotationComponentException(String annotation, Class<?> componentClass) {
        super("Annotation [" + annotation + "] cannot be assinged to component [" + componentClass.getSimpleName() + "]" );
    }
}
