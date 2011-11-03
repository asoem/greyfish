/**
 *
 */
package org.asoem.greyfish.core.actions;

import com.google.common.collect.Iterables;
import net.sourceforge.jeval.EvaluationConstants;
import net.sourceforge.jeval.EvaluationException;
import net.sourceforge.jeval.Evaluator;
import org.asoem.greyfish.core.individual.AbstractAgentComponent;
import org.asoem.greyfish.core.properties.DoubleProperty;
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.gui.utils.ClassGroup;
import org.asoem.greyfish.utils.base.DeepCloner;
import org.asoem.greyfish.utils.gui.ConfigurationHandler;
import org.asoem.greyfish.utils.gui.SetAdaptor;
import org.asoem.greyfish.utils.gui.ValueAdaptor;
import org.asoem.greyfish.utils.logging.Logger;
import org.asoem.greyfish.utils.logging.LoggerFactory;
import org.simpleframework.xml.Element;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @author christoph
 *
 */
@ClassGroup(tags="actions")
public class ModifyQuantitativePropertyAction extends AbstractGFAction {

    private static final Logger LOGGER = LoggerFactory.getLogger(ModifyQuantitativePropertyAction.class);
    private final Evaluator FORMULA_EVALUATOR = new Evaluator(EvaluationConstants.SINGLE_QUOTE ,true,true,false,true);

    @Element(name = "property")
    private DoubleProperty parameterQuantitativeProperty;

    @Element(name = "energyRenewalFormula")
    private String energyCostsFormula = "0";

    private ModifyQuantitativePropertyAction() {
        this(new Builder());
    }

    @Override
    protected ActionState executeUnconditioned(Simulation simulation) {
        parameterQuantitativeProperty.setValue(evaluateFormula(simulation));
        return ActionState.END_SUCCESS;
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
        e.add("Property", new SetAdaptor<DoubleProperty>(DoubleProperty.class) {
            @Override
            protected void set(DoubleProperty arg0) {
                parameterQuantitativeProperty = checkNotNull(arg0);
            }

            @Override
            public DoubleProperty get() {
                return parameterQuantitativeProperty;
            }

            @Override
            public Iterable<DoubleProperty> values() {
                return Iterables.filter(agent().getProperties(), DoubleProperty.class);
            }
        });
        e.add("Formula", new ValueAdaptor<String>(String.class) {
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
        return new ModifyQuantitativePropertyAction(this, cloner);
    }

    public ModifyQuantitativePropertyAction(ModifyQuantitativePropertyAction cloneable, DeepCloner map) {
        super(cloneable, map);
        this.parameterQuantitativeProperty = map.cloneField(cloneable.parameterQuantitativeProperty, DoubleProperty.class);
        this.energyCostsFormula = cloneable.energyCostsFormula;
    }

    protected ModifyQuantitativePropertyAction(AbstractBuilder<?, ?> builder) {
        super(builder);
        this.energyCostsFormula = builder.energyCostsFormula;
        this.parameterQuantitativeProperty = builder.quantitativeProperty;
    }

    public static Builder with() { return new Builder(); }
    public static final class Builder extends AbstractBuilder<ModifyQuantitativePropertyAction, Builder> {
        private Builder() {}
        @Override protected Builder self() { return this; }
        @Override public ModifyQuantitativePropertyAction checkedBuild() { return new ModifyQuantitativePropertyAction(this); }
    }

    protected static abstract class AbstractBuilder<E extends ModifyQuantitativePropertyAction, T extends AbstractBuilder<E, T>> extends AbstractGFAction.AbstractBuilder<E,T> {
        private DoubleProperty quantitativeProperty;
        private String energyCostsFormula = "0";

        public T change(DoubleProperty quantitativeProperty) { this.quantitativeProperty = checkNotNull(quantitativeProperty); return self(); }
        public T to(String energyCostsFormula) { this.energyCostsFormula = energyCostsFormula; return self(); } // TODO: check if string is a valid expression
    }
}
