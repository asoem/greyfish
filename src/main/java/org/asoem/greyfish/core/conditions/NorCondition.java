package org.asoem.greyfish.core.conditions;

import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.utils.AbstractDeepCloneable;

import java.util.Map;

public class NorCondition extends OrCondition {

	@Override
	public boolean evaluate(Simulation simulation) {
		return ! super.evaluate(simulation);
	}
	
	    protected NorCondition(AbstractBuilder<?> builder) {
        super(builder);
    }

    public static final class Builder extends AbstractBuilder<Builder> {
        @Override protected Builder self() { return this; }
    }

    protected static abstract class AbstractBuilder<T extends AbstractBuilder<T>> extends OrCondition.AbstractBuilder<T> {
        protected T fromClone(NorCondition component, Map<AbstractDeepCloneable, AbstractDeepCloneable> mapDict) {
            super.fromClone(component, mapDict);
            return self();
        }

        public AbstractDeepCloneable build() { return new NorCondition(this); }
    }
}
