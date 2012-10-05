package org.asoem.greyfish.core.genes;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import org.asoem.greyfish.core.agent.AbstractComponentList;
import org.asoem.greyfish.utils.base.Product2;
import org.asoem.greyfish.utils.base.Tuple2;

import java.util.Iterator;
import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * User: christoph
 * Date: 08.02.12
 * Time: 11:08
 */
public abstract class AbstractGeneComponentList<E extends AgentTrait<?>> extends AbstractComponentList<E> implements GeneComponentList<E> {

    private ChromosomalHistory chromosomalHistory = NoChromosomalHistory.INSTANCE;

    protected AbstractGeneComponentList() {
    }

    public boolean isCompatible(GeneComponentList<? extends AgentTrait<?>> geneComponentList) {
        if (geneComponentList == null || this.size() != geneComponentList.size())
            return false;

        final Iterator<E> this_genome_iterator = this.iterator();

        for (AgentTrait<?> gene : geneComponentList) {
            if (!this_genome_iterator.next().isMutatedCopy(gene)) {
                return false;
            }
        }

        return true;
    }

    @Override
    public void updateAllGenes(GeneComponentList<? extends E> geneComponentList) {
        checkArgument(isCompatible(geneComponentList), "Given agentTraitList %s is not compatible to this agentTraitList %s.", geneComponentList, this);

        final Iterator<? extends E> sourceIterator = geneComponentList.iterator();
        final Iterator<E> destinationIterator = this.iterator();

        while (sourceIterator.hasNext() && destinationIterator.hasNext()) {
            E source = sourceIterator.next();
            E destination = destinationIterator.next();

            destination.setAllele(source.getAllele());
        }
    }

    @Override
    public void updateGenes(List<?> values) {
        checkNotNull(values);
        checkArgument(values.size() == size());

        for (Product2<E, ?> tuple2 : Tuple2.Zipped.of(this, values)) {
            tuple2._1().setAllele(tuple2._2());
        }
    }


    @Override
    public void initGenes() {
        for (AgentTrait<?> gene : this) {
            gene.setAllele(gene.createInitialValue());
        }
    }

    @Override
    public ChromosomalHistory getOrigin() {
        return chromosomalHistory;
    }

    @Override
    public void setOrigin(ChromosomalHistory history) {
        this.chromosomalHistory = checkNotNull(history);
    }

    @Override
    public int indexOfNamed(final String name) {
        return Iterables.indexOf(this, new Predicate<E>() {
            @Override
            public boolean apply(E input) {
                return input.getName().equals(name);
            }
        });
    }


}
