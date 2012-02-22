package org.asoem.greyfish.core.properties;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Predicate;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import org.asoem.greyfish.core.eval.EvaluationException;
import org.asoem.greyfish.core.eval.GreyfishExpression;
import org.asoem.greyfish.core.eval.GreyfishExpressionFactoryHolder;
import org.asoem.greyfish.core.utils.SimpleXMLConstructor;
import org.asoem.greyfish.gui.utils.ClassGroup;
import org.asoem.greyfish.utils.base.DeepCloneable;
import org.asoem.greyfish.utils.base.DeepCloner;
import org.asoem.greyfish.utils.gui.AbstractTypedValueModel;
import org.asoem.greyfish.utils.gui.ConfigurationHandler;
import org.asoem.greyfish.utils.logging.Logger;
import org.asoem.greyfish.utils.logging.LoggerFactory;
import org.simpleframework.xml.ElementMap;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.Set;

/**
 * User: christoph
 * Date: 06.09.11
 * Time: 10:28
 */
@ClassGroup(tags = {"properties"})
public class ConditionalStatesProperty extends AbstractGFProperty implements FiniteStateProperty<String> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConditionalStatesProperty.class);

    @ElementMap(entry="state", key="condition", attribute=true, inline=true)
    private Map<String, GreyfishExpression> conditionMap = ImmutableMap.of();

    @SimpleXMLConstructor
    public ConditionalStatesProperty() {
        this(new Builder());
    }

    protected ConditionalStatesProperty(AbstractBuilder<?, ?> builder) {
        super(builder);

        conditionMap = builder.phenotypeConditionMap;
    }

    protected ConditionalStatesProperty(ConditionalStatesProperty cloneable, DeepCloner map) {
        super(cloneable, map);

        conditionMap = cloneable.conditionMap;
    }

    @Override
    public String get() {
        // TODO: Inefficient if called more than once during one simulation step. Result should be cached.
        // TODO: Compare performance to a version where evaluates logic from below is inside an expression. Could be faster than evaluation multiple expression.
        return Iterables.find(conditionMap.keySet(), new Predicate<String>() {
            @Override
            public boolean apply(@Nullable String phenotype) {
                try {
                    return conditionMap.get(phenotype).evaluateForContext(ConditionalStatesProperty.this).asBoolean();
                } catch (EvaluationException e) {
                    LOGGER.error("Failed to evaluate expression {}", phenotype);
                    return false;
                }
            }
        });
    }

    @Override
    public void configure(ConfigurationHandler e) {
        super.configure(e);

        e.add("", new AbstractTypedValueModel<String>() {
            @Override
            protected void set(String arg0) {
                conditionMap.clear();
                conditionMap.putAll(parse(arg0));
            }

            @Override
            public String get() {
                return toParsable(conditionMap);
            }
        });
    }

    private String toParsable(Map<String, GreyfishExpression> conditionMap) {
        return Joiner.on("\n").withKeyValueSeparator(" : ").join(conditionMap);
    }

    private Map<? extends String, ? extends GreyfishExpression> parse(String arg0) {
        return Maps.transformValues(Splitter.on("\n").withKeyValueSeparator(":").split(arg0), new Function<String, GreyfishExpression>() {
            @Override
            public GreyfishExpression apply(@Nullable String s) {
                return GreyfishExpressionFactoryHolder.compile(s);
            }
        });
    }

    @Override
    public DeepCloneable deepClone(DeepCloner cloner) {
        return new ConditionalStatesProperty(this, cloner);
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

    public static Builder with() { return new Builder(); }
    public static final class Builder extends AbstractBuilder<ConditionalStatesProperty,Builder> {
        @Override protected Builder self() { return this; }
        @Override public ConditionalStatesProperty checkedBuild() { return new ConditionalStatesProperty(this); }
    }

    @SuppressWarnings("UnusedDeclaration")
    protected static abstract class AbstractBuilder<E extends ConditionalStatesProperty,T extends AbstractBuilder<E,T>> extends AbstractGFProperty.AbstractBuilder<E,T> {
        private final Map<String, GreyfishExpression> phenotypeConditionMap = Maps.newHashMap();

        public T addState(String state, String when) { phenotypeConditionMap.put(state, GreyfishExpressionFactoryHolder.compile(when)); return self();}
    }

}
