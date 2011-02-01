package org.asoem.greyfish.core.properties;

import org.asoem.greyfish.core.genes.Gene;
import org.asoem.greyfish.utils.CloneMap;
import org.asoem.greyfish.utils.DeepCloneable;

/**
 * Created by IntelliJ IDEA.
 * User: christoph
 * Date: 01.02.11
 * Time: 15:10
 * To change this template use File | Settings | File Templates.
 */
public class GeneDecorator implements Gene {

    private Gene delegate;

    public GeneDecorator(Gene<?> gene) {
       this.delegate = gene;
    }

    @Override
    public Object getRepresentation() {
        return delegate.getRepresentation();
    }

    @Override
    public void setRepresentation(Object object, Class clazz) {
        delegate.setRepresentation(object, clazz);
    }

    @Override
    public void mutate() {
        delegate.mutate();
    }

    @Override
    public void initialize() {
        delegate.initialize();
    }

    @Override
    public <T extends DeepCloneable> T deepClone(Class<T> clazz) {
        return delegate.deepClone(clazz);
    }

    @Override
    public DeepCloneable deepCloneHelper(CloneMap map) {
        return delegate.deepCloneHelper(map);
    }

    @Override
    public String getName() {
        return delegate.getName();
    }

    @Override
    public void setName(String name) {
        delegate.setName(name);
    }
}
