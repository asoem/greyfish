package org.asoem.greyfish.utils.math;

import com.google.common.base.Splitter;
import org.asoem.greyfish.utils.math.MarkovChain;
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

    @Test
    public void testParse() throws Exception {
        // given
        final String rule = "A -> B : 1.0; B -> C : 1.0";

        // when
        final MarkovChain<String> chain = MarkovChain.parse(rule);

        // then
        final String endState = chain.apply(chain.apply("A"));
        assertThat(endState).isEqualTo("C");
    }
}
