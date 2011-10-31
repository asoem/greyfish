package org.asoem.greyfish.core.individual;

import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import javolution.lang.MathLib;
import org.asoem.greyfish.core.properties.FiniteStateProperty;
import org.asoem.greyfish.core.properties.RangeElementProperty;
import org.asoem.greyfish.utils.base.DeepCloneable;
import org.asoem.greyfish.utils.base.DeepCloner;
import org.asoem.greyfish.utils.collect.ImmutableMapBuilder;
import org.asoem.greyfish.utils.collect.MutableRangeElement;
import org.asoem.greyfish.utils.collect.RangeElement;
import org.asoem.greyfish.utils.gui.ConfigurationHandler;
import org.asoem.greyfish.utils.gui.MapValuesAdaptor;
import org.asoem.greyfish.utils.gui.SetAdaptor;
import org.asoem.greyfish.utils.gui.ValueAdaptor;
import org.asoem.greyfish.utils.math.RandomUtils;
import org.asoem.greyfish.utils.space.MotionVector2D;
import org.asoem.greyfish.utils.space.Movable;
import org.asoem.greyfish.utils.space.MutableMovable;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementMap;
import org.simpleframework.xml.core.Commit;

import javax.annotation.Nullable;
import java.awt.*;
import java.util.Collections;
import java.util.Map;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Iterables.*;

public class Body extends AbstractAgentComponent implements Movable {

    private final MutableMovable movingObject2D = new MutableMovable();

    @Attribute(name="radius", required = false)
    private double radius = 0.1f;

    @Element(name="colorStateProperty", required = false)
    private FiniteStateProperty<?> property;

    @ElementMap(name = "stateColorMap", entry = "entry", key = "state", value = "color",required = false)
    private Map<Object, Color> stateColorMap;

    private Object state;

    private RangeElement<?> outlineValueSupplier = new MutableRangeElement<Double>(0.0, 1.0, 0.0);

    /**
     *
     * @param owner the agent which this body is part of
     */
    private Body(Agent owner) {
        this();
        setAgent(checkNotNull(owner));
    }

    /**
     *
     * @param body The original
     * @param cloner The Cloner
     */
    private Body(Body body, DeepCloner cloner) {
        super(body, cloner);
        this.property = cloner.cloneField(body.property, FiniteStateProperty.class);
        if (property != null)
            state = property;
        stateColorMap = body.stateColorMap;
        if (body.outlineValueSupplier instanceof RangeElementProperty)
            outlineValueSupplier = cloner.cloneField((RangeElementProperty) body.outlineValueSupplier, RangeElementProperty.class);
    }

    /**
     * Default Constructor.
     */
    public Body() {
        setOrientation(RandomUtils.nextFloat(0f, (float) MathLib.TWO_PI));
        stateColorMap = ImmutableMap.of((Object)"Default", Color.BLACK);
    }

    @Commit
    private void commit() {
        if (property != null)
            state = property;
    }

    public double getRadius() {
        return radius; //(float) (radius + 0.01 * getAgent().getAge());
    }

    public Color getColor() {
        return stateColorMap.get(state);
    }

    public void setColor(Color color) {
    }

    public static Body newInstance(Agent owner) {
        return new Body(owner);
    }

    @Override
    public DeepCloneable deepClone(DeepCloner cloner) {
        return new Body(this, cloner);
    }

    @Override
    public void setOrientation(double alpha) {
        movingObject2D.setOrientation(alpha);
    }

    @Override
    public MotionVector2D getMotionVector() {
        return movingObject2D.getMotionVector();
    }

    @Override
    public void changeMotion(double angle, double velocity) {
        movingObject2D.changeMotion(angle, velocity);
    }

    @Override
    public void setMotion(double angle, double velocity) {
        movingObject2D.setMotion(angle, velocity);
    }

    @Override
    public void configure(ConfigurationHandler e) {
        super.configure(e);
        e.add(ValueAdaptor.forField("Radius of the Circle", Double.class, this, "radius"));
        SetAdaptor<Object> b = new SetAdaptor<Object>("StateProperty", Object.class) {
            @Override protected void set(Object arg0) {
                state = checkNotNull(arg0);
                if (!state.equals("Default")) {
                    property = FiniteStateProperty.class.cast(state);
                    stateColorMap = ImmutableMapBuilder.<Object, Color>newInstance()
                            .putAll(property.getStates(),
                                    Functions.identity(),
                                    new Function<Object, Color>() {

                                        @Override
                                        public Color apply(@Nullable Object o) {
                                            return Color.BLACK;
                                        }
                                    }).build();
                }
            }
            @Override public Object get() { return state; }
            @Override public Iterable<Object> values() {
                return concat(ImmutableList.of("Default"), filter(agent().getProperties(), FiniteStateProperty.class));
            }
        };
        e.add(b);

        MapValuesAdaptor<Color> colorMultiValueAdaptor = new MapValuesAdaptor<Color>("State Colors", Color.class) {
            @Override public Object[] keys() { return toArray(stateColorMap.keySet(), Object.class); }
            @Override public Color[] get() { return toArray(stateColorMap.values(), Color.class); }
            @Override public void set(Color[] list) {
                checkArgument(list.length == stateColorMap.size());
                int i = 0;
                for (Object key : stateColorMap.keySet()) stateColorMap.put(key, list[i++]);
            }
        };
        e.add(colorMultiValueAdaptor);
        b.addValueChangeListener(colorMultiValueAdaptor);
//        e.sum(ValueAdaptor.forField("The color of the Body", Color.class, this, "color"));

        e.add(new SetAdaptor<RangeElement>("Outline", RangeElement.class) {
            @Override
            public Iterable<RangeElement> values() {
                return Iterables.filter(agent().getProperties(), RangeElement.class);
            }

            @Override
            protected void set(RangeElement arg0) {
                outlineValueSupplier = arg0;
            }

            @Override
            public RangeElement get() {
                return outlineValueSupplier;
            }
        });
    }

    /*
    private Color[] generateColors(int n) {
        Color[] cols = new Color[n];
        for(int i = 0; i < n; i++)
        {
            cols[i] = Color.getHSBColor((float) i / (float) n, 0.85f, 1.0f);
        }
        return cols;
    }
    */

    @Override
    public void accept(ComponentVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public Iterable<AgentComponent> children() {
        return Collections.emptyList();
    }
}
