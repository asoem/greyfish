package org.asoem.greyfish.core.conditions;

import org.asoem.greyfish.core.properties.IntProperty;
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.lang.BuilderInterface;
import org.asoem.greyfish.utils.Exporter;
import org.asoem.greyfish.utils.ValueSelectionAdaptor;
import org.simpleframework.xml.Element;

import static com.google.common.base.Preconditions.checkNotNull;

public final class IntPropertyCondition extends IntCompareCondition {

    @Element(name="property")
    private IntProperty intProperty;

    protected IntPropertyCondition(IntPropertyCondition condition, CloneMap map) {
        super(condition, map);
        this.intProperty = deepClone(condition.intProperty, map);
    }

    @Override
    public void export(Exporter e) {
        super.export(e);
        e.addField(new ValueSelectionAdaptor<IntProperty>("", IntProperty.class, intProperty, getComponentOwner().getProperties(IntProperty.class)) {

            @Override
            protected void writeThrough(IntProperty arg0) {
                intProperty = checkFrozen(checkNotNull(arg0));
            }
        });
    }

    @Override
    protected Integer getCompareValue(Simulation simulation) {
        return intProperty.getValue();
    }

    @Override
    protected IntPropertyCondition deepCloneHelper(CloneMap map) {
        return new IntPropertyCondition(this, map);
    }

    private IntPropertyCondition() {
        this(new Builder());
    }

    protected IntPropertyCondition(AbstractBuilder<? extends AbstractBuilder> builder) {
        super(builder);
        this.intProperty = builder.intProperty;
    }

    public static Builder trueIf() { return new Builder(); }
    public static final class Builder extends AbstractBuilder<Builder> implements BuilderInterface<IntPropertyCondition> {
        private Builder() {};
        @Override protected Builder self() { return this; }
        @Override public IntPropertyCondition build() { return new IntPropertyCondition(this); }
    }

    protected static abstract class AbstractBuilder<T extends AbstractBuilder<T>> extends IntCompareCondition.AbstractBuilder<T> {
        private IntProperty intProperty;

        public T valueOf(IntProperty intProperty) { this.intProperty = checkNotNull(intProperty); return self(); }
    }
}
