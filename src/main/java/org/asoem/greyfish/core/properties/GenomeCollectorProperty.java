/**
 * 
 */
package org.asoem.greyfish.core.properties;

import java.util.Collection;
import java.util.Map;

import org.asoem.greyfish.core.genes.Genome;
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.lang.CircularFifoBuffer;
import org.asoem.greyfish.utils.AbstractDeepCloneable;
import org.asoem.greyfish.utils.RandomUtils;

import com.google.common.collect.Iterables;

/**
 * @author christoph
 *
 */
public class GenomeCollectorProperty extends AbstractDiscreteProperty<Collection<Genome>> {

	public GenomeCollectorProperty() {
		initFields();
	}

	public GenomeCollectorProperty(
			GenomeCollectorProperty genomeCollectorProperty,
			Map<AbstractDeepCloneable, AbstractDeepCloneable> mapDict) {
		super(genomeCollectorProperty, mapDict);
		initFields();
	}

	public void addGenome(Genome genome) {
		value.add(genome);
	}
	
	private void initFields() {
		value = new CircularFifoBuffer<Genome>(); // 32 max
	}

	public Genome getRandom() {
		if (value.size() > 0) {
			return Iterables.get(value, RandomUtils.nextInt(value.size()));
		}
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
		return new GenomeCollectorProperty(this, mapDict);
	}
}
