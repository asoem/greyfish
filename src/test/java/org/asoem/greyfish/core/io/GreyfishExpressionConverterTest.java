package org.asoem.greyfish.core.io;

import org.asoem.greyfish.core.eval.GreyfishExpression;
import org.asoem.greyfish.core.eval.SingletonGreyfishExpressionFactory;
import org.asoem.greyfish.core.individual.AgentComponent;
import org.junit.Test;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.convert.Registry;
import org.simpleframework.xml.convert.RegistryStrategy;
import org.simpleframework.xml.core.Persister;

import java.io.StringWriter;

import static org.fest.assertions.Assertions.assertThat;

/**
 * User: christoph
 * Date: 02.11.11
 * Time: 16:32
 */
public class GreyfishExpressionConverterTest {

    private static final String SERIALIZED_XML_STRING = "<greyfishExpression expression=\"sin(4.6)\" context=\"" + AgentComponent.class.getCanonicalName() + "\"/>";
    private static final GreyfishExpression<AgentComponent> GREYFISH_EXPRESSION = SingletonGreyfishExpressionFactory.compileExpression("sin(4.6)").forContext(AgentComponent.class);

    @Test
    public void testRead() throws Exception {
        // given
        Registry registry = new Registry();
        registry.bind(GreyfishExpression.class, GreyfishExpressionConverter.class);
        Serializer serializer = new Persister(new RegistryStrategy(registry));

        // when
        GreyfishExpression success = serializer.read(GreyfishExpression.class, SERIALIZED_XML_STRING);

        // then
        assertThat(success).isEqualTo(GREYFISH_EXPRESSION);
    }

    @Test
    public void testWrite() throws Exception {
        // given
        Registry registry = new Registry();
        registry.bind(GreyfishExpression.class, GreyfishExpressionConverter.class);
        Serializer serializer = new Persister(new RegistryStrategy(registry));
        StringWriter stringWriter = new StringWriter();
        // when
        serializer.write(GREYFISH_EXPRESSION, stringWriter);

        // then
        assertThat(stringWriter.toString()).isEqualTo(SERIALIZED_XML_STRING);
    }
}
