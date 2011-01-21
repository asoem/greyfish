/**
 *
 */
package org.asoem.greyfish.core.properties;

import org.asoem.greyfish.core.genes.Genome;
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.lang.BuilderInterface;
import org.asoem.greyfish.lang.ClassGroup;
import org.asoem.greyfish.utils.AbstractDeepCloneable;
import org.asoem.greyfish.utils.RandomUtils;
import org.uncommons.watchmaker.framework.EvaluatedCandidate;
import org.uncommons.watchmaker.framework.selection.RouletteWheelSelection;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author christoph
 *
 */
@ClassGroup(tags="property")
public class EvaluatedGenomeStorage extends AbstractDiscreteProperty<List<EvaluatedCandidate<Genome>>> {

    final private static RouletteWheelSelection SELECTOR = new RouletteWheelSelection();

    public void addGenome(Genome genome, double d) {
        if (!value.contains(genome))
            value.add(new EvaluatedCandidate<Genome>(genome, d));
    }

    public Genome getRandom() {
        if (!value.isEmpty())
            return value.get(RandomUtils.nextInt(value.size())).getCandidate();
        else
            return null;
    }

    public Genome getRWS() {
        if (!value.isEmpty()) {
            final List<Genome> selection = SELECTOR.select(value, true, 1, RandomUtils.randomInstance());
            return selection.get(0);
        }
        else
            return null;
    }


    public boolean isEmpty() {
        return value.isEmpty();
    }

    @Override
    public void initialize(Simulation simulation) {
        super.initialize(simulation);
        value.clear();
    }

    @Override
    protected AbstractDeepCloneable deepCloneHelper(
            Map<AbstractDeepCloneable, AbstractDeepCloneable> mapDict) {
        return new Builder().fromClone(this, mapDict).build();
    }

    protected EvaluatedGenomeStorage(AbstractBuilder<? extends AbstractBuilder> builder) {
        super(builder);
    }

    public static Builder with() { return new Builder(); }
    public static final class Builder extends AbstractBuilder<Builder> implements BuilderInterface<EvaluatedGenomeStorage> {
        private Builder() {}
        @Override protected Builder self() { return this; }
        @Override public EvaluatedGenomeStorage build() { return new EvaluatedGenomeStorage(this); }
    }

    protected static abstract class AbstractBuilder<T extends AbstractBuilder<T>> extends AbstractDiscreteProperty.AbstractBuilder<T, List<EvaluatedCandidate<Genome>>> {
        protected AbstractBuilder() {
            value(new ArrayList<EvaluatedCandidate<Genome>>());
        }

        protected T fromClone(EvaluatedGenomeStorage property, Map<AbstractDeepCloneable, AbstractDeepCloneable> mapDict) {
            super.fromClone(property, mapDict);
            return self();
        }
    }
}
