package org.asoem.greyfish.core.properties;

import org.asoem.greyfish.core.genes.Gene;
import org.asoem.greyfish.core.genes.GeneController;
import org.asoem.greyfish.core.genes.GeneControllerAdaptor;
import org.asoem.greyfish.core.genes.ImmutableGene;
import org.asoem.greyfish.gui.utils.ClassGroup;
import org.asoem.greyfish.utils.base.DeepCloner;

import java.util.EnumSet;
import java.util.Set;

import static org.asoem.greyfish.utils.math.RandomUtils.nextBoolean;
import static org.asoem.greyfish.utils.math.RandomUtils.trueWithProbability;

@ClassGroup(tags="properties")
public class GonoGenderStateProperty extends AbstractGFProperty implements FiniteStateProperty<GonoGenderStateProperty.Gender> {

    // TODO: Add configurable matrix for state transition values. Alternative: A configurable "state" gene field

    public enum Gender {
        MALE,
        FEMALE,
        ASEX
    }

    private final Gene<Integer> gene;

    protected GonoGenderStateProperty(GonoGenderStateProperty clone, DeepCloner cloner) {
        super(clone, cloner);
        gene = registerGene(ImmutableGene.newMutatedCopy(clone.gene));
    }

    @Override
    public Gender get() {
        return Gender.values()[gene.get()];
    }

    @Override
    public Set<Gender> getStates() {
        return EnumSet.allOf(Gender.class);
    }

    @Override
    public GonoGenderStateProperty deepClone(DeepCloner cloner) {
        return new GonoGenderStateProperty(this, cloner);
    }

    @SuppressWarnings("unused") // used in the deserialization process
    private GonoGenderStateProperty() {
        this(new Builder());
    }

    protected GonoGenderStateProperty(AbstractBuilder<?,?> builder) {
        super(builder);

        GeneController<Integer> mutationOperator = new GeneControllerAdaptor<Integer>() {
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

            @Override
            public Integer createInitialValue() {
                return nextBoolean() ? Gender.MALE.ordinal() : Gender.FEMALE.ordinal();
            }
        };

        int initialValue = nextBoolean() ? Gender.MALE.ordinal() : Gender.FEMALE.ordinal();
        gene = registerGene(new ImmutableGene<Integer>(initialValue, Integer.class, mutationOperator));
    }

    public static Builder with() { return new Builder(); }
    public static final class Builder extends AbstractBuilder<GonoGenderStateProperty, Builder> {
        private Builder() {}
        @Override protected Builder self() { return this; }
        @Override public GonoGenderStateProperty checkedBuild() { return new GonoGenderStateProperty(this); }
    }

    protected static abstract class AbstractBuilder<E extends GonoGenderStateProperty, T extends AbstractBuilder<E, T>> extends AbstractGFProperty.AbstractBuilder<E, T> {}
}
