package org.asoem.greyfish.utils;

import akka.actor.ActorRef;
import akka.actor.Channel;


/**
 * User: christoph
 * Date: 19.10.11
 * Time: 11:37
 */
public class AkkaUtils {

    /**
     *  This is a workaround for IntelliJ complaining about {@link ActorRef#tell(Object) wants some Object of type T not Object}
     * @param actor the {@code ActorRef}
     * @return the {@code actor} as a {@code Channel<Object>}
     */
    public static Channel<Object> channel(ActorRef actor) {
        return actor;
    }
}
