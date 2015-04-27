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

import org.asoem.greyfish.core.properties.AgentProperty;
import org.asoem.greyfish.utils.collect.Product2;

import java.util.List;

public interface AgentTrait<C, T> extends AgentProperty<C, T> {

    /**
     * Transform the given {@code value}. <p>This method is intended to be used for mutation</p>
     *
     * @param context the context for this trait
     * @param value   the value to mutate
     * @return the transformed value
     */
    T transform(C context, T value);

    /**
     * Transform the given values {@code allele1} and {@code allele2} into new value. <p>This method is intended to be
     * used for recombination</p>
     *
     * @param context the context for this trait
     * @param allele1 the first input value
     * @param allele2 the second input value
     * @return a pair of transformed values based on the input values
     */
    Product2<T, T> transform(C context, T allele1, T allele2);

    /**
     * Transform the given {@code values} into a list of new values. <p>This method makes no assumptions about the
     * correlation between the number of input and output values</p>
     *
     * @param context the context for this trait
     * @param alleles the input values
     * @return a list of new values based on the input values
     */
    List<T> transform(C context, List<? extends T> alleles);

    /*
     * Sets the connected agent. This method should only be called by an Agent implementation in an addXXX method.
     *
     * @param agent the new agent
     */
    //void setAgent(@Nullable A agent);

}
