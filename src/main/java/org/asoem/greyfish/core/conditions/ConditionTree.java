package org.asoem.greyfish.core.conditions;

import com.google.common.base.Preconditions;
import org.asoem.greyfish.core.individual.AbstractGFComponent;
import org.asoem.greyfish.core.individual.Individual;
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.utils.AbstractDeepCloneable;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

import java.util.Map;

@Root
public class ConditionTree extends AbstractGFComponent implements Iterable<GFCondition> {
	
	@Element(name="condition", required=false)
	private final GFCondition rootCondition;

	@Override
	public ConditionTreeDepthFirstIterator iterator() {
		return new ConditionTreeDepthFirstIterator(rootCondition);
	}


	public GFCondition getRootCondition() {
		return rootCondition;
	}

    public boolean evaluate(Simulation simulation) {
        return rootCondition.evaluate(simulation);
    }

	@Override
	public void setComponentOwner(Individual individual) {
		super.setComponentOwner(individual);
		rootCondition.setComponentOwner(individual);
	}

    @Override
    protected AbstractDeepCloneable deepCloneHelper(
            Map<AbstractDeepCloneable, AbstractDeepCloneable> mapDict) {
        return new Builder(rootCondition).fromClone(this, mapDict).build();
    }

    public ConditionTree(@Element(name="condition", required=false) GFCondition rootCondition) {
        this(new Builder(rootCondition));
    }

    protected ConditionTree(AbstractBuilder<? extends AbstractBuilder> builder) {
        super(builder);
        this.rootCondition = builder.rootCondition;
    }

    public static final class Builder extends AbstractBuilder<Builder> {
        public Builder(GFCondition rootCondition) {
            rootCondition(rootCondition);
        }

        @Override protected Builder self() { return this; }
    }

    protected static abstract class AbstractBuilder<T extends AbstractBuilder<T>> extends LeafCondition.AbstractBuilder<T> {
        private GFCondition rootCondition;

        protected T rootCondition(GFCondition condition) { this.rootCondition = Preconditions.checkNotNull(condition); return self(); }

        protected T fromClone(ConditionTree component, Map<AbstractDeepCloneable, AbstractDeepCloneable> mapDict) {
            super.fromClone(component, mapDict).
                    rootCondition(component.rootCondition);
            return self();
        }

        public ConditionTree build() { return new ConditionTree(this); }
    }

}
