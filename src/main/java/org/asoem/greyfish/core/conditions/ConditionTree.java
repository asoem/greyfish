package org.asoem.greyfish.core.conditions;

import java.util.Map;

import org.asoem.greyfish.core.individual.AbstractGFComponent;
import org.asoem.greyfish.core.individual.Individual;
import org.asoem.greyfish.utils.AbstractDeepCloneable;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

@Root
public class ConditionTree extends AbstractGFComponent implements Iterable<GFCondition> {
	
	@Element(name="condition", required=false)
	private GFCondition rootCondition;

	public ConditionTree() {
	}

	public ConditionTree(GFCondition rootCondition) {
		this.rootCondition = rootCondition;
	}

	public ConditionTree(ConditionTree conditionTree,
			Map<AbstractDeepCloneable, AbstractDeepCloneable> mapDict) {
		super(conditionTree, mapDict);
		setRootCondition(deepClone(conditionTree.getRootCondition(), mapDict));
	}

	@Override
	public ConditionTreeDepthFirstIterator iterator() {
		return new ConditionTreeDepthFirstIterator(rootCondition);
	}


	public GFCondition getRootCondition() {
		return rootCondition;
	}


	public void setRootCondition(GFCondition clone) {
		rootCondition = clone;
		if (rootCondition != null)
			rootCondition.setComponentOwner(getComponentOwner());
	}

	@Override
	public void setComponentOwner(Individual individual) {
		super.setComponentOwner(individual);
		if (rootCondition != null)
			rootCondition.setComponentOwner(individual);
	}

	@Override
	protected AbstractDeepCloneable deepCloneHelper(
			Map<AbstractDeepCloneable, AbstractDeepCloneable> mapDict) {
		return new ConditionTree(this, mapDict);
	}

	public boolean hasRootCondition() {
		return rootCondition != null;
	}

}
