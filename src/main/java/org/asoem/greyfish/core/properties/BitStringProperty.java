package org.asoem.sico.core.properties;

import java.util.Map;

import org.asoem.sico.core.genes.BitStringGene;
import org.asoem.sico.core.genes.Gene;
import org.asoem.sico.lang.ClassGroup;
import org.asoem.sico.utils.AbstractDeepCloneable;
import org.uncommons.maths.binary.BitString;

@ClassGroup(tags="property")
public class BitStringProperty extends AbstractGFProperty implements DiscreteProperty<String> {

	private Gene<BitString> gene = registerGene( new BitStringGene(3, 0.01) );
	
	public BitStringProperty() {
	}
	
	protected BitStringProperty(BitStringProperty property, Map mapDict) {
		super(property, mapDict);
	}

	@Override
	protected AbstractDeepCloneable deepCloneHelper(
			Map<AbstractDeepCloneable, AbstractDeepCloneable> mapDict) {
		return new BitStringProperty(this, mapDict);
	}

	@Override
	public String getValue() {
		return gene.toString();
	}
}
