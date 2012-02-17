package org.asoem.greyfish.core.eval.impl;

import com.google.common.collect.ImmutableMap;
import org.asoem.greyfish.core.eval.EvaluationException;
import org.asoem.greyfish.core.eval.GreyfishExpression;
import org.asoem.greyfish.core.eval.VariableResolver;
import org.asoem.greyfish.core.eval.VariableResolvers;
import org.asoem.greyfish.core.genes.Gene;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mvel2.MVEL;

import java.util.List;

import static org.fest.assertions.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

/**
 * User: christoph
 * Date: 22.09.11
 * Time: 16:46
 */

@RunWith(MockitoJUnitRunner.class)
public class MvelEvaluatorTest {

    @Mock
    VariableResolver resolver;

    @Test
    public void testSetResolver() throws EvaluationException {
        // given
        MvelEvaluator evaluator = new MvelEvaluator();
        evaluator.setExpression("a + 3.0");
        evaluator.setResolver(resolver);
        given(resolver.resolve("a")).willReturn(3.0, 4.0);

        // when
        double ret1 = evaluator.evaluate().asDouble();
        double ret2 = evaluator.evaluate().asDouble();

        // then
        verify(resolver, times(2)).resolve("a");
        assertEquals(6.0, ret1, 0);
        assertEquals(7.0, ret2, 0);
    }

    @Test
    public void testCustomParserContext_max() throws EvaluationException {
        // given
        MvelEvaluator evaluator = new MvelEvaluator();
        evaluator.setExpression("max(4.0, 5.0)");

        // when
        double ret = evaluator.evaluate().asDouble();

        // then
        assertEquals(5.0, ret, 0);
    }

    @Test
    public void testCustomParserContext_gaussian() throws EvaluationException {
        // given
        MvelEvaluator evaluator = new MvelEvaluator();
        evaluator.setExpression("rnorm(0.0, 0.2)");

        // when
        Double evaluated = evaluator.evaluate().asDouble();

        // then
        assertThat(evaluated).isNotNull();
    }

    @Test
    public void testVariableResolution_forMap() throws Exception {
        // given
        MvelEvaluator evaluator = new MvelEvaluator();
        evaluator.setExpression("test");
        evaluator.setResolver(VariableResolvers.forMap(ImmutableMap.of("test", 5.0)));

        // when
        double evaluated = evaluator.evaluate().asDouble();

        // then
        assertThat(evaluated).isEqualTo(5.0);
    }

    @Test
    public void testDollarFunction() throws Exception {
        // given
        GreyfishExpression expression = new GreyfishExpression("$('self.value')", new MvelEvaluator());
        Gene gene = mock(Gene.class);
        double value = 3.5;
        given(gene.get()).willReturn(value);

        // when
        double evaluated = expression.evaluateForContext(gene).asDouble();

        // then
        assertThat(evaluated).isEqualTo(value);
    }

    @Test
    public void testEvaluationToString() {
        // given
        MvelEvaluator evaluator = new MvelEvaluator();
        evaluator.setExpression("if (true) { \"Hello World!\"; } else { \"FooBar\"; }");
        
        // when
        String ret = evaluator.evaluate().asString();
        
        // then
        assertThat(ret).isEqualTo("Hello World!");
    }

    @Test
    public void testProbabilityMap() throws Exception {
        // given
        MvelEvaluator evaluator = new MvelEvaluator();
        MvelEvaluator.PARSER_CONTEXT.addImport("rws", MVEL.getStaticMethod(MvelEvaluatorTest.class, "rws", new Class[]{List.class}));
        evaluator.setExpression("rws([\"Male\",0.5,\"Female\",0.5])");

        // when
        String ret = evaluator.evaluate().asString();

        // then
        assertThat(ret).isEqualTo("Female");
    }
    
    public static String rws(List<?> map) {
        double rand = 0.6; // cont for testing purposes
        double sum = 0;
        for (int i = 0; i < map.size(); i = i+2) {
            sum += (Double) map.get(i+1);
            if (sum > rand)
                return (String) map.get(i);
        }
        throw new AssertionError();
    }
}