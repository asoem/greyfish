package org.asoem.greyfish.core.io.persistence;

import com.google.common.reflect.TypeToken;
import org.simpleframework.xml.convert.Converter;
import org.simpleframework.xml.stream.InputNode;
import org.simpleframework.xml.stream.OutputNode;

/**
 * User: christoph
 * Date: 04.07.12
 * Time: 15:12
 */
public class TypeTokenConverter implements Converter<TypeToken> {
    @Override
    public TypeToken read(InputNode inputNode) throws Exception {
        return TypeToken.of(Class.forName(inputNode.getValue()));
    }

    @Override
    public void write(OutputNode outputNode, TypeToken typeToken) throws Exception {
        outputNode.setValue(typeToken.toString());
    }
}
