/**
 * 
 */
package org.asoem.greyfish.core.conditions;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import org.asoem.greyfish.core.individual.Individual;
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.utils.AbstractDeepCloneable;
import org.simpleframework.xml.ElementList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;



/**
 * Implementations of <code>LogicalOperatorCondition</code> should logically concatenate two or more implementations of <code>Condition</code>.
 * @author christoph
 *
 */
public abstract class LogicalOperatorCondition extends AbstractCondition {

	@ElementList(name="child_conditions", entry="condition", inline=true, empty=true, required = false)
	protected List<GFCondition> conditions = new ArrayList<GFCondition>(0);

	private void integrate(Collection<? extends GFCondition> condition2) {
		for (GFCondition gfCondition : condition2) {
			integrate(gfCondition);
		}
	}

	private void integrate(GFCondition ... conditions) {
		for (GFCondition condition : conditions) {
			condition.setComponentOwner(getComponentOwner());
			condition.setParent(this);
		}
	}

	private void disintegrate(GFCondition condition) {
		condition.setParent(null);
		condition.setComponentOwner(null);
	}

	public GFCondition[] getConditions() {
		return conditions.toArray(new GFCondition[conditions.size()]);
	}


	@Override
	public void setComponentOwner(Individual individual) {
		super.setComponentOwner(individual);
		for (GFCondition condition : this.conditions) {
			condition.setComponentOwner(individual);
		}
	}

	@Override
	public List<GFCondition> getChildConditions() {
		return conditions;
	}

	@Override
	public boolean isLeafCondition() {
		return false;
	}

	@Override
	public void initialize(Simulation simulation) {
		super.initialize(simulation);
		for (GFCondition condition : conditions)
			condition.initialize(simulation);
		conditions = ImmutableList.copyOf(conditions);
	}

	public void addAll(Collection<GFCondition> childConditions) {
		integrate(childConditions);
		conditions.addAll(childConditions);
	}

	public int indexOf(GFCondition currentCondition) {
		return conditions.indexOf(currentCondition);
	}

	public GFCondition set(int indexOf, GFCondition newCondition) {
		integrate(newCondition);
		GFCondition ret = conditions.set(indexOf, newCondition);
		disintegrate(ret);
		return ret;
	}

	public void add(int index, GFCondition condition) {
		integrate(condition);
		conditions.add(index, condition);
	}

	@Override
	public boolean add(GFCondition newChild) {
		integrate(newChild);
		return conditions.add(newChild);
	}

	public void removeAllChildConditions() {
		for (GFCondition condition : conditions) {
			disintegrate(condition);
		}
		conditions.clear();
	}

	public GFCondition remove(int index) {
		GFCondition ret = conditions.remove(index);
		disintegrate(ret);
		return ret;
	}

	@Override
	public boolean remove(GFCondition condition) {
		return conditions.remove(condition);
	}

    protected LogicalOperatorCondition(AbstractBuilder<?> builder) {
        super(builder);
    }

    protected static abstract class AbstractBuilder<T extends AbstractBuilder<T>> extends AbstractCondition.AbstractBuilder<T> {
        private final List<GFCondition> conditions = new ArrayList<GFCondition>();

        public T addCondition(GFCondition condition) { this.conditions.add(condition); return self(); }
        public T addConditions(Iterable<GFCondition> conditions) { Iterables.addAll(this.conditions, conditions); return self(); }

        protected T fromClone(LogicalOperatorCondition component, Map<AbstractDeepCloneable, AbstractDeepCloneable> mapDict) {
            super.fromClone(component, mapDict).
                    addConditions(deepClone(component.conditions, mapDict));
            return self();
        }
    }
}
