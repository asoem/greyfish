package org.asoem.greyfish.core.utils;

import com.google.common.base.Preconditions;
import org.asoem.greyfish.utils.math.RandomUtils;

import java.util.List;

/**
 * User: christoph
 * Date: 29.04.11
 * Time: 15:20
 */
public class EvaluatedCandidates {

    /**
     *
     * @param candidates the list of candidates
     * @param <T> the type of the candidates objects
     * @return the selected candidate, {@code null} if list is empty
     */
    public static <T> EvaluatedCandidate<T> selectRouletteWheel(final List<? extends EvaluatedCandidate<T>> candidates) {
        Preconditions.checkNotNull(candidates);
        if (candidates.isEmpty())
            return null;

        double sum = sumFitness(candidates);
        if (sum == 0)
            return selectRandom(candidates);

        double rand = RandomUtils.nextDouble(sum);
        double current = 0;
        for (EvaluatedCandidate<T> candidate : candidates) {
            current += candidate.getFitness();
            if (rand < current)
                return candidate;
        }

        return null;
    }

    public static <T> EvaluatedCandidate<T> selectRandom(List<? extends EvaluatedCandidate<T>> candidates) {
        Preconditions.checkNotNull(candidates);
        if (candidates.isEmpty())
            return null;
        return candidates.get(RandomUtils.nextInt(candidates.size()));
    }

    private static double sumFitness(List<? extends EvaluatedCandidate> candidates) {
        assert(candidates != null);
        double sum = 0;
        for (EvaluatedCandidate candidate : candidates) {
            sum += candidate.getFitness();
        }
        return sum;
    }
}
