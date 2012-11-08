package org.asoem.greyfish.core.io.persistence;

import org.asoem.greyfish.core.eval.GreyfishExpressionFactoryHolder;
import org.asoem.greyfish.core.utils.EvaluatingMarkovChain;
import org.simpleframework.xml.convert.Converter;
import org.simpleframework.xml.stream.InputNode;
import org.simpleframework.xml.stream.OutputNode;

/**
 * User: christoph
 * Date: 22.02.12
 * Time: 15:17
 */
class EvaluatingMarkovChainConverter implements Converter<EvaluatingMarkovChain> {
    @Override
    public EvaluatingMarkovChain read(InputNode inputNode) throws Exception {
        return EvaluatingMarkovChain.parse(inputNode.getValue(), GreyfishExpressionFactoryHolder.get());
    }

    @Override
    public void write(OutputNode outputNode, EvaluatingMarkovChain evaluatingMarkovChain) throws Exception {
        outputNode.setValue(evaluatingMarkovChain.toRule());
    }
}
