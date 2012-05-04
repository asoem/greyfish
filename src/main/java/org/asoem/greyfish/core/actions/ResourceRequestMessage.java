package org.asoem.greyfish.core.actions;

/**
 * User: christoph
 * Date: 04.05.12
 * Time: 14:44
 */
public class ResourceRequestMessage {

    private final double requestAmount;
    private final String requestClassifier;

    public ResourceRequestMessage(double requestAmount, String requestClassifier) {
        this.requestAmount = requestAmount;
        this.requestClassifier = requestClassifier;
    }

    public double getRequestAmount() {
        return requestAmount;
    }

    public String getRequestClassifier() {
        return requestClassifier;
    }
}
