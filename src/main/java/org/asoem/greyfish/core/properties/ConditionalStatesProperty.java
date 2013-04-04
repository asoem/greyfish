package org.asoem.greyfish.core.properties;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Predicate;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import org.asoem.greyfish.core.agent.Agent;
import org.asoem.greyfish.core.eval.EvaluationException;
import org.asoem.greyfish.core.eval.GreyfishExpression;
import org.asoem.greyfish.core.eval.GreyfishExpressionFactoryHolder;
import org.asoem.greyfish.utils.base.DeepCloneable;
import org.asoem.greyfish.utils.base.DeepCloner;
import org.asoem.greyfish.utils.base.Tagged;
import org.asoem.greyfish.utils.logging.SLF4JLogger;
import org.asoem.greyfish.utils.logging.SLF4JLoggerFactory;

import java.util.Map;
import java.util.Set;

/**
 * User: christoph
 * Date: 06.09.11
 * Time: 10:28
 */
@Tagged("properties")
public class ConditionalStatesProperty<A extends Agent<A, ?>> extends AbstractAgentProperty<String, A> implements FiniteStateProperty<String, A> {

    private static final SLF4JLogger LOGGER = SLF4JLoggerFactory.getLogger(ConditionalStatesProperty.class);

    private Map<String, GreyfishExpression> conditionMap = ImmutableMap.of();

    @SuppressWarnings("UnusedDeclaration") // Needed for construction by reflection / deserialization
    public ConditionalStatesProperty() {
        this(new Builder<A>());
    }

    protected ConditionalStatesProperty(AbstractBuilder<A, ?, ?> builder) {
        super(builder);

        conditionMap = builder.phenotypeConditionMap;
    }

    protected ConditionalStatesProperty(ConditionalStatesProperty<A> cloneable, DeepCloner map) {
        super(cloneable, map);

        conditionMap = cloneable.conditionMap;
    }

    @Override
    public String getValue() {
        // TODO: Inefficient if called more than once during one simulation step. Result should be cached.
        // TODO: Compare performance to a version where evaluates logic from below is inside an expression. Could be faster than evaluation multiple expression.
        return Iterables.find(conditionMap.keySet(), new Predicate<String>() {
            @Override
            public boolean apply(String phenotype) {
                try {
                    return conditionMap.get(phenotype).evaluateForContext(ConditionalStatesProperty.this).asBoolean();
                } catch (EvaluationException e) {
                    LOGGER.error("Failed to evaluate expression {}", phenotype);
                    return false;
                }
            }
        });
    }

    private String toParsable(Map<String, GreyfishExpression> conditionMap) {
        return Joiner.on("\n").withKeyValueSeparator(" : ").join(conditionMap);
    }

    private Map<? extends String, ? extends GreyfishExpression> parse(String arg0) {
        return Maps.transformValues(Splitter.on("\n").withKeyValueSeparator(":").split(arg0), new Function<String, GreyfishExpression>() {
            @Override
            public GreyfishExpression apply(String s) {
                return GreyfishExpressionFactoryHolder.compile(s);
            }
        });
    }

    @Override
    public DeepCloneable deepClone(DeepCloner cloner) {
        return new ConditionalStatesProperty<A>(this, cloner);
    }

    @Override
    public Set<String> getStates() {
        return conditionMap.keySet();
    }

    public Map<String, GreyfishExpression> getConditionMap() {
        return conditionMap;
    }

    @Override
    public void freeze() {
        conditionMap = ImmutableMap.copyOf(conditionMap);
    }

    public static <A extends Agent<A, ?>> Builder<A> with() { return new Builder<A>(); }

    public static final class Builder<A extends Agent<A, ?>> extends AbstractBuilder<A, ConditionalStatesProperty<A>, Builder<A>> {
        @Override protected Builder<A> self() { return this; }
        @Override public ConditionalStatesProperty<A> checkedBuild() { return new ConditionalStatesProperty<A>(this); }
    }

    @SuppressWarnings("UnusedDeclaration")
    protected static abstract class AbstractBuilder<A extends Agent<A, ?>, E extends ConditionalStatesProperty<A>,T extends AbstractBuilder<A,E,T>> extends AbstractAgentProperty.AbstractBuilder<E,A,T> {
        private final Map<String, GreyfishExpression> phenotypeConditionMap = Maps.newHashMap();

        public T addState(String state, String when) { phenotypeConditionMap.put(state, GreyfishExpressionFactoryHolder.compile(when)); return self();}
    }

}
