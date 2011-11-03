package org.asoem.greyfish.core.io;

import org.asoem.greyfish.core.eval.GreyfishExpression;
import org.asoem.greyfish.core.eval.SingletonGreyfishExpressionFactory;
import org.asoem.greyfish.core.individual.AgentComponent;
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

    @SuppressWarnings({"unchecked"}) // It is not save to cast here, but we don't have other options
    public GreyfishExpression read(InputNode node) throws Exception {
        checkNotNull(node);
        String expression = node.getAttribute("expression").getValue();
        String contextClassStr = node.getAttribute("context").getValue();
        Class<AgentComponent> contextClass = (Class<AgentComponent>) Class.forName(contextClassStr);

        return SingletonGreyfishExpressionFactory.compileExpression(expression).forContext(contextClass);
    }

    public void write(OutputNode node, GreyfishExpression external) {
        String expression = checkNotNull(external).getExpression();
        Class<?> context = checkNotNull(external).getContextClass();

        node.setAttribute("expression", expression);
        node.setAttribute("context", context.getCanonicalName());
    }
}
