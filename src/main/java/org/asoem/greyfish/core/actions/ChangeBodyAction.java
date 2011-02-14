package org.asoem.greyfish.core.actions;

import org.asoem.greyfish.core.individual.AbstractGFComponent;
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.lang.BuilderInterface;
import org.asoem.greyfish.lang.ClassGroup;
import org.asoem.greyfish.utils.CloneMap;
import org.asoem.greyfish.utils.Exporter;
import org.asoem.greyfish.utils.ValueAdaptor;
import org.simpleframework.xml.Element;

import java.awt.*;

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
        getComponentOwner().setColor(color);
    }

    @Override
    public AbstractGFComponent deepCloneHelper(CloneMap map) {
        return new ChangeBodyAction(this, map);
    }

    public ChangeBodyAction(ChangeBodyAction cloneable, CloneMap map) {
        super(cloneable, map);
        this.color = cloneable.color;
    }

    @Override
    public void export(Exporter e) {
        super.export(e);
        e.add(new ValueAdaptor<Color>("Color", Color.class) {
            @Override
            public Color get() {
                return color;
            }

            @Override
            protected void set(Color arg0) {
                color = checkFrozen(checkNotNull(arg0));
            }
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
    }
}
