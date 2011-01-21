package org.asoem.greyfish.core.actions;

import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.lang.BuilderInterface;
import org.asoem.greyfish.lang.ClassGroup;
import org.asoem.greyfish.utils.AbstractDeepCloneable;
import org.asoem.greyfish.utils.Exporter;
import org.asoem.greyfish.utils.ValueAdaptor;
import org.simpleframework.xml.Element;

import java.awt.*;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

@ClassGroup(tags="actions")
public class ChangeBodyAction extends AbstractGFAction {

    @Element(name="color")
    private Color color;

    private ChangeBodyAction() {
        this(new Builder());
    }

    @Override
    protected void performAction(Simulation simulation) {
        getComponentOwner().getBody().setColor(color);
    }

    @Override
    protected AbstractDeepCloneable deepCloneHelper(
            Map<AbstractDeepCloneable, AbstractDeepCloneable> mapDict) {
        return new Builder().fromClone(this, mapDict).build();
    }

    @Override
    public void export(Exporter e) {
        super.export(e);
        e.addField( new ValueAdaptor<Color>("Color", Color.class, color) {
            @Override protected void writeThrough(Color arg0) { color = checkFrozen(checkNotNull(arg0)); }
        });
    }

    protected ChangeBodyAction(AbstractBuilder<?> builder) {
        super(builder);
        this.color = builder.color;
    }

    public static Builder with() { return new Builder(); }
    public static final class Builder extends AbstractBuilder<Builder> implements BuilderInterface<ChangeBodyAction> {
        private Builder() {}
        @Override protected Builder self() { return this; }
        @Override public ChangeBodyAction build() { return new ChangeBodyAction(this); }
    }

    protected static abstract class AbstractBuilder<T extends AbstractBuilder<T>> extends AbstractGFAction.AbstractBuilder<T> {
        private Color color;

        public T color(Color color) { this.color = checkNotNull(color); return self(); }

        protected T fromClone(ChangeBodyAction action, Map<AbstractDeepCloneable, AbstractDeepCloneable> mapDict) {
            super.fromClone(action, mapDict).color(action.color);
            return self();
        }
    }
}
