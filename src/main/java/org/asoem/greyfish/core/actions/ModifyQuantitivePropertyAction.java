/**
 *
 */
package org.asoem.greyfish.core.actions;

import com.google.common.collect.Iterables;
import net.sourceforge.jeval.EvaluationConstants;
import net.sourceforge.jeval.EvaluationException;
import net.sourceforge.jeval.Evaluator;
import org.asoem.greyfish.core.individual.AbstractAgentComponent;
import org.asoem.greyfish.core.io.Logger;
import org.asoem.greyfish.core.io.LoggerFactory;
import org.asoem.greyfish.core.properties.DoubleProperty;
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.lang.BuilderInterface;
import org.asoem.greyfish.lang.ClassGroup;
import org.asoem.greyfish.utils.DeepCloner;
import org.asoem.greyfish.utils.ConfigurationHandler;
import org.asoem.greyfish.utils.FiniteSetValueAdaptor;
import org.asoem.greyfish.utils.ValueAdaptor;
import org.simpleframework.xml.Element;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @author christoph
 *
 */
@ClassGroup(tags="actions")
public class ModifyQuantitivePropertyAction extends AbstractGFAction {

    private static final Logger LOGGER = LoggerFactory.getLogger(ModifyQuantitivePropertyAction.class);
    private final Evaluator FORMULA_EVALUATOR = new Evaluator(EvaluationConstants.SINGLE_QUOTE ,true,true,false,true);

    @Element(name = "property")
    private DoubleProperty parameterQuantitiveProperty;

    @Element(name = "energyRenewalFormula")
    private String energyCostsFormula = "0";

    private ModifyQuantitivePropertyAction() {
        this(new Builder());
    }

    @Override
    protected State executeUnconditioned(Simulation simulation) {
        parameterQuantitiveProperty.setValue(evaluateFormula(simulation));
        return State.END_SUCCESS;
    }

    public double evaluateFormula(Simulation simulation) {
        try {
            return Double.valueOf(FORMULA_EVALUATOR.evaluate());
        }
        catch (EvaluationException e) {
            LOGGER.warn("CostsFormula is not a valid expression", e);
            return 0;
        }
    }

    @Override
    public void configure(ConfigurationHandler e) {
        super.configure(e);
        e.add(new FiniteSetValueAdaptor<DoubleProperty>("Property", DoubleProperty.class) {
            @Override
            protected void set(DoubleProperty arg0) {
                parameterQuantitiveProperty = checkNotNull(arg0);
            }

            @Override
            public DoubleProperty get() {
                return parameterQuantitiveProperty;
            }

            @Override
            public Iterable<DoubleProperty> values() {
                return Iterables.filter(agent.get().getProperties(), DoubleProperty.class);
            }
        });
        e.add(new ValueAdaptor<String>("Formula", String.class) {
            @Override
            protected void set(String arg0) {
                energyCostsFormula = checkNotNull(arg0);
            } // TODO: check if string is a valid expression

            @Override
            public String get() {
                return energyCostsFormula;
            }
        });
    }

    @Override
    public void prepare(Simulation simulation) {
        super.prepare(simulation);
        try{
            FORMULA_EVALUATOR.parse(energyCostsFormula);
        } catch (EvaluationException e) {
            throw new IllegalStateException("to is not valid");
        }
    }

    @Override
    public AbstractAgentComponent deepClone(DeepCloner cloner) {
        return new ModifyQuantitivePropertyAction(this, cloner);
    }

    public ModifyQuantitivePropertyAction(ModifyQuantitivePropertyAction cloneable, DeepCloner map) {
        super(cloneable, map);
        this.parameterQuantitiveProperty = map.continueWith(cloneable.parameterQuantitiveProperty, DoubleProperty.class);
        this.energyCostsFormula = cloneable.energyCostsFormula;
    }

    protected ModifyQuantitivePropertyAction(AbstractBuilder<?> builder) {
        super(builder);
        this.energyCostsFormula = builder.energyCostsFormula;
        this.parameterQuantitiveProperty = builder.quantitiveProperty;
    }

    public static Builder with() { return new Builder(); }
    public static final class Builder extends AbstractBuilder<Builder> implements BuilderInterface<ModifyQuantitivePropertyAction> {
        private Builder() {}
        @Override protected Builder self() { return this; }
        @Override public ModifyQuantitivePropertyAction build() { return new ModifyQuantitivePropertyAction(this); }
    }

    protected static abstract class AbstractBuilder<T extends AbstractBuilder<T>> extends AbstractGFAction.AbstractBuilder<T> {
        private DoubleProperty quantitiveProperty;
        private String energyCostsFormula = "0";

        public T change(DoubleProperty quantitiveProperty) { this.quantitiveProperty = checkNotNull(quantitiveProperty); return self(); }
        public T to(String energyCostsFormula) { this.energyCostsFormula = energyCostsFormula; return self(); } // TODO: check if string is a valid expression

        public ModifyQuantitivePropertyAction build() { return new ModifyQuantitivePropertyAction(this); }
    }
}
