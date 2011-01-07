package org.asoem.greyfish.core.individual;

public interface PrototypeRegistryListener {

	public void prototypeAdded(PrototypeManager source, Individual prototype, int index);
	public void prototypeRemoved(PrototypeManager source, Individual prototype, int index);
}
