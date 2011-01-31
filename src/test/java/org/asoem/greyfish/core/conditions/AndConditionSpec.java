package org.asoem.greyfish.core.conditions;

import jdave.Specification;
import jdave.junit4.JDaveRunner;
import org.junit.runner.RunWith;

@RunWith(JDaveRunner.class)
public class AndConditionSpec extends Specification<LogicalOperatorCondition> {
    AndCondition condition = AndCondition.trueIf().and(
                        AlwaysTrueCondition.trueIf().build()
                        , AlwaysTrueCondition.trueIf().build()
                ).build();

    public class BuildWith2Cildren {
          public void mustHave2Children() {
            specify(condition.getChildConditions().size(), must.equal(2));
        }
    }

    public class ClonedCondition {
        AndCondition condition = AndCondition.trueIf()
                .addConditions(
                        AlwaysTrueCondition.trueIf().build()
                        , AlwaysTrueCondition.trueIf().build()
                ).build();
        AndCondition clone = condition.deepClone(AndCondition.class);

        public void shouldHaveTheSameNumberOfChildren() {
            specify(clone.getChildConditions().size(), must.equal(condition.getChildConditions().size()));
        }
    }
}
