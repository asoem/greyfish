package org.asoem.greyfish.core.conditions;

import org.asoem.greyfish.utils.base.DeepCloner;
import org.asoem.greyfish.utils.base.Tagged;

import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.ObjectStreamException;
import java.io.Serializable;

@Tagged("conditions")
public class NoneCondition extends BranchCondition {

    private NoneCondition(NoneCondition condition, DeepCloner map) {
        super(condition, map);
    }

    private NoneCondition(Builder builder) {
        super(builder);
    }

    @Override
    public boolean evaluate() {
        for (ActionCondition condition : getChildConditions())
            if (condition.evaluate())
                return false;
        return true;
    }

    @Override
    public NoneCondition deepClone(DeepCloner cloner) {
        return new NoneCondition(this, cloner);
    }

    private Object writeReplace() {
        return new Builder(this);
    }

    private void readObject(ObjectInputStream stream)
            throws InvalidObjectException {
        throw new InvalidObjectException("Builder required");
    }

    public static NoneCondition evaluates(ActionCondition... conditions) {
        return new Builder().add(conditions).build();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder extends BranchCondition.AbstractBuilder<NoneCondition, Builder> implements Serializable {
        private Builder() {
        }

        private Builder(NoneCondition noneCondition) {
            super(noneCondition);
        }

        @Override protected Builder self() { return this; }
        @Override public NoneCondition checkedBuild() { return new NoneCondition(this); }

        private Object readResolve() throws ObjectStreamException {
            try {
                return build();
            } catch (IllegalStateException e) {
                throw new InvalidObjectException("Build failed with: " + e.getMessage());
            }
        }

        private static final long serialVersionUID = 0;
    }
}
