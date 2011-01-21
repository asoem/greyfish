package org.asoem.greyfish.core.properties;

import org.asoem.greyfish.utils.AbstractDeepCloneable;

import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

public abstract class AbstractDiscreteProperty<E> extends AbstractGFProperty implements DiscreteProperty<E> {

	protected E value;

    @SuppressWarnings("unused")
	private AbstractDiscreteProperty() {
        this(new Builder<E>());
	}

	@Override
	public E getValue() {
		return value;
	}

    protected AbstractDiscreteProperty(AbstractBuilder<? extends AbstractBuilder, E> builder) {
        super(builder);
    }

    public static final class Builder<E> extends AbstractBuilder<Builder<E>, E> {
        @Override protected Builder<E> self() {  return this; }
    }

    protected static abstract class AbstractBuilder<T extends AbstractBuilder<T, E>, E> extends AbstractGFProperty.AbstractBuilder<T> {
        protected E value;

        public T value(E upperBound) { this.value = checkNotNull(upperBound); return self(); }

        protected T fromClone(AbstractDiscreteProperty<E> property, Map<AbstractDeepCloneable, AbstractDeepCloneable> mapDict) {
            super.fromClone(property, mapDict).
                    value(property.value);
            return self();
        }
    }
}
