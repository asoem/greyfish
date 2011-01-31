package org.asoem.greyfish.core.individual;

public interface PrototypeRegistryListener {

	public void prototypeAdded(PrototypeManager source, Prototype prototype, int index);
	public void prototypeRemoved(PrototypeManager source, Prototype prototype, int index);
}
