package org.asoem.greyfish.core.io;

import org.asoem.greyfish.core.eval.GreyfishExpression;
import org.asoem.greyfish.core.eval.GreyfishExpressionFactory;
import org.asoem.greyfish.core.individual.GFComponent;
import org.simpleframework.xml.convert.Converter;
import org.simpleframework.xml.stream.InputNode;
import org.simpleframework.xml.stream.OutputNode;

/**
 * User: christoph
 * Date: 14.09.11
 * Time: 18:37
 */
@SuppressWarnings("unchecked")
class GreyfishExpressionConverter implements Converter<GreyfishExpression> {

    public GreyfishExpression read(InputNode node) throws Exception {
        String expression = node.getAttribute("expression").getValue();
        String contextClassStr = node.getAttribute("context").getValue();
        Class<? extends GFComponent> contextClass = (Class<? extends GFComponent>) Class.forName(contextClassStr);

        return GreyfishExpressionFactory.compileExpression(expression).forContext(contextClass);
    }

    public void write(OutputNode node, GreyfishExpression external) {
        String expression = external.getExpression();
        Class<?> context = external.getContextClass();

        node.setAttribute("expression", expression);
        node.setAttribute("context", context.getCanonicalName());
    }
}
