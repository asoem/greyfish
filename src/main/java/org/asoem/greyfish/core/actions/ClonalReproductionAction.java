package org.asoem.greyfish.core.actions;

import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.lang.BuilderInterface;
import org.asoem.greyfish.lang.ClassGroup;
import org.asoem.greyfish.utils.CloneMap;
import org.asoem.greyfish.utils.Exporter;
import org.asoem.greyfish.utils.ValueAdaptor;
import org.simpleframework.xml.Attribute;

@ClassGroup(tags="actions")
public class ClonalReproductionAction extends AbstractGFAction {

    @Attribute(name = "nClones")
    private int parameterClones;

    @SuppressWarnings("unused") // used in deserialization process
    private ClonalReproductionAction() {
        this(new Builder());
    }

    @Override
    protected void performAction(Simulation simulation) {
        for (int i = 0; i < parameterClones; i++) {
            simulation.createAgent(
                    getComponentOwner().getPopulation(),
                    getComponentOwner().getAnchorPoint(),
                    getComponentOwner().getGenome().mutated());
        }
    }

    @Override
    public ClonalReproductionAction deepCloneHelper(CloneMap map) {
        return new ClonalReproductionAction(this, map);
    }

    public ClonalReproductionAction(ClonalReproductionAction cloneable, CloneMap map) {
        super(cloneable, map);
        this.parameterClones = cloneable.parameterClones;
    }

    protected ClonalReproductionAction(AbstractBuilder<?> builder) {
        super(builder);
        this.parameterClones = builder.nClones;
    }

    @Override
    public void export(Exporter e) {
        e.addField( new ValueAdaptor<Integer>("#clones", Integer.class, parameterClones) {
            @Override
            protected void writeThrough(Integer arg0) {
                parameterClones = checkFrozen(arg0);
            }
        });
    }

    public static Builder with() { return new Builder(); }
    public static final class Builder extends AbstractBuilder<Builder> implements BuilderInterface<ClonalReproductionAction> {
        private Builder() {}
        @Override protected Builder self() { return this; }
        @Override public ClonalReproductionAction build() { return new ClonalReproductionAction(this); }
    }

    protected static abstract class AbstractBuilder<T extends AbstractBuilder<T>> extends AbstractGFAction.AbstractBuilder<T> {
        private int nClones = 1;

        public T clones(int parameterClones) { this.nClones = parameterClones; return self(); }
    }
}
