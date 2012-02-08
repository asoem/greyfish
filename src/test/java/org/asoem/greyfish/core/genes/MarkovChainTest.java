package org.asoem.greyfish.core.genes;

import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;

/**
 * User: christoph
 * Date: 08.02.12
 * Time: 13:11
 */
public class MarkovChainTest {

    @Test
    public void testSimpleChain() throws Exception {
        final MarkovChain<String> chain = MarkovChain.<String>builder()
                .put("A", "B", 1)
                .put("B", "C", 1)
                .build();
        
        final String initialState = "A"; 
        
        final String endState = chain.apply(chain.apply(chain.apply(initialState)));
        
        assertThat(endState).isEqualTo("C");
    }
}
