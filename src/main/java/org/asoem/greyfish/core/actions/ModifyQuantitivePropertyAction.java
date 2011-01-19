/**
 * 
 */
package org.asoem.greyfish.core.actions;

import net.sourceforge.jeval.EvaluationConstants;
import net.sourceforge.jeval.EvaluationException;
import net.sourceforge.jeval.Evaluator;
import org.asoem.greyfish.core.io.GreyfishLogger;
import org.asoem.greyfish.core.properties.DoubleProperty;
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.lang.ClassGroup;
import org.asoem.greyfish.utils.AbstractDeepCloneable;
import org.asoem.greyfish.utils.Exporter;
import org.asoem.greyfish.utils.ValueAdaptor;
import org.asoem.greyfish.utils.ValueSelectionAdaptor;
import org.simpleframework.xml.Element;

import java.util.Map;

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
	protected AbstractDeepCloneable deepCloneHelper(
			Map<AbstractDeepCloneable, AbstractDeepCloneable> mapDict) {
		return new Builder().fromClone(this, mapDict).build();
	}

    @Override
    public void export(Exporter e) {
        super.export(e);
        e.addField( new ValueSelectionAdaptor<DoubleProperty>(
                "Property",
                DoubleProperty.class,
                parameterQuantitiveProperty,
                componentOwner.getProperties(DoubleProperty.class)) {
            @Override
            protected void writeThrough(DoubleProperty arg0) {
                parameterQuantitiveProperty = arg0;
            }
        });
        e.addField( new ValueAdaptor<String>("Formula", String.class, energyCostsFormula) {
            @Override
            protected void writeThrough(String arg0) {
                energyCostsFormula = arg0;
            }
        });
    }

    @Override
    public void initialize(Simulation simulation) {
        super.initialize(simulation);
        try{
            FORMULA_EVALUATOR.parse(energyCostsFormula);
        } catch (EvaluationException e) {
            throw new IllegalStateException("energyCostsFormula is not valid");
        }
    }

        protected ModifyQuantitivePropertyAction(AbstractBuilder<?> builder) {
        super(builder);
        this.energyCostsFormula = builder.energyCostsFormula;
        this.parameterQuantitiveProperty = builder.quantitiveProperty;
    }

    public static final class Builder extends AbstractBuilder<Builder> {
        @Override protected Builder self() {  return this; }
    }

    protected static abstract class AbstractBuilder<T extends AbstractBuilder<T>> extends AbstractGFAction.AbstractBuilder<T> {
        private DoubleProperty quantitiveProperty;
        private String energyCostsFormula = "0";

        public T quantitiveProperty(DoubleProperty quantitiveProperty) { this.quantitiveProperty = quantitiveProperty; return self(); }
        public T energyCostsFormula(String energyCostsFormula) { this.energyCostsFormula = energyCostsFormula; return self(); }

        protected T fromClone(ModifyQuantitivePropertyAction action, Map<AbstractDeepCloneable, AbstractDeepCloneable> mapDict) {
            super.fromClone(action, mapDict).
                    quantitiveProperty(deepClone(action.parameterQuantitiveProperty, mapDict)).
                    energyCostsFormula(action.energyCostsFormula);
            return self();
        }

        public ModifyQuantitivePropertyAction build() { return new ModifyQuantitivePropertyAction(this); }
    }
}
