package org.asoem.greyfish.core.properties;

import org.asoem.greyfish.core.share.Consumer;
import org.asoem.greyfish.core.share.ConsumerGroup;
import org.asoem.greyfish.core.simulation.ParallelizedSimulation;
import org.asoem.greyfish.lang.BuilderInterface;
import org.asoem.greyfish.lang.ClassGroup;
import org.asoem.greyfish.utils.DeepCloner;

@ClassGroup(tags="property")
public class ResourceProperty extends DoubleProperty {

    private final ConsumerGroup<DoubleProperty> consumerGroup = new ConsumerGroup<DoubleProperty>(getName());

    public ResourceProperty(ResourceProperty property, DeepCloner cloner) {
        super(property, cloner);
    }

    public ConsumerGroup<? extends DoubleProperty> getConsumerGroup() {
        return consumerGroup;
    }

    public void addConsumer(Consumer<DoubleProperty> consumer) {
        consumerGroup.addConsumer(consumer);
    }

    @Override
    public void prepare(ParallelizedSimulation simulation) {
        super.prepare(simulation);
        consumerGroup.removeAllConsumer();
    }

    @Override
    public DoubleProperty deepClone(DeepCloner cloner) {
        return new ResourceProperty(this, cloner);
    }

    protected ResourceProperty(DoubleProperty.AbstractBuilder<? extends DoubleProperty.AbstractBuilder> builder) {
        super(builder);
    }

    private ResourceProperty() {
        super(new Builder());
    }

//    public static Builder with() { return new Builder(); }
    public static final class Builder extends AbstractBuilder<Builder> implements BuilderInterface<ResourceProperty> {
        public Builder() {}
        @Override protected Builder self() { return this; }
        @Override public ResourceProperty build() { return new ResourceProperty(this); }
    }

    protected static abstract class AbstractBuilder<T extends AbstractBuilder<T>> extends DoubleProperty.AbstractBuilder<T> {}
}
