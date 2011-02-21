/**
 *
 */
package org.asoem.greyfish.core.properties;

import org.asoem.greyfish.core.genes.Genome;
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.lang.BuilderInterface;
import org.asoem.greyfish.lang.ClassGroup;
import org.asoem.greyfish.utils.CloneMap;
import org.asoem.greyfish.utils.RandomUtils;
import org.uncommons.watchmaker.framework.EvaluatedCandidate;
import org.uncommons.watchmaker.framework.selection.RouletteWheelSelection;

import java.util.ArrayList;
import java.util.List;

/**
 * @author christoph
 *
 */
@ClassGroup(tags="property")
public class EvaluatedGenomeStorage extends AbstractGFProperty implements DiscreteProperty<List<EvaluatedCandidate<Genome>>> {

    final private static RouletteWheelSelection SELECTOR = new RouletteWheelSelection();
    final private List<EvaluatedCandidate<Genome>> spermList = new ArrayList<EvaluatedCandidate<Genome>>();

    public EvaluatedGenomeStorage(EvaluatedGenomeStorage storage, CloneMap cloneMap) {
        super(storage, cloneMap);
    }

    public void addGenome(Genome genome, double d) {
//        if (!spermList.contains(genome)) // TODO: check for duplicates
            spermList.add(new EvaluatedCandidate<Genome>(genome, d));
    }

    public Genome getRandom() {
        if (!spermList.isEmpty())
            return spermList.get(RandomUtils.nextInt(spermList.size())).getCandidate();
        else
            return null;
    }

    public Genome getRWS() {
        if (!spermList.isEmpty()) {
            final List<Genome> selection = SELECTOR.select(spermList, true, 1, RandomUtils.randomInstance());
            return selection.get(0);
        }
        else
            return null;
    }


    public boolean isEmpty() {
        return spermList.isEmpty();
    }

    @Override
    public void initialize(Simulation simulation) {
        super.initialize(simulation);
        spermList.clear();
    }

    @Override
    public EvaluatedGenomeStorage deepCloneHelper(CloneMap cloneMap) {
        return new EvaluatedGenomeStorage(this, cloneMap);
    }

    private EvaluatedGenomeStorage() {
        this(new Builder());
    }

    protected EvaluatedGenomeStorage(AbstractBuilder<? extends AbstractBuilder> builder) {
        super(builder);
    }

    public static Builder with() { return new Builder(); }

    @Override
    public List<EvaluatedCandidate<Genome>> get() {
        return spermList;
    }

    public static final class Builder extends AbstractBuilder<Builder> implements BuilderInterface<EvaluatedGenomeStorage> {
        private Builder() {}
        @Override protected Builder self() { return this; }
        @Override public EvaluatedGenomeStorage build() { return new EvaluatedGenomeStorage(this); }
    }

    protected static abstract class AbstractBuilder<T extends AbstractBuilder<T>> extends AbstractGFProperty.AbstractBuilder<T> {
    }
}
