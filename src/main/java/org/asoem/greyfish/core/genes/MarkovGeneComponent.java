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
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

/**
 * User: christoph
 * Date: 07.02.12
 * Time: 11:28
 */
@ClassGroup(tags = {"genes"})
public class MarkovGeneComponent extends AbstractGeneComponent<String> {

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
            assert initialState != null;
            return initialState.evaluateForContext(MarkovGeneComponent.this).asString();
        }
    };

    @SuppressWarnings("UnusedDeclaration") // Needed for construction by reflection / deserialization
    private MarkovGeneComponent() {
    }
    
    public MarkovGeneComponent(EvaluatingMarkovChain<String> chain, GreyfishExpression initialState) {
        this.markovChain = checkNotNull(chain);
        this.initialState = checkNotNull(initialState);
    }

    public MarkovGeneComponent(AbstractMarkovGeneComponentBuilder<? extends MarkovGeneComponent, ? extends AbstractMarkovGeneComponentBuilder> builder) {
        super(builder);
        this.markovChain = builder.markovChain;
        this.initialState = builder.initialState;
    }

    @Override
    public void initialize() {
        super.initialize();
        this.currentState = geneController.createInitialValue();
    }

    @Override
    public void setValue(Object value) {
        checkArgument(value instanceof String);
        currentState = (String) value;
    }

    private MarkovGeneComponent(MarkovGeneComponent markovGene, DeepCloner cloner) {
        super(markovGene, cloner);
        this.markovChain = markovGene.markovChain;
        this.initialState = markovGene.initialState;
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
    public DeepCloneable deepClone(DeepCloner cloner) {
        return new MarkovGeneComponent(this, cloner);
    }

    @Override
    public String getValue() {
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

    public static MarkovGeneComponentBuilder builder() {
        return new MarkovGeneComponentBuilder();
    }

    public static class MarkovGeneComponentBuilder extends AbstractMarkovGeneComponentBuilder<MarkovGeneComponent, MarkovGeneComponentBuilder> {
        @Override
        protected MarkovGeneComponentBuilder self() {
            return this;
        }

        @Override
        protected MarkovGeneComponent checkedBuild() {
            return new MarkovGeneComponent(this);
        }
    }

    protected abstract static class AbstractMarkovGeneComponentBuilder<T extends MarkovGeneComponent, B extends AbstractComponentBuilder<T,B>> extends AbstractComponentBuilder<T,B> {

        private EvaluatingMarkovChain<String> markovChain;
        private GreyfishExpression initialState;

        public B markovChain(EvaluatingMarkovChain<String> markovChain) {
            this.markovChain = checkNotNull(markovChain);
            return self();
        }

        public B initialState(GreyfishExpression initialState) {
            this.initialState = checkNotNull(initialState);
            return self();
        }

        @Override
        protected void checkBuilder() throws IllegalStateException {
            super.checkBuilder();
            checkState(markovChain != null);
            checkState(initialState != null);
        }
    }
}
