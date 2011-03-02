package org.asoem.greyfish.core.utils;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Can be used to annotate constructors which will be used at the xml deserialization process
 * from the simpleframework
 * User: christoph
 * Date: 02.03.11
 * Time: 15:03
 */
@Target( ElementType.CONSTRUCTOR )
@Retention( RetentionPolicy.SOURCE )
public @interface SimpleXMLConstructor {
}
