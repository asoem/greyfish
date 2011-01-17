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

    public ChangeBodyAction() {
    }

    public ChangeBodyAction(Builder builder) {
        super(builder);
        parameterColor = builder.color;
    }

    @Override
    protected void performAction(Simulation simulation) {
        getComponentOwner().getBody().setColor(parameterColor);
    }

    @Override
    protected AbstractDeepCloneable deepCloneHelper(
            Map<AbstractDeepCloneable, AbstractDeepCloneable> mapDict) {
        return new Builder().deepClone(this, mapDict).build();
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

    public static class Builder extends AbstractGFAction.Builder {

        private Color color;

        protected Builder deepClone(ChangeBodyAction clone, Map<AbstractDeepCloneable, AbstractDeepCloneable> mapDict) {
            super.deepClone(clone, mapDict);
            color = clone.parameterColor;
            return this;
        }

        public Builder color(Color color) {
            this.color = color;
            return this;
        }

        public ChangeBodyAction build() {
            return new ChangeBodyAction(this);
        }
    }
}
