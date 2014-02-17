package org.asoem.greyfish.utils.space;


public interface Motion {
    int getDimension();

    double getTranslation();

    double[] getRotationAngles();
}
