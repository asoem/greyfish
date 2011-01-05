package org.asoem.sico.utils;

import org.asoem.sico.lang.Property;

import com.google.common.base.Predicates;



public class BeanUtils {

	public static <T> Property<T> createPropertyNotNull(T value) {
		return new Property<T>(value,  Predicates.notNull() );
	}
}
