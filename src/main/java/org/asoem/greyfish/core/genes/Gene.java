package org.asoem.greyfish.core.genes;

import org.asoem.greyfish.lang.HasName;



public interface Gene<T> extends Cloneable, HasName {
	public T getRepresentation();
	public void setRepresentation(Object object);
	
	public void mutate();
	public void initialize();
	
	public Gene<T> clone() throws CloneNotSupportedException;
}
