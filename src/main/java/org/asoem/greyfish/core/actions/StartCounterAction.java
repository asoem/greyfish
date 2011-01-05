package org.asoem.sico.core.actions;

import java.util.Map;

import org.asoem.sico.core.sensors.CounterSensor;
import org.asoem.sico.core.simulation.Simulation;
import org.asoem.sico.lang.ClassGroup;
import org.asoem.sico.utils.AbstractDeepCloneable;
import org.asoem.sico.utils.Exporter;
import org.asoem.sico.utils.ValueAdaptor;
import org.simpleframework.xml.Element;

@ClassGroup(tags="action")
public class StartCounterAction extends AbstractGFAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2372698632427359065L;

	private CounterSensor counterSensor;

	@Element
	private String parameterSensorName = CounterSensor.DEFAULT_SENSORNAME;

	public StartCounterAction() {
		this("StartCounterAction");
	}
	
	public StartCounterAction(String name) {
		super(name);
	}
	
	protected StartCounterAction(StartCounterAction action,
			Map<AbstractDeepCloneable, AbstractDeepCloneable> mapDict) {
		super(action, mapDict);
		this.parameterSensorName = action.parameterSensorName;
	}
	
	@Override
	protected void performAction(Simulation simulation) {
		counterSensor.start();
	}

	@Override
	public void initialize(Simulation simulation) {
		super.initialize(simulation);
		counterSensor = componentOwner.getSensorInstance(CounterSensor.class, parameterSensorName);
	}

//	public void setParameterSensorName(String parameterSensorName) {
//		this.parameterSensorName = parameterSensorName;
//	}
//
//	public String getParameterSensorName() {
//		return parameterSensorName;
//	}
	
	@Override
	public void export(Exporter e) {
		super.export(e);
		e.addField( new ValueAdaptor<String>("Name of the Sensor", String.class, parameterSensorName)
				{ @Override protected void writeThrough(String arg0) { StartCounterAction.this.parameterSensorName = arg0; }});
	}

	@Override
	protected AbstractDeepCloneable deepCloneHelper(
			Map<AbstractDeepCloneable, AbstractDeepCloneable> mapDict) {
		return new StartCounterAction(this, mapDict);
	}
}
