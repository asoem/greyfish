package org.asoem.greyfish.core.genes;

import org.asoem.greyfish.core.eval.EvaluationException;
import org.asoem.greyfish.core.eval.GreyfishExpression;
import org.asoem.greyfish.core.eval.GreyfishExpressionFactory;
import org.asoem.greyfish.core.individual.AbstractAgentComponent;
import org.asoem.greyfish.core.individual.AgentComponent;
import org.asoem.greyfish.core.individual.ComponentVisitor;
import org.asoem.greyfish.core.io.LoggerFactory;
import org.asoem.greyfish.gui.utils.ClassGroup;
import org.asoem.greyfish.utils.base.DeepCloneable;
import org.asoem.greyfish.utils.base.DeepCloner;
import org.asoem.greyfish.utils.gui.ConfigurationHandler;
import org.asoem.greyfish.utils.gui.ValueAdaptor;

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
public class DoubleGene extends AbstractAgentComponent implements Gene<Double> {

    private GreyfishExpression<DoubleGene> initialValueGenerator = GreyfishExpressionFactory.compileExpression("0.0").forContext(DoubleGene.class);

    private GreyfishExpression<DoubleGene> mutationDistributionFunction = GreyfishExpressionFactory.compileExpression("0.0").forContext(DoubleGene.class);

    private final GeneController<Double> geneController = new GeneController<Double>() {

        @Override
        public Double mutate(Double original) {
            try {
                return get() + mutationDistributionFunction.evaluateAsDouble(DoubleGene.this);
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
    public boolean isMutatedCopy(@Nullable Gene<?> gene) {
        return gene instanceof DoubleGene &&
                initialValueGenerator == ((DoubleGene) gene).initialValueGenerator;
    }

    @Override
    public double distance(Gene<?> thatGene) {
        checkArgument(this.isMutatedCopy(checkNotNull(thatGene)));
        return getGeneController().normalizedDistance(get(), ((DoubleGene) thatGene).get());
    }

    @Override
    public void set(Double value) {
        this.value = value;
    }

    @Override
    public void accept(ComponentVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public Double get() {
        return value;
    }

    @Override
    public void configure(ConfigurationHandler e) {
        super.configure(e);
        e.add(new ValueAdaptor<GreyfishExpression>("Initial Value", GreyfishExpression.class) {
            @Override
            @SuppressWarnings("unchecked") // save because of contextClass verification
            protected void set(GreyfishExpression arg0) {
                checkArgument(arg0.getContextClass().equals(DoubleGene.class));
                initialValueGenerator = (GreyfishExpression<DoubleGene>) arg0;
            }

            @Override
            public GreyfishExpression get() {
                return initialValueGenerator;
            }
        });

        e.add(new ValueAdaptor<GreyfishExpression>("Mutation", GreyfishExpression.class) {
            @Override
            @SuppressWarnings("unchecked") // save because of contextClass verification
            protected void set(GreyfishExpression arg0) {
                checkArgument(arg0.getContextClass().equals(DoubleGene.class));
                mutationDistributionFunction = (GreyfishExpression<DoubleGene>) arg0;
            }

            @Override
            public GreyfishExpression get() {
                return mutationDistributionFunction;
            }
        });
    }

    @Override
    public Iterable<AgentComponent> children() {
        return Collections.emptyList();
    }
}
