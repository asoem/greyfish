package org.asoem.greyfish.core.properties;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import org.asoem.greyfish.core.eval.EvaluationException;
import org.asoem.greyfish.core.eval.GreyfishExpression;
import org.asoem.greyfish.gui.utils.ClassGroup;
import org.asoem.greyfish.utils.base.DeepCloneable;
import org.asoem.greyfish.utils.base.DeepCloner;
import org.asoem.greyfish.utils.gui.ConfigurationHandler;
import org.asoem.greyfish.utils.logging.Logger;
import org.asoem.greyfish.utils.logging.LoggerFactory;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.Set;

/**
 * User: christoph
 * Date: 06.09.11
 * Time: 10:28
 */
@ClassGroup(tags = {"property"})
public class DiscreteTrait extends AbstractGFProperty implements FiniteStateProperty<String> {

    private static final Logger LOGGER = LoggerFactory.getLogger(DiscreteTrait.class);

    private Map<String, GreyfishExpression> phenotypeConditionMap = ImmutableMap.of();

    protected DiscreteTrait(AbstractBuilder<?,?> builder) {
        super(builder);

        phenotypeConditionMap = ImmutableMap.copyOf(builder.phenotypeConditionMap);
    }

    protected DiscreteTrait(DiscreteTrait cloneable, DeepCloner map) {
        super(cloneable, map);

        phenotypeConditionMap = cloneable.phenotypeConditionMap; // Independent of clone. All clones can share this map.
    }

    @Override
    public String get() {
        // TODO: Inefficient if called more than once during one simulation step. Should be cached.
        return Iterables.find(phenotypeConditionMap.keySet(), new Predicate<String>() {
                @Override
                public boolean apply(@Nullable String phenotype) {
                    try {
                        return phenotypeConditionMap.get(phenotype).evaluateAsBoolean(DiscreteTrait.this);
                    } catch (EvaluationException e) {
                        LOGGER.error("Failed to evaluateAsBoolean expression {}", phenotype);
                        return false;
                    }
                }
        });
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
    public Set<String> getStates() {
        return phenotypeConditionMap.keySet();
    }

    public static Builder with() { return new Builder(); }
    public static final class Builder extends AbstractBuilder<DiscreteTrait,Builder> {
        @Override protected Builder self() { return this; }
        @Override public DiscreteTrait checkedBuild() { return new DiscreteTrait(this); }
    }

    protected static abstract class AbstractBuilder<E extends DiscreteTrait,T extends AbstractBuilder<E,T>> extends AbstractGFProperty.AbstractBuilder<E,T> {
        private final Map<String, GreyfishExpression> phenotypeConditionMap = Maps.newHashMap();

        public T addState(String state, String when) { phenotypeConditionMap.put(state, GreyfishExpression.compile(when)); return self();}
    }

}
