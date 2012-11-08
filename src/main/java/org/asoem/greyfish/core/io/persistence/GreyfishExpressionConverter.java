package org.asoem.greyfish.core.io.persistence;

import org.asoem.greyfish.core.eval.EvaluatorFactory;
import org.asoem.greyfish.core.eval.GreyfishExpression;
import org.simpleframework.xml.convert.Converter;
import org.simpleframework.xml.stream.InputNode;
import org.simpleframework.xml.stream.OutputNode;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * User: christoph
 * Date: 14.09.11
 * Time: 18:37
 */
class GreyfishExpressionConverter implements Converter<GreyfishExpression> {

    public GreyfishExpression read(InputNode node) throws Exception {
        checkNotNull(node);

        String evaluatorClass = node.getAttribute("evaluatorFactory").getValue();
        String expression = node.getValue();

        return new GreyfishExpression(expression, (EvaluatorFactory) Class.forName(evaluatorClass).newInstance());
    }

    public void write(OutputNode node, GreyfishExpression external) {
        checkNotNull(external);

        node.setAttribute("evaluator", external.getEvaluator().getClass().getCanonicalName());
        node.setValue(external.getExpression());
    }
}
