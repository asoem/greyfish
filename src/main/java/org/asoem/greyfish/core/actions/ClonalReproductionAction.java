package org.asoem.greyfish.core.actions;

import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.gui.utils.ClassGroup;
import org.asoem.greyfish.utils.base.DeepCloner;
import org.asoem.greyfish.utils.gui.ConfigurationHandler;
import org.asoem.greyfish.utils.gui.ValueAdaptor;
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
    protected ActionState executeUnconditioned(Simulation simulation) {
        for (int i = 0; i < parameterClones; i++) {
            simulation.createAgent(
                    agent().getPopulation(),
                    agent().createGamete()/*.mutated()*/,
                    simulation.getSpace().getCoordinates(agent()));
        }
        return ActionState.END_SUCCESS;
    }

    @Override
    public ClonalReproductionAction deepClone(DeepCloner cloner) {
        return new ClonalReproductionAction(this, cloner);
    }

    public ClonalReproductionAction(ClonalReproductionAction cloneable, DeepCloner map) {
        super(cloneable, map);
        this.parameterClones = cloneable.parameterClones;
    }

    protected ClonalReproductionAction(AbstractBuilder<?,?> builder) {
        super(builder);
        this.parameterClones = builder.nClones;
    }

    @Override
    public void configure(ConfigurationHandler e) {
        super.configure(e);
        e.add(new ValueAdaptor<Integer>("#clones", Integer.class) {
            @Override
            protected void set(Integer arg0) {
                parameterClones = arg0;
            }

            @Override
            public Integer get() {
                return parameterClones;
            }
        });
    }

    public static Builder with() { return new Builder(); }

    public static final class Builder extends AbstractBuilder<ClonalReproductionAction,Builder>  {
        @Override protected Builder self() { return this; }
        @Override public ClonalReproductionAction checkedBuild() { return new ClonalReproductionAction(this); }
    }

    protected static abstract class AbstractBuilder<E extends ClonalReproductionAction, T extends AbstractBuilder<E,T>> extends AbstractGFAction.AbstractBuilder<E,T> {
        private int nClones = 1;

        public T clones(int parameterClones) { this.nClones = parameterClones; return self(); }
    }
}
