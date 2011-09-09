package org.asoem.greyfish.core.scenario;

import com.google.common.collect.Iterables;
import jdave.Specification;
import jdave.junit4.JDaveRunner;
import org.asoem.greyfish.core.individual.AbstractAgent;
import org.asoem.greyfish.core.individual.Population;
import org.asoem.greyfish.core.individual.Prototype;
import org.junit.runner.RunWith;

import java.awt.*;

import static org.asoem.greyfish.core.space.MutableObject2D.at;

@RunWith(JDaveRunner.class)
public class ScenarioSpec extends Specification<Scenario> {

    public class ScenarioBuildWith1Prototypes {
        final Prototype prototype = Prototype.newInstance(AbstractAgent.with().population(Population.newPopulation("TestPop", Color.black)).build());
        final Scenario scenario = Scenario.with().space(1, 1).add(prototype, at()).build();

        public void shouldReturn1Prototypes() {
            specify(Iterables.size(scenario.getPrototypes()), should.equal(1));
        }
    }

    public class ScenarioBuildWith2IdenticalPrototypes {
        final Prototype prototype = Prototype.newInstance(AbstractAgent.with().population(Population.newPopulation("TestPop", Color.black)).build());
        final Scenario scenario = Scenario.with().space(1, 1).add(prototype, at()).add(prototype, at()).build();

        public void shouldReturn1Prototypes() {
            specify(Iterables.size(scenario.getPrototypes()), should.equal(1));
        }
    }
}
