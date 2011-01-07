package org.asoem.greyfish.core.conditions;

import java.util.Map;

import org.asoem.greyfish.core.individual.AbstractGFComponent;
import org.asoem.greyfish.utils.AbstractDeepCloneable;
import org.asoem.greyfish.utils.Exporter;
import org.simpleframework.xml.core.Commit;

/**
 * A class that implements the <code>Condition</code> interface can be used to make a <code>IndividualAction</code> conditional.
 * @author christoph
 */
public abstract class AbstractCondition extends AbstractGFComponent implements GFCondition {

	private GFCondition parentCondition;

	public AbstractCondition() {
	}

	protected AbstractCondition(AbstractCondition condition,
			Map<AbstractDeepCloneable, AbstractDeepCloneable> mapDict) {
		super(condition, mapDict);
		parentCondition = deepClone(condition.parentCondition, mapDict);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1764101723431506928L;

	@Override
	public GFCondition getParentCondition() {
		return parentCondition;
	}

	@Override
	public void setParent(GFCondition parent) {
		this.parentCondition = parent;
	}

	@Override
	public boolean add(GFCondition condition) {
		return false;
	}

	@Override
	public boolean remove(GFCondition condition) {
		return false;
	}
	
	@Override
	public boolean isRootCondition() {
		return parentCondition == null;
	}
	
	@Override
	public GFCondition getRoot() {
		return (isRootCondition()) ? this : getParentCondition().getRoot();
	}
	
	@SuppressWarnings("unused")
	@Commit
	private void commit() {
		if (!isLeafCondition()) {
			for (GFCondition condition : getChildConditions()) {
				condition.setParent(this);
			}
		}
	}
	
	@Override
	public void export(Exporter e) {
	}
}
