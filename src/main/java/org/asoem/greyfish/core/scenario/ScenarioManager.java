package org.asoem.greyfish.core.scenario;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import org.asoem.greyfish.core.individual.*;
import org.asoem.greyfish.core.io.GreyfishLogger;
import org.asoem.greyfish.lang.Functor;
import org.asoem.greyfish.utils.ListenerSupport;

import java.util.*;


public class ScenarioManager extends AbstractCollection<Scenario> {

	private final List<Scenario> scenarios = Collections.synchronizedList(Lists.<Scenario>newArrayList());
	private Scenario activeScenario = null;
	private final ListenerSupport<ScenarioChoiceListener> listenerSupport = ListenerSupport.newInstance();
	private final PrototypeManager prototypeManager;

	public ScenarioManager(final PrototypeManager prototypeManager) {
		Preconditions.checkNotNull(prototypeManager);
		this.prototypeManager = prototypeManager;
	}

	@Override
	public boolean add(final Scenario scenario) {
		if ( scenarios.add(scenario) ) {
			for (IndividualInterface individual : scenario.getPrototypes()) {
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

	public void setActiveScenario(Scenario scenario) {
		if (scenario != null && ! scenarios.contains(scenario)) {
			GreyfishLogger.debug("Scenario not jet managed by this Manager.");
			return;
		}
		if (activeScenario != scenario) {
			GreyfishLogger.debug("Active scenario changed.");
			activeScenario = scenario;
			notifyScenarioChanged(scenario, activeScenario);
		}
	}

	private void notifyScenarioChanged(final Scenario scenario, final Scenario newScenario) {
		listenerSupport.notifyListeners(new Functor<ScenarioChoiceListener>() {

			@Override
			public void update(ScenarioChoiceListener listener) {
				listener.scenarioChosen(ScenarioManager.this, scenario, newScenario);
			}
		});
	}

	public Scenario getActiveScenario() {
		return activeScenario;
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
}
