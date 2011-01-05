package org.asoem.sico.core.actions;

import java.awt.Color;
import java.util.Map;

import org.asoem.sico.core.simulation.Simulation;
import org.asoem.sico.lang.ClassGroup;
import org.asoem.sico.utils.AbstractDeepCloneable;
import org.asoem.sico.utils.Exporter;
import org.asoem.sico.utils.ValueAdaptor;
import org.simpleframework.xml.Element;

@ClassGroup(tags="action")
public class ChangeBodyAction extends AbstractGFAction {

	@Element(name="color")
	private Color parameterColor;
	
	public ChangeBodyAction() {
	}

	public ChangeBodyAction(String name) {
		super(name);
	}

	protected ChangeBodyAction(ChangeBodyAction action,
			Map<AbstractDeepCloneable, AbstractDeepCloneable> mapDict) {
		super(action, mapDict);
		parameterColor = action.parameterColor;
	}

	@Override
	protected void performAction(Simulation simulation) {
		getComponentOwner().getBody().setColor(parameterColor);
	}

	@Override
	protected AbstractDeepCloneable deepCloneHelper(
			Map<AbstractDeepCloneable, AbstractDeepCloneable> mapDict) {
		return new ChangeBodyAction(this, mapDict);
	}
	
	@Override
	public void export(Exporter e) {
		super.export(e);
		e.addField( new ValueAdaptor<Color>("Color", Color.class, parameterColor) {

			@Override
			protected void writeThrough(Color arg0) {
				ChangeBodyAction.this.parameterColor = arg0;
			}
		});
	}
}
