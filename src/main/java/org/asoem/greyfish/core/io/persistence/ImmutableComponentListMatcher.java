package org.asoem.greyfish.core.io.persistence;

import org.asoem.greyfish.core.individual.ImmutableComponentList;
import org.simpleframework.xml.convert.Converter;
import org.simpleframework.xml.stream.InputNode;
import org.simpleframework.xml.stream.OutputNode;

import java.util.ArrayList;
import java.util.List;

/**
 * User: christoph
 * Date: 21.02.12
 * Time: 11:53
 */
class ImmutableComponentListMatcher implements Converter<ImmutableComponentList> {
    @Override
    public ImmutableComponentList read(InputNode inputNode) throws Exception {
        List<?> list = new ArrayList<Object>();

        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void write(OutputNode outputNode, ImmutableComponentList immutableComponentList) throws Exception {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
