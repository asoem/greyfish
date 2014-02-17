package org.asoem.greyfish.core.actions;


public class ResourceRequestMessage {

    private final double requestAmount;
    private final Object requestClassifier;

    public ResourceRequestMessage(final double requestAmount, final Object requestClassifier) {
        this.requestAmount = requestAmount;
        this.requestClassifier = requestClassifier;
    }

    public double getRequestAmount() {
        return requestAmount;
    }

    public Object getRequestClassifier() {
        return requestClassifier;
    }
}
