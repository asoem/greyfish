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

import org.asoem.greyfish.core.agent.Agent;

import java.util.List;

/**
 * A Chromosome is a carrier for {@link TraitVector}s and used to transmit values of heritable {@link Trait}s from one
 * {@link Agent} to another.
 */
public interface Chromosome {

    /**
     * Get the trait vectors for this chromosome.
     *
     * @return a list of trait vectors
     */
    List<TraitVector<?>> getTraitVectors();

    /**
     * Get the size of this chromosome, which is the equal to the size of the trait vector {@link #getTraitVectors()}.
     *
     * @return the size of this chromosome
     */
    int size();

}
