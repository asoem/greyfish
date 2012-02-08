package org.asoem.greyfish.core.genes;

import com.google.common.collect.Table;
import org.asoem.greyfish.utils.math.RandomUtils;

import java.util.Map;

/**
 * User: christoph
 * Date: 07.02.12
 * Time: 11:30
 */
public class MarkovChain {
    private final Table<String, String, Double> markovMatrix;

    public MarkovChain(Table<String, String, Double> markovMatrix) {
        this.markovMatrix = markovMatrix;
    }

    /**
     * Make a transition to the next state as defined by the markovMatrix
     * @param currentState the current state
     * @return the next state
     */
    public String next(String currentState) {
        double sum = 0;
        double rand = RandomUtils.nextDouble();
        for (Map.Entry<String, Double> cell : markovMatrix.row(currentState).entrySet()) {
             if (sum + cell.getValue() > rand) {
                 return cell.getKey();
             }
        }
        throw new AssertionError();
    }
}
