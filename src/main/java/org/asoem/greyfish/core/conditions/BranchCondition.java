/**
 *
 */
package org.asoem.greyfish.core.conditions;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.asoem.greyfish.core.agent.AgentComponent;
import org.asoem.greyfish.utils.base.DeepCloner;
import org.asoem.greyfish.utils.logging.SLF4JLogger;
import org.asoem.greyfish.utils.logging.SLF4JLoggerFactory;
import org.simpleframework.xml.ElementList;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import static com.google.common.base.Preconditions.*;
import static com.google.common.collect.Iterators.unmodifiableIterator;
import static java.util.Arrays.asList;
import static org.asoem.greyfish.utils.base.MorePreconditions.checkMutability;


/**
 * Implementations of <code>BranchCondition</code> should logically concatenate two or more implementations of <code>Condition</code>.
 * @author christoph
 *
 */
public abstract class BranchCondition extends AbstractCondition implements Iterable<ActionCondition> {

    private static final SLF4JLogger LOGGER = SLF4JLoggerFactory.getLogger(BranchCondition.class);

    @ElementList(name="child_conditions", entry="condition", inline=true, empty=true, required = false)
    protected List<ActionCondition> conditions = Lists.newArrayList();

    protected BranchCondition(BranchCondition cloneable, DeepCloner cloner) {
        super(cloneable, cloner);
        for (ActionCondition condition : cloneable.getChildConditions())
            add(cloner.getClone(condition, ActionCondition.class));
    }

    public BranchCondition(ActionCondition... conditions) {
        addAll(Arrays.asList(conditions));
        integrate(conditions);
    }

    private void integrate(Iterable<? extends ActionCondition> condition2) {
        for (ActionCondition actionCondition : condition2) {
            integrate(actionCondition);
        }
    }

    private void integrate(ActionCondition... conditions) {
        for (ActionCondition condition : conditions) {
            condition.setAgent(getAgent());
            condition.setParent(this);
        }
    }

    private void disintegrate(ActionCondition condition) {
        condition.setParent(null);
        condition.setAgent(null);
    }

    @Override
    public List<ActionCondition> getChildConditions() {
        return conditions;
    }

    @Override
    public boolean isLeafCondition() {
        return false;
    }

    @Override
    public void initialize() {
        super.initialize();
        for (ActionCondition condition : conditions)
            condition.initialize();
    }

    public void addAll(Iterable<? extends ActionCondition> childConditions) {
        checkMutability(this);
        integrate(childConditions);
        Iterables.addAll(conditions, childConditions);
    }

    public int indexOf(ActionCondition currentCondition) {
        return conditions.indexOf(currentCondition);
    }

    @Override
    public void add(ActionCondition newChild) {
        checkNotNull(newChild);
        checkMutability(this);
        integrate(newChild);
        conditions.add(newChild);
    }

    @Override
    public void insert(ActionCondition condition, int index) {
        checkNotNull(condition);
        checkMutability(this);
        integrate(condition);
        conditions.add(index, condition);
    }

    public ActionCondition remove(int index) {
        checkMutability(this);
        checkPositionIndex(index, conditions.size());
        ActionCondition ret = conditions.remove(index);
        disintegrate(ret);
        return ret;
    }

    @Override
    public void remove(ActionCondition condition) {
        checkMutability(this);
        checkArgument(conditions.contains(condition));
        boolean remove = conditions.remove(condition);
        assert remove;
    }

    @Override
    public void removeAll() {
        for (ActionCondition condition : conditions) {
            remove(condition);
        }
    }

    protected BranchCondition(AbstractBuilder<?, ?> builder) {
        super(builder);
        addAll(builder.conditions);
    }

    @Override
    public void freeze() {
        super.freeze();
        conditions = ImmutableList.copyOf(conditions);
        if (conditions.isEmpty())
            LOGGER.debug("BranchCondition '" + getName() + "' has no subconditions");
    }

    protected static abstract class AbstractBuilder<E extends BranchCondition, T extends AbstractBuilder<E, T>> extends AbstractCondition.AbstractBuilder<E,T> {
        private final List<ActionCondition> conditions = Lists.newArrayList();

        protected T add(ActionCondition condition) { condition.add(checkNotNull(condition)); return self(); }
        protected T add(ActionCondition... conditions) { this.conditions.addAll(asList(checkNotNull(conditions))); return self(); }
        protected T addAll(Iterable<? extends ActionCondition> conditions) { Iterables.addAll(this.conditions, checkNotNull(conditions)); return self(); }
    }

    @Override
    public final Iterable<AgentComponent> children() {
        return Collections.<AgentComponent>unmodifiableList(conditions);
    }

    @Override
    public final Iterator<ActionCondition> iterator() {
        return unmodifiableIterator(conditions.iterator());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        BranchCondition that = (BranchCondition) o;

        if (!conditions.equals(that.conditions)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + conditions.hashCode();
        return result;
    }
}
