package org.asoem.greyfish.core.traits;

import com.google.common.reflect.TypeToken;
import org.asoem.greyfish.utils.base.TypedSupplier;

import javax.annotation.Nullable;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * User: christoph
 * Date: 25.04.12
 * Time: 15:30
 */
public class TraitVector<T> implements TypedSupplier<T> {
    @Nullable
    private final T value;
    private final double recombinationProbability;
    private final TypeToken<T> typeToken;
    private final String name;

    private TraitVector(@Nullable final T value, final double recombinationProbability, final TypeToken<T> typeToken, final String name) {
        this.value = value;
        this.recombinationProbability = recombinationProbability;
        this.typeToken = typeToken;
        this.name = name;
    }

    @Nullable
    @Override
    public T get() {
        return value;
    }

    public double getRecombinationProbability() {
        return recombinationProbability;
    }

    @Override
    public TypeToken<T> getValueType() {
        return typeToken;
    }

    public static <T> TraitVector<T> create(@Nullable final T value, final double recombinationProbability, final TypeToken<T> typeToken, final String name) {
        checkNotNull(typeToken);
        checkNotNull(name);
        return new TraitVector<T>(value, recombinationProbability, typeToken, name);
    }

    public String getName() {
        return name;
    }

    public static <T> TraitVector<T> copyOf(final AgentTrait<?, T> input) {
        checkNotNull(input);
        return create(input.get(), input.getRecombinationProbability(), input.getValueType(), input.getName());
    }
}
