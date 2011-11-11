package org.asoem.greyfish.core.io;

import org.asoem.greyfish.core.eval.GreyfishExpression;
import org.asoem.greyfish.core.eval.SingletonGreyfishExpressionFactory;
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
        String expression = node.getAttribute("expression").getValue();

        return SingletonGreyfishExpressionFactory.compileExpression(expression);
    }

    public void write(OutputNode node, GreyfishExpression external) {
        String expression = checkNotNull(external).getExpression();

        node.setAttribute("expression", expression);
    }
}
