package org.asoem.greyfish.core.conditions;

import com.google.common.collect.Iterables;
import org.asoem.greyfish.core.properties.IntProperty;
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.utils.base.DeepCloner;
import org.asoem.greyfish.utils.gui.ConfigurationHandler;
import org.asoem.greyfish.utils.gui.SetAdaptor;
import org.simpleframework.xml.Element;

import static com.google.common.base.Preconditions.checkNotNull;

public final class IntPropertyCondition extends IntCompareCondition {

    @Element(name="property")
    private IntProperty intProperty;

    protected IntPropertyCondition(IntPropertyCondition condition, DeepCloner map) {
        super(condition, map);
        this.intProperty = map.cloneField(condition.intProperty, IntProperty.class);
    }

    @Override
    public void configure(ConfigurationHandler e) {
        super.configure(e);
        e.add("", new SetAdaptor<IntProperty>(IntProperty.class
        ) {

            @Override
            protected void set(IntProperty arg0) {
                intProperty = checkNotNull(arg0);
            }

            @Override
            public IntProperty get() {
                return intProperty;
            }

            @Override
            public Iterable<IntProperty> values() {
                return Iterables.filter(agent().getProperties(), IntProperty.class);
            }
        });
    }

    @Override
    protected Integer getCompareValue(Simulation simulation) {
        return intProperty.get();
    }

    @Override
    public IntPropertyCondition deepClone(DeepCloner cloner) {
        return new IntPropertyCondition(this, cloner);
    }

    private IntPropertyCondition() {
        this(new Builder());
    }

    protected IntPropertyCondition(AbstractBuilder<?,?> builder) {
        super(builder);
        this.intProperty = builder.intProperty;
    }

    public static Builder trueIf() { return new Builder(); }

    public static final class Builder extends AbstractBuilder<IntPropertyCondition, Builder> {
        @Override protected Builder self() { return this; }
        @Override public IntPropertyCondition checkedBuild() { return new IntPropertyCondition(this); }
    }

    protected static abstract class AbstractBuilder<E extends IntPropertyCondition, T extends AbstractBuilder<E,T>> extends IntCompareCondition.AbstractBuilder<E,T> {
        private IntProperty intProperty;

        public T valueOf(IntProperty intProperty) { this.intProperty = checkNotNull(intProperty); return self(); }
    }
}
