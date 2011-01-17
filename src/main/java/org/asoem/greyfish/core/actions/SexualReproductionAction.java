package org.asoem.greyfish.core.actions;

import java.util.Map;

import org.asoem.greyfish.core.genes.Genome;
import org.asoem.greyfish.core.individual.Individual;
import org.asoem.greyfish.core.io.GreyfishLogger;
import org.asoem.greyfish.core.properties.EvaluatedGenomeStorage;
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.lang.ClassGroup;
import org.asoem.greyfish.utils.AbstractDeepCloneable;
import org.asoem.greyfish.utils.Exporter;
import org.asoem.greyfish.utils.ValueAdaptor;
import org.asoem.greyfish.utils.ValueSelectionAdaptor;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;

@ClassGroup(tags="actions")
public class SexualReproductionAction extends AbstractGFAction {

	private static final long serialVersionUID = 7844538525466224834L;

	@Element(name="property")
	private EvaluatedGenomeStorage spermStorage;

	@Attribute(name = "reproductive_value")
	private int parameterOffspringsPerAction = 1;

	public SexualReproductionAction() {
	}

	public SexualReproductionAction(String name) {
		super(name);
	}

	public SexualReproductionAction(String name, EvaluatedGenomeStorage spermStorage, int offspringsPerAction) {
		super(name);
		this.spermStorage = spermStorage;
		this.parameterOffspringsPerAction = offspringsPerAction;
	}

	protected SexualReproductionAction(
			SexualReproductionAction action,
			Map<AbstractDeepCloneable, AbstractDeepCloneable> mapDict) {
		super(action, mapDict);
		spermStorage = deepClone(action.spermStorage, mapDict);
		parameterOffspringsPerAction = action.parameterOffspringsPerAction;
	}

	@Override
	public boolean evaluate(Simulation simulation) {
		return super.evaluate(simulation)
			&& parameterOffspringsPerAction > 0
			&& spermStorage.isEmpty() == false;
	}

	@Override
	protected void performAction(Simulation simulation) {
		assert(!spermStorage.isEmpty());

		try {
			for (int i = 0; i < parameterOffspringsPerAction; i++) {
				final Individual offspring = componentOwner.createClone(simulation);

				final Genome sperm = spermStorage.getRWS();
				Genome egg = new Genome(componentOwner.getGenome());
				egg.mutate();
				egg = egg.recombine(sperm);

				offspring.setGenome(egg);

				simulation.addIndividual(offspring, componentOwner);
			}
		} catch (Exception e) {
			GreyfishLogger.error("Error creating a clone", e);
		}
	}

	@Override
	public void export(Exporter e) {
		super.export(e);
		e.addField( new ValueAdaptor<Integer>("Offsprings per actions", Integer.class, parameterOffspringsPerAction) {
			@Override
			protected void writeThrough(Integer arg0) {
				parameterOffspringsPerAction = arg0;
			}
		});

		e.addField( new ValueSelectionAdaptor<EvaluatedGenomeStorage>("Genome storage", EvaluatedGenomeStorage.class, spermStorage, getComponentOwner().getProperties(EvaluatedGenomeStorage.class)) {
			@Override
			protected void writeThrough(EvaluatedGenomeStorage arg0) {
				spermStorage = arg0;
			}
		});
	}

	@Override
	public void initialize(Simulation simulation) {
		super.initialize(simulation);
		if (spermStorage == null) {
			spermStorage = new EvaluatedGenomeStorage();
			getComponentOwner().addProperty(spermStorage);
		}
	}

	@Override
	protected AbstractDeepCloneable deepCloneHelper(
			Map<AbstractDeepCloneable, AbstractDeepCloneable> mapDict) {
		return new SexualReproductionAction(this, mapDict);
	}
}
