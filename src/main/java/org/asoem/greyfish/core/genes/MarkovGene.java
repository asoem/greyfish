package org.asoem.greyfish.core.genes;

import org.asoem.greyfish.core.eval.GreyfishExpression;
import org.asoem.greyfish.core.eval.GreyfishExpressionFactory;
import org.asoem.greyfish.core.eval.GreyfishExpressionFactoryHolder;
import org.asoem.greyfish.core.utils.EvaluatingMarkovChain;
import org.asoem.greyfish.gui.utils.ClassGroup;
import org.asoem.greyfish.utils.base.DeepCloneable;
import org.asoem.greyfish.utils.base.DeepCloner;
import org.asoem.greyfish.utils.gui.AbstractTypedValueModel;
import org.asoem.greyfish.utils.gui.ConfigurationHandler;
import org.simpleframework.xml.Element;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * User: christoph
 * Date: 07.02.12
 * Time: 11:28
 */
@ClassGroup(tags = {"genes"})
public class MarkovGene extends AbstractGene<String> {

    private static final GreyfishExpressionFactory EXPRESSION_FACTORY = GreyfishExpressionFactoryHolder.get();

    @Element(required = false)
    private EvaluatingMarkovChain<String> markovChain;

    @Element(required = false)
    private GreyfishExpression initialState;

    @Element(required = false)
    private String currentState;

    private final GeneController<String> geneController = new GeneControllerAdaptor<String>() {
        @Override
        public String mutate(String original) {
            return markovChain.apply(original, GreyfishExpression.createContextResolver(this));
        }

        @Override
        public String createInitialValue() {
            return MarkovGene.this.initialState.evaluateForContext(MarkovGene.this).asString();
        }
    };

    @SuppressWarnings("UnusedDeclaration") // Needed for construction by reflection / deserialization
    public MarkovGene() {
    }
    
    public MarkovGene(EvaluatingMarkovChain<String> chain, GreyfishExpression initialState) {
        markovChain = chain;
        this.initialState = initialState;
        this.currentState = geneController.createInitialValue();
    }

    @Override
    public void setValue(Object value) {
        checkArgument(value instanceof String);
        currentState = (String) value;
    }

    private MarkovGene(MarkovGene markovGene, DeepCloner cloner) {
        super(markovGene, cloner);
        this.markovChain = markovGene.markovChain;
        this.currentState = markovGene.currentState;
    }

    @Override
    public Class<String> getSupplierClass() {
        return String.class;
    }

    @Override
    public GeneController<String> getGeneController() {
        return geneController;
    }

    @Override
    public void init() {
        currentState = initialState.evaluateForContext(MarkovGene.this).asString();
    }

    @Override
    public void mutate() {
        currentState = markovChain.apply(currentState, GreyfishExpression.createContextResolver(this));
    }

    @Override
    public DeepCloneable deepClone(DeepCloner cloner) {
        return new MarkovGene(this, cloner);
    }

    @Override
    public String get() {
        return currentState;
    }

    @Override
    public void configure(ConfigurationHandler e) {
        super.configure(e);

        e.add("Transition Rules", new AbstractTypedValueModel<String>() {
            @Override
            protected void set(String arg0) {
                markovChain = EvaluatingMarkovChain.parse(arg0, EXPRESSION_FACTORY);
            }

            @Override
            public String get() {
                return markovChain == null ? "" : markovChain.toRule();
            }
        });

        e.add("Initial State", new AbstractTypedValueModel<String>() {
            @Override
            protected void set(String arg0) {
                initialState = GreyfishExpressionFactoryHolder.compile(arg0);
            }

            @Override
            public String get() {
                return initialState == null ? "" : initialState.getExpression();
            }
        });
    }

    public EvaluatingMarkovChain<String> getMarkovChain() {
        return markovChain;
    }

    public GreyfishExpression getInitialState() {
        return initialState;
    }
}
