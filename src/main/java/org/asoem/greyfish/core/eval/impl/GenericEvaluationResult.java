package org.asoem.greyfish.core.eval.impl;

import org.asoem.greyfish.core.eval.EvaluationException;
import org.asoem.greyfish.core.eval.EvaluationResult;

import javax.annotation.Nullable;

/**
 * User: christoph
 * Date: 17.02.12
 * Time: 16:10
 */
public class GenericEvaluationResult implements EvaluationResult {

    @Nullable
    private final Object result;
    private final RESULT_TYPE resultType;

    public GenericEvaluationResult(@Nullable Object result) {
        this.result = result;
        if (result instanceof Double)
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
            resultType = RESULT_TYPE.OTHER;
    }

    @Override
    public <T> T as(Class<T> resultType) {
        return resultType.cast(result);
    }

    @Override
    public double asDouble() throws EvaluationException {
        switch (resultType) {
            case DOUBLE: return as(Double.class);
            case INTEGER: return as(Integer.class).doubleValue();
            case FLOAT: return as(Float.class).doubleValue();
            case STRING: return Double.parseDouble(as(String.class));
            default: throw new EvaluationException(result + " cannot be converted to Double");
        }
    }

    @Override
    public boolean asBoolean() throws EvaluationException {
        switch (resultType) {
            case BOOLEAN: return as(Boolean.class);
            case STRING: return Boolean.parseBoolean(as(String.class));
            default: throw new EvaluationException(result + " cannot be converted to Boolean");
        }
    }

    @Override
    public String asString() throws EvaluationException {
        return String.valueOf(result);
    }

    @Override
    public Object asObject() {
        return result;
    }

    @Override
    public int asInt() throws EvaluationException {
        switch (resultType) {
            case INTEGER: return as(Integer.class);
            case DOUBLE: return as(Double.class).intValue();
            case FLOAT: return as(Double.class).intValue();
            case STRING: return Integer.parseInt(as(String.class));
            default: throw new EvaluationException(result + " cannot be converted to Integer");
        }
    }

    private enum RESULT_TYPE {
        DOUBLE,
        FLOAT,
        INTEGER,
        BOOLEAN,
        STRING,
        OTHER
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GenericEvaluationResult that = (GenericEvaluationResult) o;

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
        return "GenericEvaluationResult{" +
                "result=" + result +
                ", resultType=" + resultType +
                '}';
    }
}
