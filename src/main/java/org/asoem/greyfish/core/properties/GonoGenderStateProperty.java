package org.asoem.greyfish.core.properties;

import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import org.asoem.greyfish.core.genes.DefaultGene;
import org.asoem.greyfish.core.genes.MutationOperator;
import org.asoem.greyfish.lang.BuilderInterface;
import org.asoem.greyfish.lang.ClassGroup;
import org.asoem.greyfish.utils.CloneMap;
import org.asoem.greyfish.utils.RandomUtils;

import java.util.Set;

@ClassGroup(tags="property")
public class GonoGenderStateProperty extends AbstractGFProperty implements FiniteSetProperty<GonoGenderStateProperty.Gender> {

    // TODO: Add configurable matrix for state transition values. Alternative: A configurable "state" gene field

    public enum Gender {
        MALE("Male"),
        FEMALE("Female"),
        ASEX("Asex");

        private final String name;

        Gender(String name) {
            this.name = name;
        }

        public static String[] names() {
            ImmutableList.Builder<String> builder = ImmutableList.builder();
            for (Gender g : values())
                builder.add(g.name());
            return Iterables.toArray(builder.build(), String.class);
        }

        @Override
        public String toString() {
            return name;
        }
    }

    private final Supplier<Integer> gene = registerGene(
            new DefaultGene<Integer>(RandomUtils.nextBoolean() ? 0 : 1, Integer.class, new MutationOperator<Integer>() {
                private final static double SEX_ASEX_TRANSITION_PROBABILITY = 0.001;

                @Override
                public Integer mutate(Integer original) {
                    return RandomUtils.nextDouble() < SEX_ASEX_TRANSITION_PROBABILITY ?
                            Gender.ASEX.ordinal() :
                            RandomUtils.nextBoolean() ?
                                    Gender.MALE.ordinal() : Gender.FEMALE.ordinal();
                }

                @Override
                public double normalizedDistance(Integer orig, Integer copy) {
                    return 1;
                }

                @Override
                public double normalizedWeightedDistance(Integer orig, Integer copy) {
                    return normalizedDistance(orig, copy) * 0.1;
                }
            }
    ));

    public GonoGenderStateProperty(GonoGenderStateProperty gonoGenderStateProperty, CloneMap cloneMap) {
        super(gonoGenderStateProperty, cloneMap);
    }

    @Override
    public Gender get() {
        return Gender.values()[gene.get()];
    }

    @Override
    public Set<Gender> getSet() {
        return Sets.newHashSet(Gender.values());
    }

    @Override
    public GonoGenderStateProperty deepCloneHelper(CloneMap cloneMap) {
        return new GonoGenderStateProperty(this, cloneMap);
    }

    private GonoGenderStateProperty() {
        this(new Builder());
    }

    protected GonoGenderStateProperty(AbstractBuilder<? extends AbstractBuilder> builder) {
        super(builder);
    }

    public static Builder with() { return new Builder(); }
    public static final class Builder extends AbstractBuilder<Builder> implements BuilderInterface<GonoGenderStateProperty> {
        private Builder() {}
        @Override protected Builder self() { return this; }
        @Override public GonoGenderStateProperty build() { return new GonoGenderStateProperty(this); }
    }

    protected static abstract class AbstractBuilder<T extends AbstractBuilder<T>> extends AbstractGFProperty.AbstractBuilder<T> {}
}
