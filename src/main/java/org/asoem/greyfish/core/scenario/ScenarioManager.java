package org.asoem.greyfish.core.scenario;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import org.asoem.greyfish.core.individual.Agent;
import org.asoem.greyfish.core.individual.Prototype;
import org.asoem.greyfish.core.individual.PrototypeManager;
import org.asoem.greyfish.lang.Functor;
import org.asoem.greyfish.utils.ListenerSupport;

import java.util.AbstractCollection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;


public class ScenarioManager extends AbstractCollection<Scenario> {

	private final List<Scenario> scenarios = Collections.synchronizedList(Lists.<Scenario>newArrayList());
	private final Scenario activeScenario = null;
	private final ListenerSupport<ScenarioChoiceListener> listenerSupport = ListenerSupport.newInstance();
	private final PrototypeManager prototypeManager;

	public ScenarioManager(final PrototypeManager prototypeManager) {
		Preconditions.checkNotNull(prototypeManager);
		this.prototypeManager = prototypeManager;
	}

	@Override
	public boolean add(final Scenario scenario) {
        Preconditions.checkNotNull(scenario);
		if ( scenarios.add(scenario) ) {
			for (Agent individual : scenario.getPrototypes()) {
				if (individual instanceof Prototype)
					prototypeManager.add(Prototype.class.cast(individual));
			}
			
			listenerSupport.notifyListeners( new Functor<ScenarioChoiceListener>() {

				@Override
				public void update(ScenarioChoiceListener listener) {
					listener.scenarioAdded(ScenarioManager.this, scenario);
				}
			});
			return true;
		}
		return false;
	}

	@Override
	public boolean remove(Object o) {
		if ( scenarios.remove(o) ) {
			final Scenario scenario = (Scenario)o;
			listenerSupport.notifyListeners(new Functor<ScenarioChoiceListener>() {

				@Override
				public void update(ScenarioChoiceListener listener) {
					listener.scenarioRemoved(ScenarioManager.this, scenario);
				}
			});
			return true;
		}
		return false;
	}

	public void addScenarioChangedListener(ScenarioChoiceListener listener) {
		listenerSupport.addListener(listener);
	}

	public void removeScenarioChangedListener(ScenarioChoiceListener listener) {
		listenerSupport.removeListener(listener);
	}

	@Override
	public Iterator<Scenario> iterator() {
		return scenarios.iterator();
	}

	@Override
	public int size() {
		return scenarios.size();
	}

    public PrototypeManager getPrototypeManager() {
        return prototypeManager;
    }
}
