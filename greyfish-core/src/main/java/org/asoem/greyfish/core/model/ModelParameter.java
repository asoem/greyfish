package org.asoem.greyfish.core.model;

import com.google.inject.BindingAnnotation;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@BindingAnnotation
@Target({ FIELD }) @Retention(RUNTIME)
public @interface ModelParameter {
    String value() default "";
    boolean optional() default true;
}
