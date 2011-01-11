package org.asoem.greyfish.core.properties;

import com.google.common.base.Function;
import org.asoem.greyfish.core.genes.AbstractGene;
import org.asoem.greyfish.core.genes.Gene;
import org.asoem.greyfish.lang.ClassGroup;
import org.asoem.greyfish.utils.AbstractDeepCloneable;
import org.asoem.greyfish.utils.RandomUtils;

import java.util.Map;

@ClassGroup(tags="property")
public class GonoGenderStateProperty extends AbstractGFProperty implements FiniteSetProperty<String> {

	private static final long serialVersionUID = 6435914714926346900L;

	private static final String MALE = "Male";
	private static final String FEMALE = "Female";
	private static final String ASEX = "Asex";
	
	private static final String[] GENDER = new String[] {MALE, FEMALE, ASEX};
	private static final Function<Gene<Byte>, String> GENE_EXPRESSION_FUNCTION = new Function<Gene<Byte>, String>() {
		@Override
		public String apply(Gene<Byte> input) {
            assert input != null;
            assert input.getRepresentation() != null;
            return GENDER[input.getRepresentation().byteValue()];
		}
	};

	private final Gene<Byte> gene = registerGene(new AbstractGene<Byte>(new Byte((byte) 0)) {
		
		
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

	public GonoGenderStateProperty() {
	}

	protected GonoGenderStateProperty(
			GonoGenderStateProperty property,
			Map<AbstractDeepCloneable, AbstractDeepCloneable> mapDict) {
		super(property, mapDict);
	}

	@Override
	protected AbstractDeepCloneable deepCloneHelper(
			Map<AbstractDeepCloneable, AbstractDeepCloneable> mapDict) {
		return new GonoGenderStateProperty(this, mapDict);
	}

	@Override
	public String getValue() {
		return GENE_EXPRESSION_FUNCTION.apply(gene);
	}

	@Override
	public String[] getSet() {
		return GENDER;
	}
}
