package org.asoem.greyfish.core.properties;

import org.asoem.greyfish.lang.BuilderInterface;
import org.asoem.greyfish.lang.ClassGroup;
import org.asoem.greyfish.utils.CloneMap;
import org.asoem.greyfish.utils.Exporter;

@ClassGroup(tags="property")
public class IntProperty extends OrderedSetProperty<Integer> {

    private IntProperty() {
        this(new Builder());
    }

    protected IntProperty(IntProperty intProperty, CloneMap cloneMap) {
        super(intProperty, cloneMap);
    }

    @Override
    public IntProperty deepCloneHelper(CloneMap cloneMap) {
        return new IntProperty(this, cloneMap);
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

    public static Builder with() { return new Builder(); }
    public static final class Builder extends AbstractBuilder<Builder> implements BuilderInterface<IntProperty> {
        private Builder() {lowerBound(0).upperBound(0).initialValue(0);}
        @Override protected Builder self() { return this; }
        @Override public IntProperty build() { return new IntProperty(this); }
    }

    protected static abstract class AbstractBuilder<T extends AbstractBuilder<T>> extends OrderedSetProperty.AbstractBuilder<T, Integer> {}
}
