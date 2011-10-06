/**
 *
 */
package org.asoem.greyfish.core.properties;

import com.google.common.collect.Lists;
import org.asoem.greyfish.core.genes.Genome;
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.core.utils.EvaluatedCandidate;
import org.asoem.greyfish.core.utils.EvaluatedCandidates;
import org.asoem.greyfish.core.utils.SimpleXMLConstructor;
import org.asoem.greyfish.lang.BuilderInterface;
import org.asoem.greyfish.lang.ClassGroup;
import org.asoem.greyfish.utils.DeepCloner;

import java.util.Collections;
import java.util.List;

/**
 * @author christoph
 *
 */
@ClassGroup(tags="property")
public class EvaluatedGenomeStorage extends AbstractGFProperty implements DiscreteProperty<List<EvaluatedCandidate<Genome>>> {

    final private List<EvaluatedCandidate<Genome>> spermList = Lists.newArrayList();

    public EvaluatedGenomeStorage(EvaluatedGenomeStorage storage, DeepCloner cloner) {
        super(storage, cloner);
    }

    public void addGenome(Genome genome, double d) {
//        if (!spermList.contains(genome)) // TODO: check for duplicates
            spermList.add(new EvaluatedCandidate<Genome>(genome, d));
    }

    public Genome getRandom() {
        if (!spermList.isEmpty())
            return EvaluatedCandidates.selectRandom(get()).getObject();
        else
            return null;
    }

    public Genome getRWS() {
        if (!spermList.isEmpty()) {
            return EvaluatedCandidates.selectRouletteWheel(get()).getObject();
        }
        else
            return null;
    }


    public boolean isEmpty() {
        return spermList.isEmpty();
    }

    @Override
    public void prepare(Simulation simulation) {
        super.prepare(simulation);
        spermList.clear();
    }

    @Override
    public EvaluatedGenomeStorage deepClone(DeepCloner cloner) {
        return new EvaluatedGenomeStorage(this, cloner);
    }

    @SimpleXMLConstructor
    private EvaluatedGenomeStorage() {
        this(new Builder());
    }

    protected EvaluatedGenomeStorage(AbstractBuilder<? extends AbstractBuilder> builder) {
        super(builder);
    }

    public static Builder with() { return new Builder(); }

    @Override
    public List<EvaluatedCandidate<Genome>> get() {
        return Collections.unmodifiableList(spermList);
    }

    public static final class Builder extends AbstractBuilder<Builder> implements BuilderInterface<EvaluatedGenomeStorage> {
        private Builder() {}
        @Override protected Builder self() { return this; }
        @Override public EvaluatedGenomeStorage build() { return new EvaluatedGenomeStorage(this); }
    }

    protected static abstract class AbstractBuilder<T extends AbstractBuilder<T>> extends AbstractGFProperty.AbstractBuilder<T> {
    }
}
