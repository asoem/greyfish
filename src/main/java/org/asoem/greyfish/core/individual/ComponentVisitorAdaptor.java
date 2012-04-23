package org.asoem.greyfish.core.individual;

import org.asoem.greyfish.core.actions.GFAction;
import org.asoem.greyfish.core.conditions.GFCondition;
import org.asoem.greyfish.core.genes.Gene;
import org.asoem.greyfish.core.properties.GFProperty;

/**
 * User: christoph
 * Date: 23.04.12
 * Time: 10:38
 */
public class ComponentVisitorAdaptor implements ComponentVisitor {
    @Override
    public void visit(GFProperty property) {
    }

    @Override
    public void visit(GFAction action) {
    }

    @Override
    public void visit(GFCondition condition) {
    }

    @Override
    public void visit(Body body) {
    }

    @Override
    public void visit(Gene<?> gene) {
    }

    @Override
    public void visit(AgentComponent abstractAgentComponent) {
    }
}
