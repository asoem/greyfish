package org.asoem.greyfish.core.conditions;

import org.asoem.greyfish.core.individual.AbstractGFComponent;
import org.asoem.greyfish.core.individual.GFComponent;
import org.asoem.greyfish.core.individual.Individual;
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.lang.BuilderInterface;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

import java.util.Iterator;

@Root
public class ConditionTree extends AbstractGFComponent {
	
	@Element(name="condition", required=false)
	private final GFCondition rootCondition;

    public ConditionTree(ConditionTree tree, CloneMap map) {
        super(tree, map);
        this.rootCondition = deepClone(tree.rootCondition, map);
    }

	public GFCondition getRootCondition() {
		return rootCondition;
	}

    @Override
    public Iterator<GFComponent> iterator() {
        return rootCondition == null ? super.iterator() : rootCondition.iterator();
    }

    public ConditionTreeDepthFirstIterator treeIterator() {
        return ConditionTreeDepthFirstIterator.forRoot(rootCondition);
    }

    public boolean evaluate(Simulation simulation) {
        return rootCondition == null || rootCondition.evaluate(simulation);
    }

	@Override
	public void setComponentRoot(Individual individual) {
		super.setComponentRoot(individual);
        if (rootCondition != null)
            rootCondition.setComponentRoot(individual);
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
