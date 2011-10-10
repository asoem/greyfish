package org.asoem.greyfish.core.properties;

import org.asoem.greyfish.lang.ClassGroup;
import org.asoem.greyfish.utils.ConfigurationHandler;
import org.asoem.greyfish.utils.DeepCloner;

@ClassGroup(tags="property")
public class IntProperty extends AbstractWellOrderedSetElementProperty<Integer> {

    private IntProperty() {
        this(new Builder());
    }

    protected IntProperty(IntProperty intProperty, DeepCloner cloner) {
        super(intProperty, cloner);
    }

    @Override
    public IntProperty deepClone(DeepCloner cloner) {
        return new IntProperty(this, cloner);
    }

    @Override
	public void configure(ConfigurationHandler e) {
		super.configure(e, Integer.class);
	}

	public void subtract(int val) {
		setValue(value - val);
	}
	
	public void add(int val) {
		setValue(value + val);
	}

    protected IntProperty(AbstractBuilder<?,?> builder) {
        super(builder);
    }

    public static Builder with() { return new Builder(); }
    public static final class Builder extends AbstractBuilder<IntProperty, Builder>  {
        public Builder() {lowerBound(0).upperBound(0).initialValue(0);}
        @Override protected Builder self() { return this; }
        @Override public IntProperty checkedBuild() { return new IntProperty(this); }
    }

    protected static abstract class AbstractBuilder<E extends IntProperty, T extends AbstractBuilder<E,T>> extends AbstractWellOrderedSetElementProperty.AbstractBuilder<E, T, Integer> {}
}
