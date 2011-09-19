package org.asoem.greyfish.core.conditions;

import jdave.Specification;
import jdave.junit4.JDaveRunner;
import org.asoem.greyfish.utils.CloneMap;
import org.junit.runner.RunWith;

@RunWith(JDaveRunner.class)
public class AndConditionSpec extends Specification<LogicalOperatorCondition> {
    final AllCondition condition = AllCondition.trueIf().and(
                        AlwaysTrueCondition.trueIf().build()
                        , AlwaysTrueCondition.trueIf().build()
                ).build();

    public class BuildWith2Cildren {
          public void mustHave2Children() {
            specify(condition.getChildConditions().size(), must.equal(2));
        }
    }

    public class ClonedCondition {
        final AllCondition condition = AllCondition.trueIf()
                .addConditions(
                        AlwaysTrueCondition.trueIf().build()
                        , AlwaysTrueCondition.trueIf().build()
                ).build();
        final AllCondition clone = CloneMap.deepClone(condition, AllCondition.class);

        public void shouldHaveTheSameNumberOfChildren() {
            specify(clone.getChildConditions().size(), must.equal(condition.getChildConditions().size()));
        }
    }
}
