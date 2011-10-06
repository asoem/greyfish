package org.asoem.greyfish.core.individual;

public interface PrototypeRegistryListener {

	public void prototypeAdded(PrototypeManager source, Agent prototype, int index);
	public void prototypeRemoved(PrototypeManager source, Agent prototype, int index);
}
