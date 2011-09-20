package org.asoem.greyfish.core.properties;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import org.asoem.greyfish.core.eval.EvaluationException;
import org.asoem.greyfish.core.eval.GreyfishExpression;
import org.asoem.greyfish.core.eval.GreyfishExpressionFactory;
import org.asoem.greyfish.core.io.Logger;
import org.asoem.greyfish.core.io.LoggerFactory;
import org.asoem.greyfish.lang.BuilderInterface;
import org.asoem.greyfish.lang.ClassGroup;
import org.asoem.greyfish.utils.DeepCloner;
import org.asoem.greyfish.utils.ConfigurationHandler;
import org.asoem.greyfish.utils.DeepCloneable;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.Set;

/**
 * User: christoph
 * Date: 06.09.11
 * Time: 10:28
 */
@ClassGroup(tags = {"property"})
public class DiscreteTrait extends AbstractGFProperty implements FiniteSetProperty<String> {

    private static final Logger LOGGER = LoggerFactory.getLogger(DiscreteTrait.class);
    private Map<String, GreyfishExpression<DiscreteTrait>> phenotypeConditionMap = ImmutableMap.of();
    private String currentState = null;
    private boolean dirty = true;

    protected DiscreteTrait(AbstractBuilder<? extends AbstractBuilder> builder) {
        super(builder);

        phenotypeConditionMap = ImmutableMap.copyOf(builder.phenotypeConditionMap);
    }

    protected DiscreteTrait(DiscreteTrait cloneable, DeepCloner map) {
        super(cloneable, map);

        phenotypeConditionMap = cloneable.phenotypeConditionMap; // Independent of clone. All clones can share this map.
    }

    @Override
    public String get() {
        if (dirty) {
            // TODO: in a quick and dirty state. How to handle no match / empty map?
            currentState = Iterables.find(phenotypeConditionMap.keySet(), new Predicate<String>() {
                @Override
                public boolean apply(@Nullable String expression) {
                    try {
                        return phenotypeConditionMap.get(expression).evaluateAsBoolean(DiscreteTrait.this);
                    } catch (EvaluationException e) {
                        LOGGER.warn("Failed to evaluateAsBoolean expression {}", expression);
                        return false;
                    }
                }
            },null);
            // TODO: currently dirty is never set to true again
            //dirty = false;
        }

        return currentState;
    }

    @Override
    public void configure(ConfigurationHandler e) {
        super.configure(e);

        // TODO: add map to exporter
    }

    @Override
    public DeepCloneable deepClone(DeepCloner cloner) {
        return new DiscreteTrait(this, cloner);
    }

    @Override
    public Set<String> getSet() {
        return phenotypeConditionMap.keySet();
    }

    public static Builder with() { return new Builder(); }
    public static final class Builder extends AbstractBuilder<Builder> implements BuilderInterface<DiscreteTrait> {
        private Builder() {}
        @Override protected Builder self() { return this; }
        @Override public DiscreteTrait build() { return new DiscreteTrait(checkedSelf()); }
    }

    protected static abstract class AbstractBuilder<T extends AbstractBuilder<T>> extends AbstractGFProperty.AbstractBuilder<T> {
        private final Map<String, GreyfishExpression<DiscreteTrait>> phenotypeConditionMap = Maps.newHashMap();

        public AbstractBuilder<T> addState(String state, String when) { phenotypeConditionMap.put(state, GreyfishExpressionFactory.compileExpression(when).forContext(DiscreteTrait.class)); return self();}
    }

}
