package org.asoem.greyfish.core.actions;

import org.asoem.greyfish.core.individual.AbstractAgentComponent;
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.lang.ClassGroup;
import org.asoem.greyfish.utils.ConfigurationHandler;
import org.asoem.greyfish.utils.DeepCloner;
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
    protected ActionState executeUnconditioned(Simulation simulation) {
        agent().setColor(color);
        return ActionState.END_SUCCESS;
    }

    @Override
    public AbstractAgentComponent deepClone(DeepCloner cloner) {
        return new ChangeBodyAction(this, cloner);
    }

    public ChangeBodyAction(ChangeBodyAction cloneable, DeepCloner map) {
        super(cloneable, map);
        this.color = cloneable.color;
    }

    @Override
    public void configure(ConfigurationHandler e) {
        super.configure(e);
        e.add(new ValueAdaptor<Color>("Color", Color.class) {
            @Override
            public Color get() {
                return color;
            }

            @Override
            protected void set(Color arg0) {
                color = checkNotNull(arg0);
            }
        });
    }

    protected ChangeBodyAction(AbstractBuilder<?,?> builder) {
        super(builder);
        this.color = builder.color;
    }

    public static Builder with() { return new Builder(); }
    public static final class Builder extends AbstractBuilder<ChangeBodyAction, Builder> {
        @Override protected Builder self() { return this; }
        @Override public ChangeBodyAction checkedBuild() { return new ChangeBodyAction(this); }
    }

    protected static abstract class AbstractBuilder<E extends ChangeBodyAction,T extends AbstractBuilder<E,T>> extends AbstractGFAction.AbstractBuilder<E,T> {
        private Color color;

        public T color(Color color) { this.color = checkNotNull(color); return self(); }
    }
}
