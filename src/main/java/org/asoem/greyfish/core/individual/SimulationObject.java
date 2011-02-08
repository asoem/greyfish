package org.asoem.greyfish.core.individual;

import org.asoem.greyfish.core.genes.GenomeInterface;
import org.asoem.greyfish.core.properties.GFProperty;
import org.asoem.greyfish.core.simulation.Initializeable;
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.core.space.Object2DInterface;
import org.asoem.greyfish.utils.DeepCloneable;

import java.awt.*;

public interface SimulationObject extends DeepCloneable, Initializeable, Object2DInterface {
    Population getPopulation();
    void setTimeOfBirth(int timeOfBirth);
    void execute(Simulation simulation);
    GenomeInterface getGenome();
    <T extends GFProperty> Iterable<T> getProperties(Class<T> doublePropertyClass);
    double getRadius();
    Color getColor();
    int getId();

    double getOrientation();
    void rotate(double angle);
}
