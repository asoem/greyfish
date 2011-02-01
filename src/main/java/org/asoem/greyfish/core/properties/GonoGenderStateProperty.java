package org.asoem.greyfish.core.properties;

import com.google.common.base.Function;
import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import org.asoem.greyfish.core.genes.IntegerGene;
import org.asoem.greyfish.lang.BuilderInterface;
import org.asoem.greyfish.lang.ClassGroup;
import org.asoem.greyfish.utils.CloneMap;
import org.asoem.greyfish.utils.RandomUtils;

@ClassGroup(tags="property")
public class GonoGenderStateProperty extends AbstractGFProperty implements FiniteSetProperty<String> {

    private enum GENDER {
        MALE("Male"),
        FEMALE("Female"),
        ASEX("Asex");

        private String name;

        GENDER(String name) {
            this.name = name;
        }

        public static String[] names() {
            ImmutableList.Builder<String> builder = ImmutableList.builder();
            for (GENDER g : values())
                builder.add(g.name());
            return Iterables.toArray(builder.build(), String.class);
        }
    }

	private static final Function<Integer, String> GENE_EXPRESSION_FUNCTION = new Function<Integer, String>() {
		@Override
		public String apply(Integer input) {
            assert input != null;
            return GENDER.values()[input].name();
		}
	};

	private final Supplier<Integer> gene = registerGene(new IntegerGene(0, 0, 2) {
		@Override
		public void mutate() {
			if (RandomUtils.nextDouble() < 0.001)
				setRepresentation(2, null); // ASEX
		}

		@Override
		public void initialize() {
            setRepresentation((RandomUtils.nextBoolean() ? 0 : 1), null); // Male or Female
		}
	}, Integer.class);

    public GonoGenderStateProperty(GonoGenderStateProperty gonoGenderStateProperty, CloneMap cloneMap) {
        super(gonoGenderStateProperty, cloneMap);
    }

    @Override
	public String getValue() {
		return GENE_EXPRESSION_FUNCTION.apply(gene.get());
	}

	@Override
	public String[] getSet() {
		return GENDER.names();
	}

    @Override
    public GonoGenderStateProperty deepCloneHelper(CloneMap cloneMap) {
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
