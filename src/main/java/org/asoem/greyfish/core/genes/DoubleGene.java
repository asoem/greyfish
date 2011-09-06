package org.asoem.greyfish.core.genes;

/**
 * Created by IntelliJ IDEA.
 * User: christoph
 * Date: 05.09.11
 * Time: 18:23
 * To change this template use File | Settings | File Templates.
 */
public class DoubleGene extends DefaultGene<Double> {
    public DoubleGene(Double element, Class<Double> clazz, GeneController<Double> mutationFunction) {
        super(element, clazz, mutationFunction);
    }

    public DoubleGene(Gene<Double> doubleGene) {
        super(doubleGene);
    }
}
