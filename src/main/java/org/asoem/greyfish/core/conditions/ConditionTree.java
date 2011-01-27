package org.asoem.greyfish.core.conditions;

import org.asoem.greyfish.core.individual.AbstractGFComponent;
import org.asoem.greyfish.core.individual.Individual;
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.lang.BuilderInterface;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

@Root
public class ConditionTree extends AbstractGFComponent implements Iterable<GFCondition> {
	
	@Element(name="condition", required=false)
	private final GFCondition rootCondition;

    public ConditionTree(ConditionTree tree, CloneMap map) {
        super(tree, map);
        this.rootCondition = deepClone(tree.rootCondition, map);
    }

    @Override
	public ConditionTreeDepthFirstIterator iterator() {
		return new ConditionTreeDepthFirstIterator(rootCondition);
	}

	public GFCondition getRootCondition() {
		return rootCondition;
	}

    public boolean evaluate(Simulation simulation) {
        return rootCondition == null || rootCondition.evaluate(simulation);
    }

	@Override
	public void setComponentOwner(Individual individual) {
		super.setComponentOwner(individual);
        if (rootCondition != null)
            rootCondition.setComponentOwner(individual);
	}

    @Override
    protected AbstractGFComponent deepCloneHelper(CloneMap map) {
        return new ConditionTree(this, map);
    }

    public ConditionTree(@Element(name="condition", required=false) GFCondition rootCondition) {
        this(new Builder(rootCondition));
    }

    protected ConditionTree(AbstractBuilder<? extends AbstractBuilder> builder) {
        super(builder);
        this.rootCondition = builder.rootCondition;
    }

    public static final class Builder extends AbstractBuilder<Builder> implements BuilderInterface<ConditionTree> {
        public Builder(GFCondition rootCondition) {
            rootCondition(rootCondition);
        }
        @Override protected Builder self() { return this; }
        public ConditionTree build() { return new ConditionTree(this); }
    }

    protected static abstract class AbstractBuilder<T extends AbstractBuilder<T>> extends LeafCondition.AbstractBuilder<T> {
        private GFCondition rootCondition;

        protected T rootCondition(GFCondition condition) { this.rootCondition = condition; return self(); }
    }

    @Override
    public void initialize(Simulation simulation) {
        if (rootCondition != null)
            rootCondition.initialize(simulation);
    }
}
