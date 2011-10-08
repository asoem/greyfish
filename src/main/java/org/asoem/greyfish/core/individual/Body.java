package org.asoem.greyfish.core.individual;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import javolution.lang.MathLib;
import org.asoem.greyfish.core.properties.FiniteSetProperty;
import org.asoem.greyfish.core.properties.WellOrderedSetElementProperty;
import org.asoem.greyfish.core.space.Coordinates2D;
import org.asoem.greyfish.core.space.DefaultMovingObject2D;
import org.asoem.greyfish.core.space.MovingObject2D;
import org.asoem.greyfish.core.utils.SimpleXMLConstructor;
import org.asoem.greyfish.gui.utils.Circle;
import org.asoem.greyfish.lang.FiniteSetSupplier;
import org.asoem.greyfish.lang.FiniteSetSuppliers;
import org.asoem.greyfish.lang.MutableWellOrderedSetElement;
import org.asoem.greyfish.lang.WellOrderedSetElement;
import org.asoem.greyfish.utils.*;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementMap;
import org.simpleframework.xml.core.Commit;

import java.awt.*;
import java.awt.geom.Arc2D;
import java.util.Collections;
import java.util.Map;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Iterables.*;

public class Body extends AbstractAgentComponent implements MovingObject2D {

    private final DefaultMovingObject2D movingObject2D = new DefaultMovingObject2D();
    @Attribute(name="radius", required = false)
    private double radius = 0.1f;
    private static final FiniteSetSupplier DEFAULT_SUPPLIER = FiniteSetSuppliers.of("Default");
    @Element(name="colorStateProperty", required = false)
    private FiniteSetProperty property;
    @ElementMap(name = "stateColorMap", entry = "entry", key = "state", value = "color",required = false)
    private final Map<Object, Color> stateColorMap;
    private FiniteSetSupplier<?> states = DEFAULT_SUPPLIER;
    private WellOrderedSetElement<?> outlineValueSupplier = new MutableWellOrderedSetElement<Double>(0.0, 1.0, 0.0);

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
        this.property = cloner.cloneField(body.property, FiniteSetProperty.class);
        if (property != null)
            states = property;
        stateColorMap = body.stateColorMap;
        if (body.outlineValueSupplier instanceof WellOrderedSetElementProperty)
            outlineValueSupplier = cloner.cloneField((WellOrderedSetElementProperty) body.outlineValueSupplier, WellOrderedSetElementProperty.class);
    }

    @SimpleXMLConstructor
    private Body(@ElementMap(name = "stateColorMap", entry = "entry", key = "state", value = "color",required = false) Map<Object, Color> stateColorMap) {
        this.stateColorMap = stateColorMap;
    }

    /**
     * Default Constructor.
     */
    public Body() {
        setOrientation(RandomUtils.nextFloat(0f, (float) MathLib.TWO_PI));
        stateColorMap = Maps.newHashMap();
        generateColors();
    }

    @Commit
    private void commit() {
        if (property != null)
            states = property;
    }

    public double getRadius() {
        return radius; //(float) (radius + 0.01 * getAgent().getAge());
    }

    public Color getColor() {
        return stateColorMap.get(states.get());
    }

    public void setColor(Color color) {
//        this.color = color;
        generateColors();
    }

    public static Body newInstance(Agent owner) {
        return new Body(owner);
    }

    private void generateColors() {
        stateColorMap.clear();
        Color[] colors = generateColors(states.getSet().size());
        int i = 0;
        for (Object o : states.getSet()) {
            stateColorMap.put(o, colors[i++]);
        }
    }

    @Override
    public DeepCloneable deepClone(DeepCloner cloner) {
        return new Body(this, cloner);
    }

    @Override
    public double getOrientation() {
        return movingObject2D.getOrientation();
    }

    @Override
    public void setOrientation(double alpha) {
        movingObject2D.setOrientation(alpha);
    }

    @Override
    public PolarPoint getMotionVector() {
        return movingObject2D.getMotionVector();
    }

    @Override
    public void setMotionVector(PolarPoint polarPoint) {
        movingObject2D.setMotionVector(polarPoint);
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
    public Coordinates2D getCoordinates() {
        return movingObject2D.getCoordinates();
    }

    @Override
    public void setAnchorPoint(Coordinates2D coordinates2d) {
        movingObject2D.setAnchorPoint(coordinates2d);
    }

    @Override
    public void configure(ConfigurationHandler e) {
        super.configure(e);
        e.add(ValueAdaptor.forField("Radius of the Circle", Double.class, this, "radius"));
        FiniteSetValueAdaptor<FiniteSetSupplier> b = new FiniteSetValueAdaptor<FiniteSetSupplier>("StateProperty", FiniteSetSupplier.class) {
            @Override protected void set(FiniteSetSupplier arg0) { states = checkNotNull(arg0);
                if (!states.equals(DEFAULT_SUPPLIER)) property = FiniteSetProperty.class.cast(states);
                generateColors(); }
            @Override public FiniteSetSupplier get() { return states; }
            @Override public Iterable<FiniteSetSupplier> values() {
                return concat(ImmutableList.of(DEFAULT_SUPPLIER), filter(agent.get().getProperties(), FiniteSetProperty.class));
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

        e.add(new FiniteSetValueAdaptor<WellOrderedSetElement>("Outline", WellOrderedSetElement.class) {
            @Override
            public Iterable<WellOrderedSetElement> values() {
                return Iterables.filter(agent.get().getProperties(), WellOrderedSetElement.class);
            }

            @Override
            protected void set(WellOrderedSetElement arg0) {
                outlineValueSupplier = arg0;
            }

            @Override
            public WellOrderedSetElement get() {
                return outlineValueSupplier;
            }
        });
    }

    private Color[] generateColors(int n) {
        Color[] cols = new Color[n];
        for(int i = 0; i < n; i++)
        {
            cols[i] = Color.getHSBColor((float) i / (float) n, 0.85f, 1.0f);
        }
        return cols;
    }

    // TODO: Invert dependency
    public void draw(Graphics2D g2d) {
        Circle c = Circle.at(getCoordinates().getX(), getCoordinates().getY(), getRadius());

        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(0.06f));
        Arc2D.Double arc = new Arc2D.Double(c.getBounds2D(), 0, (int) (outlineValueSupplier.get().doubleValue() / outlineValueSupplier.getUpperBound().doubleValue() * 360), Arc2D.OPEN);
        g2d.draw(arc);

        g2d.setColor(getColor());
        g2d.fill(c);
    }

    @Override
    public void accept(ComponentVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public Iterable<AgentComponent> children() {
        return Collections.emptyList();
    }
}
