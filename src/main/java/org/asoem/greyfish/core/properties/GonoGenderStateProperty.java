package org.asoem.greyfish.core.properties;

import com.google.common.collect.Sets;
import org.asoem.greyfish.core.genes.DefaultGene;
import org.asoem.greyfish.core.genes.Gene;
import org.asoem.greyfish.core.genes.MutationOperator;
import org.asoem.greyfish.lang.BuilderInterface;
import org.asoem.greyfish.lang.ClassGroup;
import org.asoem.greyfish.utils.CloneMap;

import java.util.Set;

import static org.asoem.greyfish.utils.RandomUtils.nextBoolean;
import static org.asoem.greyfish.utils.RandomUtils.trueWithProbability;

@ClassGroup(tags="property")
public class GonoGenderStateProperty extends AbstractGFProperty implements FiniteSetProperty<GonoGenderStateProperty.Gender> {

    // TODO: Add configurable matrix for state transition values. Alternative: A configurable "state" gene field

    public enum Gender {
        MALE,
        FEMALE,
        ASEX
    }

    private final Gene<Integer> gene;

    protected GonoGenderStateProperty(GonoGenderStateProperty clone, CloneMap cloneMap) {
        super(clone, cloneMap);
        gene = registerGene(DefaultGene.newMutatedCopy(clone.gene));
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

    @SuppressWarnings("unused") // used in the deserialization process
    private GonoGenderStateProperty() {
        this(new Builder());
    }

    protected GonoGenderStateProperty(AbstractBuilder<? extends AbstractBuilder> builder) {
        super(builder);

        MutationOperator<Integer> mutationOperator = new MutationOperator<Integer>() {
            private final static double SEX_ASEX_TRANSITION_PROBABILITY = 0.001;

            @Override
            public Integer mutate(Integer original) {
                switch (Gender.values()[original]) {
                    case ASEX:
                        return Gender.ASEX.ordinal();
                    default:
                        return trueWithProbability(SEX_ASEX_TRANSITION_PROBABILITY) ?
                                Gender.ASEX.ordinal() :
                                nextBoolean() ? Gender.MALE.ordinal() : Gender.FEMALE.ordinal();
                }

            }

            @Override
            public double normalizedDistance(Integer orig, Integer copy) {
                return 1;
            }

            @Override
            public double normalizedWeightedDistance(Integer orig, Integer copy) {
                return normalizedDistance(orig, copy) * 0.1;
            }
        };

        int initialValue = nextBoolean() ? Gender.MALE.ordinal() : Gender.FEMALE.ordinal();
        gene = registerGene(new DefaultGene<Integer>(initialValue, Integer.class, mutationOperator));
    }

    public static Builder with() { return new Builder(); }
    public static final class Builder extends AbstractBuilder<Builder> implements BuilderInterface<GonoGenderStateProperty> {
        private Builder() {}
        @Override protected Builder self() { return this; }
        @Override public GonoGenderStateProperty build() { return new GonoGenderStateProperty(this); }
    }

    protected static abstract class AbstractBuilder<T extends AbstractBuilder<T>> extends AbstractGFProperty.AbstractBuilder<T> {}
}
