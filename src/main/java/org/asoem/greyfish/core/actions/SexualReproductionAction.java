package org.asoem.greyfish.core.actions;

import org.asoem.greyfish.core.genes.Genome;
import org.asoem.greyfish.core.individual.Agent;
import org.asoem.greyfish.core.individual.Individual;
import org.asoem.greyfish.core.individual.IndividualInterface;
import org.asoem.greyfish.core.properties.EvaluatedGenomeStorage;
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.lang.BuilderInterface;
import org.asoem.greyfish.lang.ClassGroup;
import org.asoem.greyfish.utils.CloneMap;
import org.asoem.greyfish.utils.Exporter;
import org.asoem.greyfish.utils.ValueAdaptor;
import org.asoem.greyfish.utils.ValueSelectionAdaptor;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;

import static com.google.common.base.Preconditions.checkNotNull;

@ClassGroup(tags="actions")
public class SexualReproductionAction extends AbstractGFAction {

    @Element(name="property")
    private EvaluatedGenomeStorage spermStorage;

    @Attribute(name = "reproductive_value")
    private int nOffspring = 1;

    public SexualReproductionAction() {
        this(new Builder());
    }

    @Override
    public boolean evaluate(Simulation simulation) {
        return super.evaluate(simulation)
                && nOffspring > 0
                && !spermStorage.isEmpty();
    }

    @Override
    protected void performAction(Simulation simulation) {
        assert(!spermStorage.isEmpty());

        for (int i = 0; i < nOffspring; i++) {
            final Agent offspring = componentOwner.deepClone(Agent.class);

            final Genome sperm = spermStorage.getRWS();
            Genome egg = new Genome(componentOwner.getGenome());
            egg.mutate();
            egg = egg.recombine(sperm);

            offspring.setGenome(egg);

            simulation.addNextStep(offspring, componentOwner);
        }
    }

    @Override
    public void export(Exporter e) {
        super.export(e);
        e.addField( new ValueAdaptor<Integer>("Offsprings per actions", Integer.class, nOffspring) {
            @Override
            protected void writeThrough(Integer arg0) {
                nOffspring = checkFrozen(checkNotNull(arg0));
            }
        });

        e.addField( new ValueSelectionAdaptor<EvaluatedGenomeStorage>("Genome storage", EvaluatedGenomeStorage.class, spermStorage, getComponentOwner().getProperties(EvaluatedGenomeStorage.class)) {
            @Override
            protected void writeThrough(EvaluatedGenomeStorage arg0) {
                spermStorage = checkFrozen(checkNotNull(arg0));
            }
        });
    }

    @Override
    public SexualReproductionAction deepCloneHelper(CloneMap cloneMap) {
        return new SexualReproductionAction(this, cloneMap);
    }

    private SexualReproductionAction(SexualReproductionAction cloneable, CloneMap map) {
        super(cloneable, map);
        this.spermStorage = map.clone(cloneable.spermStorage, EvaluatedGenomeStorage.class);
        this.nOffspring = cloneable.nOffspring;
    }

    protected SexualReproductionAction(AbstractBuilder<?> builder) {
        super(builder);
        this.spermStorage = builder.spermStorage;
        this.nOffspring = builder.nOffspring;
    }

    public static Builder with() { return new Builder(); }
    public static final class Builder extends AbstractBuilder<Builder> implements BuilderInterface<SexualReproductionAction> {
        private Builder() {}
        @Override protected Builder self() { return this; }
        @Override public SexualReproductionAction build() { return new SexualReproductionAction(this); }
    }

    protected static abstract class AbstractBuilder<T extends AbstractBuilder<T>> extends AbstractGFAction.AbstractBuilder<T> {
        private EvaluatedGenomeStorage spermStorage;
        private int nOffspring = 1;

        public T spermStorage(EvaluatedGenomeStorage spermStorage) { this.spermStorage = checkNotNull(spermStorage); return self(); }
        public T constantOffspringNumber(int nOffspring) { this.nOffspring = nOffspring; return self(); }
    }
}
