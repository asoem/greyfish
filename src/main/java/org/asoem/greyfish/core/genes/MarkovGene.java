package org.asoem.greyfish.core.genes;

import com.google.common.collect.ImmutableTable;
import org.asoem.greyfish.core.eval.GreyfishExpression;
import org.asoem.greyfish.core.utils.SimpleXMLConstructor;
import org.asoem.greyfish.utils.base.DeepCloneable;
import org.asoem.greyfish.utils.base.DeepCloner;
import org.asoem.greyfish.utils.gui.AbstractTypedValueModel;
import org.asoem.greyfish.utils.gui.ConfigurationHandler;
import org.simpleframework.xml.Element;

/**
 * User: christoph
 * Date: 07.02.12
 * Time: 11:28
 */
public class MarkovGene extends AbstractGene<String> {

    @Element
    private MarkovChain markovChain;

    @Element
    private GreyfishExpression initialStateProvider;
    
    private String currentState;

    private final GeneController<String> geneController = new GeneControllerAdaptor<String>() {
        @Override
        public String mutate(String original) {
            return markovChain.next(get());
        }

        @Override
        public String createInitialValue() {
            return "A";
        }
    };

    @SimpleXMLConstructor
    public MarkovGene() {
        markovChain = new MarkovChain(ImmutableTable.of("A","A",0.0));
    }

    private MarkovGene(MarkovGene markovGene, DeepCloner cloner) {
        super(markovGene, cloner);
        this.markovChain = markovGene.markovChain;
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
        
        e.add("Transition Matrix", new AbstractTypedValueModel<MarkovChain>() {
            @Override
            protected void set(MarkovChain arg0) {
                markovChain = arg0;
            }

            @Override
            public MarkovChain get() {
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
