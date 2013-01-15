package org.asoem.greyfish.core.eval.impl;

import com.google.common.base.Objects;
import org.asoem.greyfish.core.eval.EvaluationException;
import org.asoem.greyfish.core.eval.EvaluationResult;

import javax.annotation.Nullable;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;

/**
 * User: christoph
 * Date: 17.02.12
 * Time: 16:10
 */
public class ConvertingEvaluationResult implements EvaluationResult {

    @Nullable
    private final Object result;
    private final RESULT_TYPE resultType;

    public ConvertingEvaluationResult(@Nullable Object result) {
        this.result = result;
        if (result == null)
            resultType = RESULT_TYPE.NULL;
        else if (result instanceof Double)
            resultType = RESULT_TYPE.DOUBLE;
        else if (result instanceof Float)
            resultType = RESULT_TYPE.FLOAT;
        else if (result instanceof Integer)
            resultType = RESULT_TYPE.INTEGER;
        else if (result instanceof Boolean)
            resultType = RESULT_TYPE.BOOLEAN;
        else if (result instanceof String)
            resultType = RESULT_TYPE.STRING;
        else
            resultType = RESULT_TYPE.OBJECT;
    }

    @SuppressWarnings("ConstantConditions") // result is only null if resultType is NULL
    @Override
    public Number asNumber() throws EvaluationException, ParseException {
        switch (resultType) {
            case STRING:
                return NumberFormat.getInstance(Locale.US).parse((String) result); // TODO: cache
            case DOUBLE:
            case INTEGER:
            case FLOAT:
                return (Number) result;
            case BOOLEAN:
                return ((Boolean) result) ? 1 : 0;
            default: throw new EvaluationException(result + " cannot be converted to a Number");
        }
    }

    @SuppressWarnings("ConstantConditions") // result is only null if resultType is NULL
    @Override
    public double asDouble() throws EvaluationException, ParseException {
        return asNumber().doubleValue();
    }

    @SuppressWarnings("ConstantConditions") // result is only null if resultType is NULL
    @Override
    public boolean asBoolean() throws EvaluationException {
        switch (resultType) {
            case BOOLEAN: return (Boolean)result;
            case STRING: return Boolean.parseBoolean((String)result); // TODO: include 1 as true
            default: throw new EvaluationException(result + " cannot be converted to Boolean");
        }
    }

    @Override
    public String asString() throws EvaluationException {
        return String.valueOf(result);
    }

    @Override
    public Object get() {
        return result;
    }

    @SuppressWarnings("ConstantConditions") // result is only null if resultType is NULL
    @Override
    public int asInt() throws EvaluationException, ParseException {
        return asNumber().intValue();
    }

    private enum RESULT_TYPE {
        DOUBLE,
        FLOAT,
        INTEGER,
        BOOLEAN,
        STRING,
        NULL,
        OBJECT
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ConvertingEvaluationResult that = (ConvertingEvaluationResult) o;

        return !(result != null ? !result.equals(that.result) : that.result != null) && resultType == that.resultType;

    }

    @Override
    public int hashCode() {
        int result1 = result != null ? result.hashCode() : 0;
        result1 = 31 * result1 + resultType.hashCode();
        return result1;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .addValue(result)
                .addValue(resultType)
                .toString();
    }
}
