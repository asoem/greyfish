/**
 * 
 */
package org.asoem.greyfish.core.actions;

import java.util.Collection;
import java.util.Map;

import org.asoem.greyfish.core.properties.DoubleProperty;
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.lang.ClassGroup;
import org.asoem.greyfish.utils.AbstractDeepCloneable;
import org.simpleframework.xml.Element;

/**
 * @author christoph
 * 
 */
@ClassGroup(tags="action")
public class ModifyQuantitivePropertyAction extends AbstractGFAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6872389954076565274L;

	@Element(name = "property")
	private DoubleProperty parameterQuantitiveProperty;

	@Element(name = "amount")
	private DoubleProperty parameterAmount;

	/**
	 * 
	 */
	public ModifyQuantitivePropertyAction() {
	}

	/**
	 * @param name
	 */
	public ModifyQuantitivePropertyAction(String name) {
		super(name);
	}

	protected ModifyQuantitivePropertyAction(
			ModifyQuantitivePropertyAction action,
			Map<AbstractDeepCloneable, AbstractDeepCloneable> mapDict) {
		super(action, mapDict);
		parameterQuantitiveProperty = deepClone(action.getParameterQuantitiveProperty(), mapDict);
		parameterAmount = deepClone(action.getParameterAmount(), mapDict);
	}

	public DoubleProperty getParameterQuantitiveProperty() {
		return parameterQuantitiveProperty;
	}

	public void setParameterQuantitiveProperty(
			DoubleProperty parameterQuantitativeProperty) {
		this.parameterQuantitiveProperty = parameterQuantitativeProperty;
	}

	public DoubleProperty[] valuesParameterQuantitiveProperty() {
		final Collection<DoubleProperty> ret = getComponentOwner()
		.getProperties(DoubleProperty.class);
		return ret.toArray(new DoubleProperty[ret
		                                      .size()]);
	}

	public DoubleProperty getParameterAmount() {
		return parameterAmount;
	}

	public void setParameterAmount(DoubleProperty parameterAmount) {
		this.parameterAmount = parameterAmount;
	}
	
	public DoubleProperty[] valuesParameterAmount() {
		final Collection<DoubleProperty> ret = getComponentOwner().getProperties(DoubleProperty.class);
		return ret.toArray(new DoubleProperty[ret.size()]);
	}

	@Override
	protected void performAction(Simulation simulation) {
		parameterQuantitiveProperty.setValue(Math.max(0, Math.min(
				parameterQuantitiveProperty.getUpperBound(),
				parameterQuantitiveProperty.getValue()
				+ parameterAmount.getValue())));
	}

	@Override
	public void initialize(Simulation simulation) {
		super.initialize(simulation);
		if (parameterQuantitiveProperty == null) {
			parameterQuantitiveProperty = new DoubleProperty();
			getComponentOwner().addProperty(parameterQuantitiveProperty);
		}
	}

	@Override
	protected AbstractDeepCloneable deepCloneHelper(
			Map<AbstractDeepCloneable, AbstractDeepCloneable> mapDict) {
		return new ModifyQuantitivePropertyAction(this, mapDict);
	}
}
