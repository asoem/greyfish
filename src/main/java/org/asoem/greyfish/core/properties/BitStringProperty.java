package org.asoem.greyfish.core.properties;

import org.asoem.greyfish.core.genes.BitStringGene;
import org.asoem.greyfish.core.genes.Gene;
import org.asoem.greyfish.lang.BuilderInterface;
import org.asoem.greyfish.lang.ClassGroup;
import org.asoem.greyfish.utils.CloneMap;
import org.uncommons.maths.binary.BitString;

@ClassGroup(tags="property")
public class BitStringProperty extends AbstractGFProperty implements DiscreteProperty<String> {

	private final Gene<BitString> gene = registerGene( new BitStringGene(3, 0.01) );

	private BitStringProperty() {
        this(new Builder());
	}

    protected BitStringProperty(BitStringProperty property, CloneMap cloneMap) {
        super(property, cloneMap);
    }

    @Override
    public BitStringProperty deepCloneHelper(CloneMap cloneMap) {
        return new BitStringProperty(this, cloneMap);
    }

    @Override
	public String getValue() {
		return gene.toString();
	}

    protected BitStringProperty(AbstractBuilder<?> builder) {
        super(builder);
    }

    public static final class Builder extends AbstractBuilder<Builder> implements BuilderInterface<BitStringProperty> {
        @Override protected Builder self() {  return this; }
        public BitStringProperty build() { return new BitStringProperty(this); }
    }

    protected static abstract class AbstractBuilder<T extends AbstractBuilder<T>> extends AbstractGFProperty.AbstractBuilder<T> {
    }
}
