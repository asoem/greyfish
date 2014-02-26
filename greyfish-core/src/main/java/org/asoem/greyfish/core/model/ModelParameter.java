package org.asoem.greyfish.core.model;

import com.google.inject.BindingAnnotation;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Retention(RUNTIME)
@Target({FIELD, PARAMETER, METHOD})
@BindingAnnotation
public @interface ModelParameter {
    String value() default "";

    @Deprecated boolean optional() default true;
}
