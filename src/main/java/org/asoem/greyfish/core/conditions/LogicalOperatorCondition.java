/**
 *
 */
package org.asoem.greyfish.core.conditions;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import org.asoem.greyfish.core.individual.GFComponent;
import org.asoem.greyfish.core.individual.IndividualInterface;
import org.asoem.greyfish.core.io.Logger;
import org.asoem.greyfish.core.io.LoggerFactory;
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.utils.CloneMap;
import org.simpleframework.xml.ElementList;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.Arrays.asList;


/**
 * Implementations of <code>LogicalOperatorCondition</code> should logically concatenate two or more implementations of <code>Condition</code>.
 * @author christoph
 *
 */
public abstract class LogicalOperatorCondition extends AbstractCondition {

    private static final Logger LOGGER = LoggerFactory.getLogger(LogicalOperatorCondition.class);
    @ElementList(name="child_conditions", entry="condition", inline=true, empty=true, required = false)
    protected List<GFCondition> conditions = new ArrayList<GFCondition>(0);

    protected LogicalOperatorCondition(LogicalOperatorCondition clonable, CloneMap map) {
        super(clonable, map);
        addAll(map.cloneAll(clonable.getChildConditions(), GFCondition.class));
    }

    private void integrate(Iterable<? extends GFCondition> condition2) {
        for (GFCondition gfCondition : condition2) {
            integrate(gfCondition);
        }
    }

    private void integrate(GFCondition ... conditions) {
        for (GFCondition condition : conditions) {
            condition.setComponentRoot(getComponentOwner());
            condition.setParent(this);
        }
    }

    private void disintegrate(GFCondition condition) {
        condition.setParent(null);
        condition.setComponentRoot(null);
    }

    public GFCondition[] getConditions() {
        return conditions.toArray(new GFCondition[conditions.size()]);
    }


    @Override
    public void setComponentRoot(IndividualInterface individual) {
        super.setComponentRoot(individual);
        for (GFCondition condition : this.conditions) {
            condition.setComponentRoot(individual);
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
    public void prepare(Simulation simulation) {
        super.prepare(simulation);
        for (GFCondition condition : conditions)
            condition.prepare(simulation);
    }

    public void addAll(Iterable<GFCondition> childConditions) {
        checkNotFrozen();
        integrate(childConditions);
        Iterables.addAll(conditions, childConditions);
    }

    public int indexOf(GFCondition currentCondition) {
        return conditions.indexOf(currentCondition);
    }

    public GFCondition set(int indexOf, GFCondition newCondition) {
        checkNotFrozen();
        integrate(newCondition);
        GFCondition ret = conditions.set(indexOf, newCondition);
        disintegrate(ret);
        return ret;
    }

    public void add(int index, GFCondition condition) {
        checkNotNull(condition);
        checkNotFrozen();
        integrate(condition);
        conditions.add(index, condition);
    }

    @Override
    public boolean add(GFCondition newChild) {
        checkNotNull(newChild);
        checkNotFrozen();
        integrate(newChild);
        return conditions.add(newChild);
    }

    public void removeAllChildConditions() {
        checkNotFrozen();
        for (GFCondition condition : conditions)
            disintegrate(condition);
        conditions.clear();
    }

    public GFCondition remove(int index) {
        checkNotFrozen();
        GFCondition ret = conditions.remove(index);
        disintegrate(ret);
        return ret;
    }

    @Override
    public boolean remove(GFCondition condition) {
        checkNotFrozen();
        return conditions.remove(condition);
    }

    protected LogicalOperatorCondition(AbstractBuilder<?> builder) {
        super(builder);
        addAll(builder.conditions);
    }

    @Override
    public void freeze() {
        super.freeze();
        conditions = ImmutableList.copyOf(conditions);
        if (conditions.isEmpty())
            LOGGER.debug("LogicalOperatorCondition '" + name + "' has no Subconditions");
    }

    @Override
    public void checkConsistency(Iterable<? extends GFComponent> components) throws IllegalStateException {
        super.checkConsistency(components);
        for (GFCondition condition : conditions) {
            condition.checkConsistency(components);
        }
    }

    protected static abstract class AbstractBuilder<T extends AbstractBuilder<T>> extends AbstractCondition.AbstractBuilder<T> {
        private final List<GFCondition> conditions = new ArrayList<GFCondition>();

        protected T addConditions(GFCondition... conditions) { addConditions(asList(conditions)); return self(); }
        protected T addConditions(Iterable<GFCondition> conditions) { Iterables.addAll(this.conditions, conditions); return self(); }
    }


    @Override
    public Iterator<GFComponent> iterator() {
        return Iterables.<GFComponent>concat(conditions).iterator();
    }
}
