package org.asoem.sico.core.conditions;

import java.util.Map;

import org.asoem.sico.core.sensors.CounterSensor;
import org.asoem.sico.core.simulation.Simulation;
import org.asoem.sico.utils.AbstractDeepCloneable;
import org.asoem.sico.utils.Exporter;
import org.asoem.sico.utils.ValueAdaptor;
import org.simpleframework.xml.Element;

public class CounterSensorCondition extends IntCompareCondition {

	private static final long serialVersionUID = 1860984848904087585L;

	private CounterSensor counterSensor;

	@Element
	private String parameterSensorName = CounterSensor.DEFAULT_SENSORNAME;

	public CounterSensorCondition(
			CounterSensorCondition condition,
			Map<AbstractDeepCloneable, AbstractDeepCloneable> mapDict) {
		super(condition, mapDict);
		this.parameterSensorName = condition.parameterSensorName;
	}

	public CounterSensorCondition() {
	}

	@Override
	public void initialize(Simulation simulation) {
		super.initialize(simulation);
		counterSensor = componentOwner.getSensorInstance(CounterSensor.class, parameterSensorName);
	}

	@Override
	protected Integer getCompareValue(Simulation simulation) {
		return counterSensor.getTime();
	}

	@Override
	protected AbstractDeepCloneable deepCloneHelper(
			Map<AbstractDeepCloneable, AbstractDeepCloneable> mapDict) {
		return new CounterSensorCondition(this, mapDict);
	}
	
	@Override
	public void export(Exporter e) {
		super.export(e);
		e.addField( new ValueAdaptor<String>("Sensor Name", String.class, parameterSensorName) {
			@Override
			protected void writeThrough(String arg0) {
				CounterSensorCondition.this.parameterSensorName = arg0;
			}
		});
	}
}
