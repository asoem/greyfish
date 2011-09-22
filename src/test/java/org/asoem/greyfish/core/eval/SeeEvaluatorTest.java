package org.asoem.greyfish.core.eval;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertSame;
import static org.mockito.BDDMockito.given;

/**
 * User: christoph
 * Date: 22.09.11
 * Time: 12:10
 */
@RunWith(MockitoJUnitRunner.class)
public class SeeEvaluatorTest {

    @Mock VariableResolver resolver;

    @Test
    public void usesEvaluatorForUserUnknownFunctions() throws EvaluationException {
        // given
        SeeEvaluator evaluator = new SeeEvaluator();
        evaluator.setExpression("f(3.2)");
        evaluator.setResolver(resolver);
        given(resolver.resolve("f")).willReturn(3.2);

        // when
        double ret = evaluator.evaluateAsDouble();

        // then
        assertSame(3.2, ret);
    }
}
