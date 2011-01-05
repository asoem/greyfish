/**
 * 
 */
package org.asoem.sico.core.properties;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.asoem.sico.core.genes.Genome;
import org.asoem.sico.core.simulation.Simulation;
import org.asoem.sico.lang.ClassGroup;
import org.asoem.sico.utils.AbstractDeepCloneable;
import org.asoem.sico.utils.RandomUtils;
import org.uncommons.watchmaker.framework.EvaluatedCandidate;
import org.uncommons.watchmaker.framework.selection.RouletteWheelSelection;

/**
 * @author christoph
 *
 */
@ClassGroup(tags="property")
public class EvaluatedGenomeStorage extends AbstractDiscreteProperty<List<EvaluatedCandidate<Genome>>> {

	final private static RouletteWheelSelection SELECTOR = new RouletteWheelSelection();

	public EvaluatedGenomeStorage() {
		value = new ArrayList<EvaluatedCandidate<Genome>>();
	}

	public EvaluatedGenomeStorage(
			EvaluatedGenomeStorage genomeCollectorProperty,
			Map<AbstractDeepCloneable, AbstractDeepCloneable> mapDict) {
		super(genomeCollectorProperty, mapDict);
		value = new ArrayList<EvaluatedCandidate<Genome>>();
	}

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
		return new EvaluatedGenomeStorage(this, mapDict);
	}
}
