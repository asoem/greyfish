package org.asoem.greyfish.core.eval;

import java.text.ParseException;

/**
 * User: christoph
 * Date: 17.02.12
 * Time: 16:09
 */
public interface EvaluationResult {
    /**
     *
     * @return the raw result of the valuated expression
     * @throws EvaluationException
     */
    Object get() throws EvaluationException;

    Number asNumber() throws EvaluationException, ParseException;

    /**
     *
     * @return the evaluation result converted to a double value
     * @throws EvaluationException
     */
    double asDouble() throws EvaluationException, ParseException;
    int asInt() throws EvaluationException, ParseException;

    boolean asBoolean() throws EvaluationException;
    String asString() throws EvaluationException;
}
