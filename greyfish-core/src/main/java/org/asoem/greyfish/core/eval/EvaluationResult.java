package org.asoem.greyfish.core.eval;

/**
 * The result object of an evaluated {@link Evaluator}.
 */
public interface EvaluationResult {
    /**
     *
     * @return the raw result of the evaluated expression
     * @throws EvaluationException
     */
    Object get() throws EvaluationException;

    /**
     *
     * @return this result as a {@link Number}, if possible.
     * @throws EvaluationException if this result cannot be interpreted as a {@link Number}
     */
    Number asNumber() throws EvaluationException;

    /**
     *
     * @return the evaluation result as a {@code double}, if possible.
     * @throws EvaluationException if this result cannot be interpreted as a {@code double}
     */
    double asDouble() throws EvaluationException;

    /**
     *
     * @return the evaluation result as an {@code int}, if possible.
     * @throws EvaluationException if this result cannot be interpreted as a {@code int}
     */
    int asInt() throws EvaluationException;

    /**
     *
     * @return the evaluation result as a {@code boolean}, if possible.
     * @throws EvaluationException if this result cannot be interpreted as a {@code boolean}
     */
    boolean asBoolean() throws EvaluationException;

    /**
     *
     * @return the evaluation result as a {@code String}, if possible.
     * @throws EvaluationException if this result cannot be interpreted as a {@link String}
     */
    String asString() throws EvaluationException;
}
