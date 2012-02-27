/**
 *
 */
package org.asoem.greyfish.core.properties;

import com.google.common.collect.Lists;
import org.asoem.greyfish.core.genes.EvaluatedChromosome;
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.core.utils.SimpleXMLConstructor;
import org.asoem.greyfish.gui.utils.ClassGroup;
import org.asoem.greyfish.utils.base.DeepCloner;

import java.util.Collections;
import java.util.List;

/**
 * @author christoph
 *
 */
@ClassGroup(tags="properties")
public class EvaluatedGenomeStorage extends AbstractGFProperty implements DiscreteProperty<List<EvaluatedChromosome<?>>> {

    final private List<EvaluatedChromosome<?>> spermList = Lists.newArrayList();

    public EvaluatedGenomeStorage(EvaluatedGenomeStorage storage, DeepCloner cloner) {
        super(storage, cloner);
    }

    public void addGenome(EvaluatedChromosome<?> genome) {
        spermList.add(genome);
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
    public EvaluatedGenomeStorage() {
        this(new Builder());
    }

    protected EvaluatedGenomeStorage(AbstractBuilder<? extends EvaluatedGenomeStorage, ? extends AbstractBuilder> builder) {
        super(builder);
    }

    public static Builder with() { return new Builder(); }

    @Override
    public List<EvaluatedChromosome<?>> get() {
        return Collections.unmodifiableList(spermList);
    }

    public void clear() {
        spermList.clear();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        EvaluatedGenomeStorage storage = (EvaluatedGenomeStorage) o;

        return spermList.equals(storage.spermList);

    }

    @Override
    public int hashCode() {
        return spermList.hashCode();
    }

    public static final class Builder extends AbstractBuilder<EvaluatedGenomeStorage, Builder> {
        private Builder() {}
        @Override protected Builder self() { return this; }
        @Override public EvaluatedGenomeStorage checkedBuild() { return new EvaluatedGenomeStorage(this); }
    }

    protected static abstract class AbstractBuilder<E extends EvaluatedGenomeStorage,T extends AbstractBuilder<E,T>> extends AbstractGFProperty.AbstractBuilder<E,T> {
    }
}
