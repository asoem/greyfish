package org.asoem.greyfish.core.actions;

import java.util.Map;

import org.asoem.greyfish.core.properties.DoubleProperty;
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.lang.ClassGroup;
import org.asoem.greyfish.utils.AbstractDeepCloneable;
import org.simpleframework.xml.Element;

@ClassGroup(tags="action")
public class ConvertQuantityAction extends AbstractGFAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1791069218971587293L;

	@Element(name="source")
	private DoubleProperty parameterSource;

	@Element(name="target")
	private DoubleProperty parameterTarget;

	@Element(name="factor")
	private double parameterFactor;

	@Element(name="max")
	private double parameterMax;

	public ConvertQuantityAction() {
		init(null, null, 0, 0);
	}

	public ConvertQuantityAction(String name) {
		super(name);
		init(null, null, 0, 0);
	}

	protected ConvertQuantityAction(ConvertQuantityAction action,
			Map<AbstractDeepCloneable, AbstractDeepCloneable> mapDict) {
		super(action, mapDict);
		final DoubleProperty source = deepClone(action.getParameterSource(), mapDict);
		final DoubleProperty target = deepClone(action.getParameterTarget(), mapDict);
		init(source, target, action.parameterFactor, action.parameterMax);
	}

	private void init(DoubleProperty source, DoubleProperty target, double factor, double max) {
		setParameterSource(source);
		setParameterTarget(target);
		setParameterFactor(factor);
		setParameterMax(max);
	}

	@Override
	protected void performAction(Simulation simulation) {
		if (parameterSource != null && parameterTarget != null) {
			double add_amount = Math.min(parameterSource.getValue(), parameterMax) * parameterFactor;

			if (parameterTarget.getValue() + add_amount > parameterTarget.getUpperBound()) {
				add_amount = parameterTarget.getUpperBound() - parameterTarget.getValue();
			}

			parameterTarget.setValue(parameterTarget.getValue() + add_amount);
			parameterSource.setValue(parameterSource.getValue() - add_amount / parameterFactor );
		}
	}

	public DoubleProperty getParameterSource() {
		return parameterSource;
	}

	public void setParameterSource(DoubleProperty parameterSource) {
		this.parameterSource = parameterSource;
	}

	public DoubleProperty[] valuesParameterSource() {
		return getComponentOwner().getProperties(DoubleProperty.class).toArray(new DoubleProperty[0]);
	}

	public DoubleProperty getParameterTarget() {
		return parameterTarget;
	}

	public void setParameterTarget(DoubleProperty parameterTarget) {
		this.parameterTarget = parameterTarget;
	}

	public DoubleProperty[] valuesParameterTarget() {
		return getComponentOwner().getProperties(DoubleProperty.class).toArray(new DoubleProperty[0]);
	}

	public double getParameterFactor() {
		return parameterFactor;
	}

	public void setParameterFactor(double parameterFactor) {
		this.parameterFactor = parameterFactor;
	}

	public double getParameterMax() {
		return parameterMax;
	}

	public void setParameterMax(double parameterMax) {
		this.parameterMax = parameterMax;
	}

	@Override
	public void initialize(Simulation simulation) {
		super.initialize(simulation);
		if (parameterSource == null) {
			parameterSource = new DoubleProperty();
			getComponentOwner().addProperty(parameterSource);
		}
		if (parameterTarget == null) {
			parameterTarget = new DoubleProperty();
			getComponentOwner().addProperty(parameterTarget);
		}
	}

	@Override
	protected AbstractDeepCloneable deepCloneHelper(
			Map<AbstractDeepCloneable, AbstractDeepCloneable> mapDict) {
		return new ConvertQuantityAction(this, mapDict);
	}
}
