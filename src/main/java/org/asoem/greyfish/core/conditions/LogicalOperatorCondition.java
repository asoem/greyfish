/**
 *
 */
package org.asoem.greyfish.core.conditions;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.asoem.greyfish.core.individual.AgentComponent;
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.utils.base.DeepCloner;
import org.asoem.greyfish.utils.logging.Logger;
import org.asoem.greyfish.utils.logging.LoggerFactory;
import org.simpleframework.xml.ElementList;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import static com.google.common.base.Preconditions.*;
import static com.google.common.collect.Iterators.unmodifiableIterator;
import static java.util.Arrays.asList;


/**
 * Implementations of <code>LogicalOperatorCondition</code> should logically concatenate two or more implementations of <code>Condition</code>.
 * @author christoph
 *
 */
public abstract class LogicalOperatorCondition extends AbstractCondition implements Iterable<GFCondition> {

    private static final Logger LOGGER = LoggerFactory.getLogger(LogicalOperatorCondition.class);

    @ElementList(name="child_conditions", entry="condition", inline=true, empty=true, required = false)
    protected List<GFCondition> conditions = Lists.newArrayList();

    protected LogicalOperatorCondition(LogicalOperatorCondition cloneable, DeepCloner map) {
        super(cloneable, map);
        for (GFCondition condition : cloneable.getChildConditions())
            add(map.cloneField(condition, GFCondition.class));
    }

    public LogicalOperatorCondition(GFCondition ... conditions) {
        addAll(Arrays.asList(conditions));
        integrate(conditions);
    }

    private void integrate(Iterable<? extends GFCondition> condition2) {
        for (GFCondition gfCondition : condition2) {
            integrate(gfCondition);
        }
    }

    private void integrate(GFCondition ... conditions) {
        for (GFCondition condition : conditions) {
            condition.setAgent(getAgent());
            condition.setParent(this);
        }
    }

    private void disintegrate(GFCondition condition) {
        condition.setParent(null);
        condition.setAgent(null);
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

    public void addAll(Iterable<? extends GFCondition> childConditions) {
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
    public void add(GFCondition newChild) {
        checkNotNull(newChild);
        checkNotFrozen();
        integrate(newChild);
        conditions.add(newChild);
    }

    public GFCondition remove(int index) {
        checkNotFrozen();
        checkPositionIndex(index, conditions.size());
        GFCondition ret = conditions.remove(index);
        disintegrate(ret);
        return ret;
    }

    @Override
    public void remove(GFCondition condition) {
        checkNotFrozen();
        checkArgument(conditions.contains(condition));
        boolean remove = conditions.remove(condition);
        assert remove;
    }

    protected LogicalOperatorCondition(AbstractBuilder<?,?> builder) {
        super(builder);
        addAll(builder.conditions);
    }

    @Override
    public void freeze() {
        super.freeze();
        conditions = ImmutableList.copyOf(conditions);
        if (conditions.isEmpty())
            LOGGER.debug("LogicalOperatorCondition '" + getName() + "' has no subconditions");
    }

    protected static abstract class AbstractBuilder<E extends LogicalOperatorCondition, T extends AbstractBuilder<E, T>> extends AbstractCondition.AbstractBuilder<E,T> {
        private final List<GFCondition> conditions = Lists.newArrayList();

        protected T add(GFCondition condition) { condition.add(checkNotNull(condition)); return self(); }
        protected T add(GFCondition ... conditions) { this.conditions.addAll(asList(checkNotNull(conditions))); return self(); }
        protected T addAll(Iterable<? extends GFCondition> conditions) { Iterables.addAll(this.conditions, checkNotNull(conditions)); return self(); }
    }

    @Override
    public final Iterable<AgentComponent> children() {
        return Collections.<AgentComponent>unmodifiableList(conditions);
    }

    @Override
    public final Iterator<GFCondition> iterator() {
        return unmodifiableIterator(conditions.iterator());
    }
}
