package org.asoem.greyfish.core.genes;

import com.google.common.collect.ImmutableMap;
import org.asoem.greyfish.core.eval.EvaluationException;
import org.asoem.greyfish.core.eval.GreyfishExpression;
import org.asoem.greyfish.core.eval.GreyfishExpressionFactory;
import org.asoem.greyfish.core.utils.SimpleXMLConstructor;
import org.asoem.greyfish.gui.utils.ClassGroup;
import org.asoem.greyfish.utils.base.DeepCloneable;
import org.asoem.greyfish.utils.base.DeepCloner;
import org.asoem.greyfish.utils.gui.AbstractTypedValueModel;
import org.asoem.greyfish.utils.gui.ConfigurationHandler;
import org.asoem.greyfish.utils.logging.LoggerFactory;

/**
 * User: christoph
 * Date: 22.09.11
 * Time: 09:37
 */
@ClassGroup(tags = "genes")
public class DoubleGene extends AbstractGene<Double> {

    private GreyfishExpression initialValueGenerator = GreyfishExpressionFactory.compile("0.0");

    private GreyfishExpression mutationDistributionFunction = GreyfishExpressionFactory.compile("0.0");

    private GreyfishExpression distanceMetric = GreyfishExpressionFactory.compile("abs(y - x)");

    private final GeneController<Double> geneController = new GeneController<Double>() {

        @Override
        public Double mutate(Double original) {
            try {
                return mutationDistributionFunction.evaluateForContext(DoubleGene.this).asDouble();
            } catch (EvaluationException e) {
                LoggerFactory.getLogger(DoubleGene.class).error("Error in mutationDistributionFunction", e);
                return original;
            }
        }

        @Override
        public double normalizedDistance(Double orig, Double copy) {
            return distanceMetric.evaluateForContext(this, ImmutableMap.of("x", orig, "y", copy)).asDouble();
        }

        @Override
        public double normalizedWeightedDistance(Double orig, Double copy) {
            return normalizedDistance(orig, copy);
        }

        @Override
        public Double createInitialValue() {
            try {
                return initialValueGenerator.evaluateForContext(DoubleGene.this).asDouble();
            } catch (EvaluationException e) {
                LoggerFactory.getLogger(DoubleGene.class).error("Error in initialValueGenerator", e);
                return 0.0;
            }
        }
    };

    private Double value = 0.0;

    @SimpleXMLConstructor
    public DoubleGene() {}

    protected DoubleGene(DoubleGene doubleMutableGene, DeepCloner cloner) {
        super(doubleMutableGene, cloner);
        this.initialValueGenerator = doubleMutableGene.initialValueGenerator;
        this.mutationDistributionFunction = doubleMutableGene.mutationDistributionFunction;
    }

    @Override
    public DeepCloneable deepClone(DeepCloner cloner) {
        return new DoubleGene(this, cloner);
    }

    @Override
    public Class<Double> getSupplierClass() {
        return Double.class;
    }

    @Override
    public GeneController<Double> getGeneController() {
        return geneController;
    }

    @Override
    public void setValue(Object value) {
        this.value = Double.class.cast(value);
    }

    @Override
    public Double get() {
        return value;
    }

    @Override
    public void configure(ConfigurationHandler e) {
        super.configure(e);
        e.add("Initial Value", new AbstractTypedValueModel<GreyfishExpression>() {
            @Override
            protected void set(GreyfishExpression arg0) {
                initialValueGenerator = arg0;
            }

            @Override
            public GreyfishExpression get() {
                return initialValueGenerator;
            }
        });

        e.add("Mutation", new AbstractTypedValueModel<GreyfishExpression>() {
            @Override
            protected void set(GreyfishExpression arg0) {
                mutationDistributionFunction = arg0;
            }

            @Override
            public GreyfishExpression get() {
                return mutationDistributionFunction;
            }
        });
    }
}
