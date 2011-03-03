package org.asoem.greyfish.core.individual;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import javolution.lang.MathLib;
import org.asoem.greyfish.core.properties.FiniteSetProperty;
import org.asoem.greyfish.core.space.DefaultMovingObject2D;
import org.asoem.greyfish.core.space.Location2D;
import org.asoem.greyfish.core.space.MovingObject2D;
import org.asoem.greyfish.lang.FiniteSetSupplier;
import org.asoem.greyfish.lang.FiniteSetSuppliers;
import org.asoem.greyfish.utils.*;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementMap;
import org.simpleframework.xml.core.Commit;

import java.awt.*;
import java.util.Map;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Iterables.*;

public class Body extends AbstractGFComponent implements MovingObject2D, ConfigurableValueProvider {

    private final DefaultMovingObject2D movingObject2D = new DefaultMovingObject2D();

    @Attribute(name="radius", required = false)
    private float radius = 0.1f;

    private static final FiniteSetSupplier DEFAULT_SUPPLIER = FiniteSetSuppliers.of("Default");

    @Element(name="colorStateProperty", required = false)
    private FiniteSetProperty property;

    @ElementMap(name = "stateColorMap", entry = "entry", key = "state", value = "color",required = false)
    private final Map<Object, Color> stateColorMap;

    private FiniteSetSupplier<?> states = DEFAULT_SUPPLIER;

    private Body(IndividualInterface owner) {
        setComponentRoot(owner);
        setOrientation(RandomUtils.nextFloat(0f, (float) MathLib.TWO_PI));
        stateColorMap = Maps.newHashMap();
        generateColors();
    }

    private Body(Body body, CloneMap map) {
        super(body, map);
        this.property = map.clone(body.property, FiniteSetProperty.class);
        if (property != null)
            states = property;
        stateColorMap = body.stateColorMap;
    }

    @SuppressWarnings("unused") // used by the deserialization process
    private Body(@ElementMap(name = "stateColorMap", entry = "entry", key = "state", value = "color",required = false) Map<Object, Color> stateColorMap) {
        this.stateColorMap = stateColorMap;
    }

    @SuppressWarnings("unused") // used by the deserialization process
    @Commit
    private void commit() {
        if (property != null)
            states = property;
    }

    public float getRadius() {
        return radius; //(float) (radius + 0.01 * getComponentOwner().getAge());
    }

    public Color getColor() {
        return stateColorMap.get(states.get());
    }

    public void setColor(Color color) {
//        this.color = color;
        generateColors();
    }

    public static Body newInstance(IndividualInterface owner) {
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
    public DeepCloneable deepCloneHelper(CloneMap map) {
        return new Body(this, map);
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
    public Location2D getAnchorPoint() {
        return movingObject2D.getAnchorPoint();
    }

    @Override
    public void setAnchorPoint(Location2D location2d) {
        movingObject2D.setAnchorPoint(location2d);
    }

    @Override
    public double getX() {
        return movingObject2D.getX();
    }

    @Override
    public double getY() {
        return movingObject2D.getY();
    }

    @Override
    public void export(Exporter e) {
        e.add(ValueAdaptor.forField("Radius of the Circle", Float.class, this, "radius"));
        FiniteSetValueAdaptor<FiniteSetSupplier> b = new FiniteSetValueAdaptor<FiniteSetSupplier>("StateProperty", FiniteSetSupplier.class) {
            @Override protected void set(FiniteSetSupplier arg0) { states = checkNotNull(arg0);
                if (!states.equals(DEFAULT_SUPPLIER)) property = FiniteSetProperty.class.cast(states);
                generateColors(); }
            @Override public FiniteSetSupplier get() { return states; }
            @Override public Iterable<FiniteSetSupplier> values() {
                return concat(ImmutableList.of(DEFAULT_SUPPLIER), filter(getComponentOwner().getProperties(), FiniteSetProperty.class));
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
//        e.add(ValueAdaptor.forField("The color of the Body", Color.class, this, "color"));
    }

    private Color[] generateColors(int n) {
        Color[] cols = new Color[n];
        for(int i = 0; i < n; i++)
        {
            cols[i] = Color.getHSBColor((float) i / (float) n, 0.85f, 1.0f);
        }
        return cols;
    }
}
