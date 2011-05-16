/**
 *
 */
package org.asoem.greyfish.core.properties;

import com.google.common.collect.Lists;
import org.asoem.greyfish.core.genes.GenomeInterface;
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.core.utils.EvaluatedCandidate;
import org.asoem.greyfish.core.utils.EvaluatedCandidates;
import org.asoem.greyfish.core.utils.SimpleXMLConstructor;
import org.asoem.greyfish.lang.BuilderInterface;
import org.asoem.greyfish.lang.ClassGroup;
import org.asoem.greyfish.utils.CloneMap;

import java.util.Collections;
import java.util.List;

/**
 * @author christoph
 *
 */
@ClassGroup(tags="property")
public class EvaluatedGenomeStorage extends AbstractGFProperty implements DiscreteProperty<List<EvaluatedCandidate<GenomeInterface>>> {

    final private List<EvaluatedCandidate<GenomeInterface>> spermList = Lists.newArrayList();

    public EvaluatedGenomeStorage(EvaluatedGenomeStorage storage, CloneMap cloneMap) {
        super(storage, cloneMap);
    }

    public void addGenome(GenomeInterface genome, double d) {
//        if (!spermList.contains(genome)) // TODO: check for duplicates
            spermList.add(new EvaluatedCandidate<GenomeInterface>(genome, d));
    }

    public GenomeInterface getRandom() {
        if (!spermList.isEmpty())
            return EvaluatedCandidates.selectRandom(get()).getObject();
        else
            return null;
    }

    public GenomeInterface getRWS() {
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
    public EvaluatedGenomeStorage deepCloneHelper(CloneMap cloneMap) {
        return new EvaluatedGenomeStorage(this, cloneMap);
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
    public List<EvaluatedCandidate<GenomeInterface>> get() {
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
