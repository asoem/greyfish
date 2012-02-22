package org.asoem.greyfish.core.genes;

import com.google.common.collect.ImmutableMap;
import org.asoem.greyfish.core.eval.EvaluationException;
import org.asoem.greyfish.core.eval.GreyfishExpression;
import org.asoem.greyfish.core.eval.GreyfishExpressionFactoryHolder;
import org.asoem.greyfish.core.utils.SimpleXMLConstructor;
import org.asoem.greyfish.gui.utils.ClassGroup;
import org.asoem.greyfish.utils.base.DeepCloneable;
import org.asoem.greyfish.utils.base.DeepCloner;
import org.asoem.greyfish.utils.gui.AbstractTypedValueModel;
import org.asoem.greyfish.utils.gui.ConfigurationHandler;
import org.asoem.greyfish.utils.logging.LoggerFactory;
import org.simpleframework.xml.Element;

/**
 * User: christoph
 * Date: 22.09.11
 * Time: 09:37
 */
@ClassGroup(tags = "genes")
public class DoubleGene extends AbstractGene<Double> {

    @Element
    private GreyfishExpression initialValue = GreyfishExpressionFactoryHolder.compile("0.0");

    @Element
    private GreyfishExpression mutation = GreyfishExpressionFactoryHolder.compile("0.0");

    @Element
    private GreyfishExpression distanceMetric = GreyfishExpressionFactoryHolder.compile("abs(y - x)");

    private final GeneController<Double> geneController = new GeneController<Double>() {

        @Override
        public Double mutate(Double original) {
            try {
                return get() + mutation.evaluateForContext(DoubleGene.this).asDouble();
            } catch (EvaluationException e) {
                LoggerFactory.getLogger(DoubleGene.class).error("Error in mutation", e);
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
                return initialValue.evaluateForContext(DoubleGene.this).asDouble();
            } catch (EvaluationException e) {
                LoggerFactory.getLogger(DoubleGene.class).error("Error in initialValue", e);
                return 0.0;
            }
        }
    };

    private Double value = 0.0;

    @SimpleXMLConstructor
    public DoubleGene() {}

    protected DoubleGene(DoubleGene doubleMutableGene, DeepCloner cloner) {
        super(doubleMutableGene, cloner);
        this.initialValue = doubleMutableGene.initialValue;
        this.mutation = doubleMutableGene.mutation;
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

    public GreyfishExpression getInitialValue() {
        return initialValue;
    }

    public void setInitialValue(GreyfishExpression initialValue) {
        this.initialValue = initialValue;
    }

    public GreyfishExpression getMutation() {
        return mutation;
    }

    public void setMutation(GreyfishExpression mutation) {
        this.mutation = mutation;
    }

    public GreyfishExpression getDistanceMetric() {
        return distanceMetric;
    }

    public void setDistanceMetric(GreyfishExpression distanceMetric) {
        this.distanceMetric = distanceMetric;
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
                initialValue = arg0;
            }

            @Override
            public GreyfishExpression get() {
                return initialValue;
            }
        });

        e.add("Mutation", new AbstractTypedValueModel<GreyfishExpression>() {
            @Override
            protected void set(GreyfishExpression arg0) {
                mutation = arg0;
            }

            @Override
            public GreyfishExpression get() {
                return mutation;
            }
        });
    }
}
