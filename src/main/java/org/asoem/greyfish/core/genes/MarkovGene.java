package org.asoem.greyfish.core.genes;

import org.asoem.greyfish.core.eval.GreyfishExpression;
import org.asoem.greyfish.core.utils.SimpleXMLConstructor;
import org.asoem.greyfish.gui.utils.ClassGroup;
import org.asoem.greyfish.utils.base.DeepCloneable;
import org.asoem.greyfish.utils.base.DeepCloner;
import org.asoem.greyfish.utils.gui.AbstractTypedValueModel;
import org.asoem.greyfish.utils.gui.ConfigurationHandler;
import org.asoem.greyfish.utils.math.RandomUtils;
import org.simpleframework.xml.Element;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * User: christoph
 * Date: 07.02.12
 * Time: 11:28
 */
@ClassGroup(tags = {"genes"})
public class MarkovGene extends AbstractGene<String> {

    @Element
    private MarkovChain<String> markovChain;

    @Element
    private GreyfishExpression initialStateProvider;
    
    private String currentState;

    private final GeneController<String> geneController;

    @SimpleXMLConstructor
    public MarkovGene() {
        markovChain = MarkovChain.<String>builder()
                .put("Male", "Female", 0.5)
                .put("Female", "Male", 0.5)
                .build();
        geneController  = new GeneControllerAdaptor<String>() {
            @Override
            public String mutate(String original) {
                return markovChain.apply(original);
            }

            @Override
            public String createInitialValue() {
                return new String[] {"Male", "Female"}[RandomUtils.nextInt(2)];
            }
        };
    }

    @Override
    public void setValue(Object value) {
        checkArgument(value instanceof String);
        currentState = (String) value;
    }

    private MarkovGene(MarkovGene markovGene, DeepCloner cloner) {
        super(markovGene, cloner);
        this.markovChain = markovGene.markovChain;
        this.geneController = markovGene.geneController;
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
        
        e.add("Transition Matrix", new AbstractTypedValueModel<MarkovChain<String>>() {
            @Override
            protected void set(MarkovChain<String> arg0) {
                markovChain = arg0;
            }

            @Override
            public MarkovChain<String> get() {
                return markovChain;
            }
        });
        
        e.add("Initial State", new AbstractTypedValueModel<GreyfishExpression>() {
            @Override
            protected void set(GreyfishExpression arg0) {
                initialStateProvider = arg0;
            }

            @Override
            public GreyfishExpression get() {
                return initialStateProvider;
            }
        });
    }
}
