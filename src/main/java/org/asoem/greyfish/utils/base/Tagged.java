package org.asoem.greyfish.utils.base;

import java.lang.annotation.*;

@Target( {ElementType.TYPE, ElementType.METHOD, ElementType.FIELD} )
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Tagged {
	
	String[] value();
}
