package org.asoem.greyfish.core.individual;

import com.google.common.collect.AbstractIterator;
import org.asoem.greyfish.core.actions.GFAction;
import org.asoem.greyfish.core.conditions.GFCondition;
import org.asoem.greyfish.core.genes.Gene;
import org.asoem.greyfish.core.properties.GFProperty;

/**
 * User: christoph
 * Date: 04.10.11
 * Time: 21:02
 */
public class AgentComponentsIterator extends AbstractIterator<AgentComponent> implements ComponentVisitor {

    public AgentComponentsIterator(Agent agent) {
    }

    @Override
    protected AgentComponent computeNext() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void visit(GFProperty property) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void visit(GFAction action) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void visit(GFCondition condition) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void visit(Body body) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void visit(Gene<?> gene) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void visit(RootComponent rootComponent) {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
