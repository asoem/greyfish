package org.asoem.sico.core.genes;

import org.asoem.sico.lang.HasName;



public interface Gene<T> extends Cloneable, HasName {
	public T getRepresentation();
	public void setRepresentation(Object object);
	
	public void mutate();
	public void initialize();
	
	public Gene<T> clone() throws CloneNotSupportedException;
}
