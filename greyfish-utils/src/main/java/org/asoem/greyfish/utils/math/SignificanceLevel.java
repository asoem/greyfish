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

package org.asoem.greyfish.utils.math;

/**
 * Constants for commonly used significance levels [1].
 * <h2>References</h2>
 * <ol>
 *     <li><a href="http://en.wikipedia.org/wiki/Statistical_significance">http://en.wikipedia.org/wiki/Statistical_significance</a></li>
 * </ol>
 */
public enum SignificanceLevel {
    LOW_SIGNIFICANT(0.1, ""),
    SIGNIFICANT(0.05, "*"),
    VERY_SIGNIFICANT(0.01, "**"),
    HIGHLY_SIGNIFICANT(0.001, "***");

    private final double alpha;
    private final String symbol;

    SignificanceLevel(final double alpha, final String symbol) {
        this.alpha = alpha;
        this.symbol = symbol;
    }

    public double getAlpha() {
        return alpha;
    }

    public String getSymbol() {
        return symbol;
    }

    @Override
    public String toString() {
        return "p <= " + alpha + " " + getSymbol();
    }

    public boolean check(final double p) {
        return p <= this.alpha;
    }
}
