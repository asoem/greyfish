/*
 * Copyright (C) 2015 The greyfish authors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.asoem.greyfish.core.traits;

import com.google.inject.Guice;
import com.google.inject.Inject;
import org.asoem.greyfish.core.eval.GreyfishExpressionFactory;
import org.asoem.greyfish.core.inject.CoreModule;
import org.asoem.greyfish.utils.persistence.Persister;


public class MarkovGeneTest {
    @Inject
    private Persister persister;
    @Inject
    private GreyfishExpressionFactory factory;

    public MarkovGeneTest() {
        Guice.createInjector(new CoreModule()).injectMembers(this);
    }

    /*
    @Test
    public void testPersistence() throws Exception {
        // given
        final EvaluatingMarkovChain<String> markovChain = EvaluatingMarkovChain.parse("A -> B : 1.0", factory);
        final Callback<Object, String> initialState = constant("A");
        QualitativeTrait markovGene = new QualitativeTrait(markovChain, initialState);

        // when
        QualitativeTrait deserialized = Persisters.createCopy(markovGene, QualitativeTrait.class, persister);

        // then
        assertThat(deserialized.getMarkovChain()).isEqualTo(markovChain);
        assertThat(deserialized.getInitializationKernel()).isEqualTo(constant("A"));
    }
    */
}
