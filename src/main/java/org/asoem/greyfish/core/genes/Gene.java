package org.asoem.greyfish.core.genes;

import org.asoem.greyfish.lang.HasName;
import org.asoem.greyfish.utils.DeepCloneable;


public interface Gene<T> extends DeepCloneable, HasName {
	public T getRepresentation();
	public void setRepresentation(Object object, Class<T> clazz);
	
	public void mutate();
	public void initialize();
}
