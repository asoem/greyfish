package org.asoem.greyfish.core.genes;

import org.asoem.greyfish.core.eval.EvaluationException;
import org.asoem.greyfish.core.eval.GreyfishExpression;
import org.asoem.greyfish.core.eval.GreyfishExpressionFactory;
import org.asoem.greyfish.core.individual.AbstractAgentComponent;
import org.asoem.greyfish.core.individual.AgentComponent;
import org.asoem.greyfish.core.individual.ComponentVisitor;
import org.asoem.greyfish.core.utils.SimpleXMLConstructor;
import org.asoem.greyfish.gui.utils.ClassGroup;
import org.asoem.greyfish.utils.base.DeepCloneable;
import org.asoem.greyfish.utils.base.DeepCloner;
import org.asoem.greyfish.utils.gui.ConfigurationHandler;
import org.asoem.greyfish.utils.gui.AbstractTypedValueModel;
import org.asoem.greyfish.utils.logging.LoggerFactory;

import javax.annotation.Nullable;
import java.util.Collections;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * User: christoph
 * Date: 22.09.11
 * Time: 09:37
 */
@ClassGroup(tags = "genes")
public class DoubleGene extends AbstractGene<Double> {

    private GreyfishExpression initialValueGenerator = GreyfishExpressionFactory.compile("0.0");

    private GreyfishExpression mutationDistributionFunction = GreyfishExpressionFactory.compile("0.0");

    private final GeneController<Double> geneController = new GeneController<Double>() {

        @Override
        public Double mutate(Double original) {
            try {
                return mutationDistributionFunction.evaluateAsDouble(DoubleGene.this);
            } catch (EvaluationException e) {
                LoggerFactory.getLogger(DoubleGene.class).error("Error in mutationDistributionFunction", e);
                return original;
            }
        }

        @Override
        public double normalizedDistance(Double orig, Double copy) {
            return Math.abs(orig - copy);
        }

        @Override
        public double normalizedWeightedDistance(Double orig, Double copy) {
            return normalizedDistance(orig, copy);
        }

        @Override
        public Double createInitialValue() {
            try {
                return initialValueGenerator.evaluateAsDouble(DoubleGene.this);
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
