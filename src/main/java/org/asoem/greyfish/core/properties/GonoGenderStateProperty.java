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

    private enum Gender {
        MALE("Male"),
        FEMALE("Female"),
        ASEX("Asex");

        private String name;

        Gender(String name) {
            this.name = name;
        }

        public static String[] names() {
            ImmutableList.Builder<String> builder = ImmutableList.builder();
            for (Gender g : values())
                builder.add(g.name());
            return Iterables.toArray(builder.build(), String.class);
        }
    }

    private final Supplier<Integer> gene = registerGene(
            new IntegerGene(0, new Function<Integer, Integer>() {
                @Override
                public Integer apply(Integer integer) {
                    return RandomUtils.nextDouble() < 0.001 ? 2 : RandomUtils.nextBoolean() ? 0 : 1;
                }
            }),
            Integer.class
    );

    public GonoGenderStateProperty(GonoGenderStateProperty gonoGenderStateProperty, CloneMap cloneMap) {
        super(gonoGenderStateProperty, cloneMap);
    }

    @Override
    public String getValue() {
        return Gender.values()[gene.get()].name;
    }

    @Override
    public String[] getSet() {
        return Gender.names();
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
