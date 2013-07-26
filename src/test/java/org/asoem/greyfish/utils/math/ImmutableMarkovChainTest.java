package org.asoem.greyfish.utils.math;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * User: christoph
 * Date: 08.02.12
 * Time: 13:11
 */
public class ImmutableMarkovChainTest {

    @Test
    public void testSimpleChain() throws Exception {
        final ImmutableMarkovChain<String> chain = ImmutableMarkovChain.<String>builder()
                .put("A", "B", 1)
                .put("B", "C", 1)
                .build();
        
        final String initialState = "A"; 
        
        final String endState = chain.apply(chain.apply(chain.apply(initialState)));
        
        assertThat(endState, is("C"));
    }

    @Test
    public void testParse() throws Exception {
        // given
        final String rule = "A -> B : 1.0; B -> C : 1.0";

        // when
        final ImmutableMarkovChain<String> chain = ImmutableMarkovChain.parse(rule);

        // then
        final String endState = chain.apply(chain.apply("A"));
        assertThat(endState, is("C"));
    }
}
