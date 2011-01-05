package org.asoem.sico.core.scenario;

import java.util.EventListener;


public interface ScenarioChoiceListener extends EventListener {

	public void scenarioAdded(ScenarioManager source, Scenario scenario);
	public void scenarioRemoved(ScenarioManager source, Scenario scenario);

	public void scenarioChosen(ScenarioManager manager, Scenario oldScenario, Scenario newScenario);
}
