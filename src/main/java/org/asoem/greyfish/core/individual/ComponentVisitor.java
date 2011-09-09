package org.asoem.greyfish.core.individual;

import org.asoem.greyfish.core.actions.GFAction;
import org.asoem.greyfish.core.conditions.ConditionTree;
import org.asoem.greyfish.core.conditions.GFCondition;
import org.asoem.greyfish.core.properties.GFProperty;

/**
 * User: christoph
 * Date: 09.09.11
 * Time: 16:46
 */
public interface ComponentVisitor {
    void visit(GFProperty property);
    void visit(GFAction action);
    void visit(GFCondition condition);
    void visit(Body body);
    void visit(ConditionTree conditionTree);
}
