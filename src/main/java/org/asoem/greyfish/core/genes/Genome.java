package org.asoem.sico.core.genes;

import java.util.AbstractList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javolution.util.FastList;

import org.asoem.sico.core.io.GreyfishLogger;
import org.asoem.sico.utils.RandomUtils;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;

public class Genome extends AbstractList<Gene<?>> {

	private final List<Gene<?>> genes = new FastList<Gene<?>>();

	public Genome() {
	}

	public Genome(Genome g) {
		Preconditions.checkNotNull(g);
		try {
			for (Gene<?> gene : g.getGenes()) {
				add(gene.clone());
			}
		} catch (CloneNotSupportedException e) {
			GreyfishLogger.error("A Gene ", e);
		}
	}

	@Override
	public boolean add(Gene<?> e) {
		Preconditions.checkNotNull(e);
		return genes.add(e);
	}

	@Override
	public boolean addAll(Collection<? extends Gene<?>> c) {
		Preconditions.checkNotNull(c);
		return genes.addAll(c);
	}

	public Collection<Gene<?>> getGenes() {
		return genes;
	}

	public void mutate() {
		for (Gene<?> gene : genes) {
			gene.mutate();
		}
	}

	public Genome recombine(Genome genome) {
		Preconditions.checkArgument(isCompatibleGenome(genome));

		Genome ret = new Genome(this);

		Iterator<Gene<?>> other_genome_iter = genome.iterator();
		Iterator<Gene<?>> ret_genome_iter = ret.iterator();

		while (ret_genome_iter.hasNext() && other_genome_iter.hasNext()) {

			Gene<?> retGene = ret_genome_iter.next();
			Gene<?> otherGene = other_genome_iter.next();

			if (RandomUtils.nextBoolean())
				retGene.setRepresentation(otherGene.getRepresentation());
		}
		return ret;
	}

	@Override
	public Genome clone() {
		return new Genome(this);
	}

	@Override
	public Iterator<Gene<?>> iterator() {
		return genes.iterator();
	}

	@Override
	public int size() {
		return genes.size();
	}

	public void initialize() {
		for (Gene<?> gene : this)
			gene.initialize();
	}

	public void initGenome(Genome genome) {
		Preconditions.checkArgument(isCompatibleGenome(genome));

		final Iterator<Gene<?>> other_genome_iter = genome.iterator();
		final Iterator<Gene<?>> this_genome_iter = this.iterator();

		while (this_genome_iter.hasNext()
				&& other_genome_iter.hasNext()) {
			final Gene<?> thisGene = this_genome_iter.next();
			final Gene<?> newGene = other_genome_iter.next();
			thisGene.setRepresentation(newGene.getRepresentation());
		}
	}

	private boolean isCompatibleGenome(Genome genome) {
		return genome != null
		&& genome.size() == this.size();
	}

	public Gene<?> getGene(String geneName) {
		for (Gene<?> g : genes) {
			if (g.getName().equals(geneName))
				return g;
		}
		return null;
	}

	public boolean containsGene(String geneName) {
		for (Gene<?> g : genes) {
			if (g.getName().equals(geneName))
				return true;
		}
		return false;
	}

	@Override
	public Gene<?> get(int index) {
		return genes.get(index);
	}

	@Override
	public void clear() {
		genes.clear();
	}
	
	@Override
	public String toString() {
		return "[" + Joiner.on(',').join(genes) + "]";
	}
}
