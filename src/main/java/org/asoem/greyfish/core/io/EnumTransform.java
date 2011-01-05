package org.asoem.sico.core.io;

import org.simpleframework.xml.transform.Transform;

@SuppressWarnings("rawtypes")
public class EnumTransform implements Transform<Enum> {
	
	private final Class type;
	
	public EnumTransform(Class type) {
		this.type = type;
	}
	
	@SuppressWarnings("unchecked")
	public Enum read(String value) throws Exception {
		return Enum.valueOf(type, value);
	}
	
	public String write(Enum value) throws Exception {
		return value.name();
	}
}
