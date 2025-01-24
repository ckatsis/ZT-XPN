package org.p4sdn.app.pipeline.components;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.p4sdn.app.exceptions.AnnotationComponentException;
import org.p4sdn.app.exceptions.AnnotationException;

public class Annotation extends Component{

    public static final ComponentType type = ComponentType.ANNOTATION;
    public static final String CONTROL_PLANE_ANNOTATION = "controller_header";
    public static final String ANNOTATION_SYMBOL = "@";
    public static final String[] allowedAnnotationTypes  = {CONTROL_PLANE_ANNOTATION};
    public static Map<String, Set<Class<?>>> allowedAnnotationMap = new HashMap<>();
    
    static {
        // Header annotations
        Set<Class<?>> set = new HashSet<>();
        set.add(Header.class);
        allowedAnnotationMap.put(CONTROL_PLANE_ANNOTATION, set);
    }

    private Component associatedComponent;
    private String annotationType;

    public Annotation(String name, String annotationType, Component associatedComponent) 
        throws AnnotationException, AnnotationComponentException {
        super(name, type);
        boolean isValid = false;

        for(String type: allowedAnnotationTypes){
            if(type.equals(annotationType)){
                isValid = true;
                break;
            }
        }

        if(isValid) {
            this.annotationType = annotationType;
        } else
            throw new AnnotationException(annotationType);

        isValid = false;
        Set<Class<?>> allowedClasses  = allowedAnnotationMap.get(annotationType);

        if (allowedClasses != null) {
            if(allowedClasses.contains(associatedComponent.getClass())) {
                isValid = true;
            }
        }

        if (isValid) {
            this.associatedComponent = associatedComponent;
        } else
            throw new AnnotationComponentException(annotationType, associatedComponent.getClass());
    }

    public Component getAssociatedComponent() {
        return this.associatedComponent;
    }
    

    @Override
    public String toString() {
        StringBuilder annotaBuilder = new StringBuilder();
        annotaBuilder.append(generateIndentation());
        annotaBuilder.append(ANNOTATION_SYMBOL + annotationType + LEFT_PAR + QUOTE + getName() + QUOTE + RIGHT_PAR);
        return annotaBuilder.toString();
    }



    @Override
    public String compile() {
        return toString();
    }
    
}
