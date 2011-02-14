/**
 *
 */
package org.asoem.greyfish.core.actions;

import com.google.common.collect.Iterables;
import net.sourceforge.jeval.EvaluationConstants;
import net.sourceforge.jeval.EvaluationException;
import net.sourceforge.jeval.Evaluator;
import org.asoem.greyfish.core.individual.AbstractGFComponent;
import org.asoem.greyfish.core.io.GreyfishLogger;
import org.asoem.greyfish.core.properties.DoubleProperty;
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.lang.BuilderInterface;
import org.asoem.greyfish.lang.ClassGroup;
import org.asoem.greyfish.utils.CloneMap;
import org.asoem.greyfish.utils.Exporter;
import org.asoem.greyfish.utils.ValueAdaptor;
import org.asoem.greyfish.utils.ValueSelectionAdaptor;
import org.simpleframework.xml.Element;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @author christoph
 *
 */
@ClassGroup(tags="actions")
public class ModifyQuantitivePropertyAction extends AbstractGFAction {

    private final Evaluator FORMULA_EVALUATOR = new Evaluator(EvaluationConstants.SINGLE_QUOTE ,true,true,false,true);

    @Element(name = "property")
    private DoubleProperty parameterQuantitiveProperty;

    @Element(name = "energyRenewalFormula")
    private String energyCostsFormula = "0";

    private ModifyQuantitivePropertyAction() {
        this(new Builder());
    }

    @Override
    protected void performAction(Simulation simulation) {
        parameterQuantitiveProperty.setValue(evaluateFormula());
    }

    public double evaluateFormula() {
        try {
            return Double.valueOf(FORMULA_EVALUATOR.evaluate());
        }
        catch (EvaluationException e) {
            GreyfishLogger.warn("CostsFormula is not a valid expression", e);
            return 0;
        }
    }

    @Override
    public void export(Exporter e) {
        super.export(e);
        e.add(new ValueSelectionAdaptor<DoubleProperty>("Property", DoubleProperty.class) {
            @Override
            protected void set(DoubleProperty arg0) {
                parameterQuantitiveProperty = checkFrozen(checkNotNull(arg0));
            }

            @Override
            public DoubleProperty get() {
                return parameterQuantitiveProperty;
            }

            @Override
            public Iterable<DoubleProperty> values() {
                return Iterables.filter(getComponentOwner().getProperties(), DoubleProperty.class);
            }
        });
        e.add(new ValueAdaptor<String>("Formula", String.class) {
            @Override
            protected void set(String arg0) {
                energyCostsFormula = checkFrozen(checkNotNull(arg0));
            } // TODO: check if string is a valid expression

            @Override
            public String get() {
                return energyCostsFormula;
            }
        });
    }

    @Override
    public void initialize(Simulation simulation) {
        super.initialize(simulation);
        try{
            FORMULA_EVALUATOR.parse(energyCostsFormula);
        } catch (EvaluationException e) {
            throw new IllegalStateException("to is not valid");
        }
    }

    @Override
    public AbstractGFComponent deepCloneHelper(CloneMap cloneMap) {
        return new ModifyQuantitivePropertyAction(this, cloneMap);
    }

    public ModifyQuantitivePropertyAction(ModifyQuantitivePropertyAction cloneable, CloneMap map) {
        super(cloneable, map);
        this.parameterQuantitiveProperty = map.clone(cloneable.parameterQuantitiveProperty, DoubleProperty.class);
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
