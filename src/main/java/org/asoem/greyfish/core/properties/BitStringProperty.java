package org.asoem.greyfish.core.properties;

import org.asoem.greyfish.core.genes.BitStringGene;
import org.asoem.greyfish.core.genes.Gene;
import org.asoem.greyfish.lang.ClassGroup;
import org.asoem.greyfish.utils.AbstractDeepCloneable;
import org.uncommons.maths.binary.BitString;

import java.util.Map;

@ClassGroup(tags="property")
public class BitStringProperty extends AbstractGFProperty implements DiscreteProperty<String> {

	private Gene<BitString> gene = registerGene( new BitStringGene(3, 0.01) );

	private BitStringProperty() {
        this(new Builder());
	}

	@Override
	protected AbstractDeepCloneable deepCloneHelper(
			Map<AbstractDeepCloneable, AbstractDeepCloneable> mapDict) {
		return new Builder().fromClone(this, mapDict).build();
	}

	@Override
	public String getValue() {
		return gene.toString();
	}

    protected BitStringProperty(AbstractBuilder<?> builder) {
        super(builder);
    }

    public static final class Builder extends AbstractBuilder<Builder> {
        @Override protected Builder self() {  return this; }
    }

    protected static abstract class AbstractBuilder<T extends AbstractBuilder<T>> extends AbstractGFProperty.AbstractBuilder<T> {
        protected T fromClone(BitStringProperty property, Map<AbstractDeepCloneable, AbstractDeepCloneable> mapDict) {
            super.fromClone(property, mapDict);
            return self();
        }
         public BitStringProperty build() { return new BitStringProperty(this); }
    }
}
