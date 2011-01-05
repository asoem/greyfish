package org.asoem.sico.utils;

import java.util.HashMap;
import java.util.Map;

import com.google.common.base.Preconditions;

public abstract class AbstractDeepCloneable implements DeepClonable {
	
	protected AbstractDeepCloneable() { }

	protected AbstractDeepCloneable(AbstractDeepCloneable srcObj,
			Map<AbstractDeepCloneable, AbstractDeepCloneable> mapDict) {
		Preconditions.checkNotNull(srcObj);
		Preconditions.checkNotNull(mapDict);
		
		mapDict.put(srcObj, this);
	}

	final public DeepClonable deepClone() {
		return deepClone(new HashMap<AbstractDeepCloneable, AbstractDeepCloneable>());
	}

	protected DeepClonable deepClone(Map<AbstractDeepCloneable, AbstractDeepCloneable> mapDict) {
		Preconditions.checkNotNull(mapDict);

		return mapDict.containsKey(this) ? mapDict.get(this) : deepCloneHelper(mapDict);
	}

	///Classes should override this method
	///with the following code:
	/// return new NameOfMyClass(this, mapDict);
	protected abstract AbstractDeepCloneable deepCloneHelper(
			Map<AbstractDeepCloneable, AbstractDeepCloneable> mapDict);
	
	@SuppressWarnings("unchecked")
	protected static <T extends DeepClonable> T deepClone(T component, Map<AbstractDeepCloneable, AbstractDeepCloneable> map) {
		// There must not exist any implementation of DeepClonable which doesn't extend DeepCloneable
		return (component != null) ? (T) ((AbstractDeepCloneable)component).deepClone(map) : null;
	}
}
