package org.asoem.greyfish.core.eval;

/**
 * User: christoph
 * Date: 17.02.12
 * Time: 16:09
 */
public interface EvaluationResult {
    <T> T as(Class<T> resultType);
    double asDouble() throws EvaluationException;
    boolean asBoolean() throws EvaluationException;
    String asString() throws EvaluationException;
    Object asObject();
}
