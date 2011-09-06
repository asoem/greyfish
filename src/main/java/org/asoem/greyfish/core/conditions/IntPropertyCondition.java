package org.asoem.greyfish.core.conditions;

import com.google.common.collect.Iterables;
import org.asoem.greyfish.core.actions.ActionContext;
import org.asoem.greyfish.core.properties.IntProperty;
import org.asoem.greyfish.lang.BuilderInterface;
import org.asoem.greyfish.utils.CloneMap;
import org.asoem.greyfish.utils.ConfigurationHandler;
import org.asoem.greyfish.utils.FiniteSetValueAdaptor;
import org.simpleframework.xml.Element;

import static com.google.common.base.Preconditions.checkNotNull;

public final class IntPropertyCondition extends IntCompareCondition {

    @Element(name="property")
    private IntProperty intProperty;

    protected IntPropertyCondition(IntPropertyCondition condition, CloneMap map) {
        super(condition, map);
        this.intProperty = map.clone(condition.intProperty, IntProperty.class);
    }

    @Override
    public void configure(ConfigurationHandler e) {
        super.configure(e);
        e.add(new FiniteSetValueAdaptor<IntProperty>("", IntProperty.class
        ) {

            @Override
            protected void set(IntProperty arg0) {
                intProperty = checkFrozen(checkNotNull(arg0));
            }

            @Override
            public IntProperty get() {
                return intProperty;
            }

            @Override
            public Iterable<IntProperty> values() {
                return Iterables.filter(getComponentOwner().getProperties(), IntProperty.class);
            }
        });
    }

    @Override
    protected Integer getCompareValue(ActionContext context) {
        return intProperty.get();
    }

    @Override
    public IntPropertyCondition deepCloneHelper(CloneMap map) {
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
        private Builder() {}

        @Override protected Builder self() { return this; }
        @Override public IntPropertyCondition build() { return new IntPropertyCondition(this); }
    }

    protected static abstract class AbstractBuilder<T extends AbstractBuilder<T>> extends IntCompareCondition.AbstractBuilder<T> {
        private IntProperty intProperty;

        public T valueOf(IntProperty intProperty) { this.intProperty = checkNotNull(intProperty); return self(); }
    }
}
