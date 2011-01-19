package org.asoem.greyfish.core.actions;

import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.lang.ClassGroup;
import org.asoem.greyfish.utils.AbstractDeepCloneable;
import org.asoem.greyfish.utils.Exporter;
import org.asoem.greyfish.utils.ValueAdaptor;
import org.simpleframework.xml.Element;

import java.awt.*;
import java.util.Map;

@ClassGroup(tags="actions")
public class ChangeBodyAction extends AbstractGFAction {

    @Element(name="color")
    private Color parameterColor;

    private ChangeBodyAction() {
        this(new Builder());
    }

    @Override
    protected void performAction(Simulation simulation) {
        getComponentOwner().getBody().setColor(parameterColor);
    }

    @Override
    protected AbstractDeepCloneable deepCloneHelper(
            Map<AbstractDeepCloneable, AbstractDeepCloneable> mapDict) {
        return new Builder().fromClone(this, mapDict).build();
    }

    @Override
    public void export(Exporter e) {
        super.export(e);
        e.addField( new ValueAdaptor<Color>("Color", Color.class, parameterColor) {

            @Override
            protected void writeThrough(Color arg0) {
                ChangeBodyAction.this.parameterColor = arg0;
            }
        });
    }

    protected ChangeBodyAction(AbstractBuilder<?> builder) {
        super(builder);
        this.parameterColor = builder.parameterColor;
    }

    public static final class Builder extends AbstractBuilder<Builder> {
        @Override protected Builder self() {  return this; }
    }

    protected static abstract class AbstractBuilder<T extends AbstractBuilder<T>> extends AbstractGFAction.AbstractBuilder<T> {
        private Color parameterColor;

        public T parameterColor(Color parameterColor) { this.parameterColor = parameterColor; return self(); }

        protected T fromClone(ChangeBodyAction action, Map<AbstractDeepCloneable, AbstractDeepCloneable> mapDict) {
            super.fromClone(action, mapDict).parameterColor(action.parameterColor);
            return self();
        }

        public ChangeBodyAction build() { return new ChangeBodyAction(this); }
    }
}
