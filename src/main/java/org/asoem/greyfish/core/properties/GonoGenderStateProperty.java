package org.asoem.greyfish.core.properties;

import com.google.common.base.Function;
import org.asoem.greyfish.core.genes.AbstractGene;
import org.asoem.greyfish.core.genes.Gene;
import org.asoem.greyfish.core.individual.AbstractGFComponent;
import org.asoem.greyfish.lang.BuilderInterface;
import org.asoem.greyfish.lang.ClassGroup;
import org.asoem.greyfish.utils.AbstractDeepCloneable;
import org.asoem.greyfish.utils.RandomUtils;

import java.util.Map;

@ClassGroup(tags="property")
public class GonoGenderStateProperty extends AbstractGFProperty implements FiniteSetProperty<String> {

	private static final String MALE = "Male";
	private static final String FEMALE = "Female";
	private static final String ASEX = "Asex";
	
	private static final String[] GENDER = new String[] {MALE, FEMALE, ASEX};
	private static final Function<Gene<Byte>, String> GENE_EXPRESSION_FUNCTION = new Function<Gene<Byte>, String>() {
		@Override
		public String apply(Gene<Byte> input) {
            assert input != null;
            assert input.getRepresentation() != null;
            return GENDER[input.getRepresentation()];
		}
	};

	private final Gene<Byte> gene = registerGene(new AbstractGene<Byte>((byte) 0) {
		@Override
		public void mutate() {
			if (RandomUtils.nextDouble() < 0.001)
				setRepresentation((byte)2); // ASEX
		}

		@Override
		public void initialize() {
            setRepresentation((byte)(RandomUtils.nextBoolean() ? 0 : 1)); // Male or Female
		}
	});

    public GonoGenderStateProperty(GonoGenderStateProperty gonoGenderStateProperty, CloneMap cloneMap) {
        super(gonoGenderStateProperty, cloneMap);
    }

    @Override
	public String getValue() {
		return GENE_EXPRESSION_FUNCTION.apply(gene);
	}

	@Override
	public String[] getSet() {
		return GENDER;
	}

    @Override
    protected GonoGenderStateProperty deepCloneHelper(CloneMap cloneMap) {
        return new GonoGenderStateProperty(this, cloneMap);
    }

    private GonoGenderStateProperty() {
        this(new Builder());
    }

    protected GonoGenderStateProperty(AbstractBuilder<? extends AbstractBuilder> builder) {
        super(builder);
    }

    public static Builder with() { return new Builder(); }
    public static final class Builder extends AbstractBuilder<Builder> implements BuilderInterface<GonoGenderStateProperty> {
        private Builder() {}
        @Override protected Builder self() { return this; }
        @Override public GonoGenderStateProperty build() { return new GonoGenderStateProperty(this); }
    }

    protected static abstract class AbstractBuilder<T extends AbstractBuilder<T>> extends AbstractGFProperty.AbstractBuilder<T> {}
}
