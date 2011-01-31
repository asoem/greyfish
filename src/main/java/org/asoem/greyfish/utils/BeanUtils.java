package org.asoem.greyfish.utils;

import com.google.common.base.Predicates;
import org.asoem.greyfish.lang.Property;



public class BeanUtils {

	public static <T> Property<T> createPropertyNotNull(T value) {
		return new Property<T>(value,  Predicates.notNull() );
	}
}
