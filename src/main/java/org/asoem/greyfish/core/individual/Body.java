package org.asoem.greyfish.core.individual;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import javolution.lang.MathLib;
import org.asoem.greyfish.core.properties.FiniteSetProperty;
import org.asoem.greyfish.core.properties.GFProperty;
import org.asoem.greyfish.core.simulation.Initializeable;
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.core.space.*;
import org.asoem.greyfish.utils.*;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Version;

import java.awt.*;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

public class Body extends AbstractGFComponent implements Object2DInterface, ConfigurableValueProvider {

    private final static float DEFAULT_RADIUS = 0.1f;
    private final Object2D movingObject2D = new Object2D();
    private final static Color DEFAULT_COLOR = Color.BLACK;

    @Element(name="color", required = false)
    private Color color = DEFAULT_COLOR;
    @Element(name="colorStateProperty", required = false)
    private FiniteSetProperty<?> states = null;
    @Element(name="colorStateMap", required = false)
    private Map<Object, Color> stateColorMap = null;

    private Supplier<Color> colorSupplier = Suppliers.ofInstance(DEFAULT_COLOR);

    private Body(IndividualInterface owner) {
        setComponentRoot(owner);
        setOrientation(RandomUtils.nextFloat(0f, (float) MathLib.TWO_PI));
    }

    private Body(Body body, CloneMap map) {
        super(body, map);
        this.states = map.clone(body.states, FiniteSetProperty.class);
        stateColorMap = body.stateColorMap;
        update();
    }

    public float getRadius() {
        return DEFAULT_RADIUS;
    }

    public Color getColor() {
        return colorSupplier.get();
    }

    public void setColor(Color color) {
        this.color = color;
        update();
    }

    public static Body newInstance(IndividualInterface owner) {
        return new Body(owner);
    }

    public void update() {
        // color
        if (states != null) {
            stateColorMap = Maps.newHashMap();
            Color[] colors = generateColors(states.getSet().length);
            int i = 0;
            for (Object o : states.getSet()) {
                stateColorMap.put(o, colors[i++]);
            }

            colorSupplier = new Supplier<Color>() {
                @Override
                public Color get() {
                    return stateColorMap.get(states.get());
                }
            };
        }
        else {
            colorSupplier = Suppliers.ofInstance(color);
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
    public Location2DInterface getAnchorPoint() {
        return movingObject2D.getAnchorPoint();
    }

    @Override
    public void setAnchorPoint(Location2DInterface location2d) {
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
        e.addField( new ValueSelectionAdaptor<FiniteSetProperty>(
                "StateProperty",
                FiniteSetProperty.class,
                states,
                Iterables.filter(getComponentOwner().getProperties(), FiniteSetProperty.class)) {
            @Override
            protected void writeThrough(FiniteSetProperty arg0) {
                states = checkNotNull(arg0);
                update();
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
}
