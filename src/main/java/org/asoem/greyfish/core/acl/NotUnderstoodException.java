package org.asoem.greyfish.core.acl;

/**
 * Created by IntelliJ IDEA.
 * User: christoph
 * Date: 14.01.11
 * Time: 14:19
 * To change this template use File | Settings | File Templates.
 */
public class NotUnderstoodException extends Exception {
    public NotUnderstoodException() {
    }

    public NotUnderstoodException(String message) {
        super(message);
    }

    public ACLMessage createReply(ACLMessage message) {
        ACLMessage ret = message.createReply();
        ret.setPerformative(ACLPerformative.NOT_UNDERSTOOD);
        ret.setStringContent(getMessage());
        return ret;
    }
}
