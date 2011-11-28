package org.asoem.greyfish.core.actions;

import org.asoem.greyfish.core.properties.GFProperty;

/**
 * User: christoph
 * Date: 05.04.11
 * Time: 10:17
 */
public class CompatibilityAwareResourceConversation {

    public static class CFPContent {
        private final double amount;
        private final GFProperty similatityTrait;

        public CFPContent(double amount, GFProperty similatityTrait) {
            this.amount = amount;
            this.similatityTrait = similatityTrait;
        }

        public double getAmount() {
            return amount;
        }

        public GFProperty getSimilatityTrait() {
            return similatityTrait;
        }
    }

    public static class ProposeContent {
        private final double amount;

        public ProposeContent(double amount) {
            this.amount = amount;
        }

        public double getAmount() {
            return amount;
        }
    }
}
