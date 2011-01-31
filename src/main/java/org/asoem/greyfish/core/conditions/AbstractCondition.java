package org.asoem.greyfish.core.conditions;

import org.asoem.greyfish.core.individual.AbstractGFComponent;
import org.asoem.greyfish.utils.CloneMap;
import org.asoem.greyfish.utils.Exporter;
import org.simpleframework.xml.core.Commit;

/**
 * A class that implements the <code>Condition</code> interface.
 * Can be used to make a <code>GFAction</code> conditional.
 * @author christoph
 */
public abstract class AbstractCondition extends AbstractGFComponent implements GFCondition {

	private GFCondition parentCondition;

    protected AbstractCondition(AbstractCondition clonable, CloneMap map) {
        super(clonable, map);
        this.parentCondition = map.clone(clonable.parentCondition, GFCondition.class);
    }

    @Override
	public GFCondition getParentCondition() {
		return parentCondition;
	}

	@Override
	public void setParent(GFCondition parent) {
		this.parentCondition = checkFrozen(parent);
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

    protected AbstractCondition(AbstractBuilder<? extends AbstractBuilder> builder) {
        super(builder);
        this.parentCondition = builder.parentCondition;
    }

    protected static abstract class AbstractBuilder<T extends AbstractBuilder<T>> extends AbstractGFComponent.AbstractBuilder<T> {
        private GFCondition parentCondition;
    }

    @Override
    public String toString() {
        return parentCondition + "<-" + this.getClass().getSimpleName();
    }
}
