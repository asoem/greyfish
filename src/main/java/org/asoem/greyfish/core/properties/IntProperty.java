package org.asoem.greyfish.core.properties;

import org.asoem.greyfish.lang.ClassGroup;
import org.asoem.greyfish.utils.AbstractDeepCloneable;
import org.asoem.greyfish.utils.Exporter;

import java.util.Map;

@ClassGroup(tags="property")
public class IntProperty extends OrderedSetProperty<Integer> {

    private IntProperty() {
        this(new Builder());
    }

	@Override
	protected AbstractDeepCloneable deepCloneHelper(
			Map<AbstractDeepCloneable, AbstractDeepCloneable> mapDict) {
		return new Builder().fromClone(this, mapDict).build();
	}
	
	@Override
	public void export(Exporter e) {
		super.export(e, Integer.class);
	}

	public void subtract(int val) {
		setValue(value - val);
	}
	
	public void add(int val) {
		setValue(value + val);
	}

    protected IntProperty(AbstractBuilder<? extends AbstractBuilder> builder) {
        super(builder);
    }

    public static final class Builder extends AbstractBuilder<Builder> {
        @Override protected Builder self() {  return this; }
    }

    protected static abstract class AbstractBuilder<T extends AbstractBuilder<T>> extends OrderedSetProperty.AbstractBuilder<T, Integer> {
        protected AbstractBuilder() {
            lowerBound(0).upperBound(100).initialValue(50);
        }

        protected T fromClone(IntProperty property, Map<AbstractDeepCloneable, AbstractDeepCloneable> mapDict) {
            super.fromClone(property, mapDict);
            return self();
        }

        public IntProperty build() { return new IntProperty(this); }
    }
}
