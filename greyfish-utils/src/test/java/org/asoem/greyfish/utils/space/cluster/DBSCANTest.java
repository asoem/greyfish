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

package org.asoem.greyfish.utils.space.cluster;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.io.Resources;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.csv.QuoteMode;
import org.asoem.greyfish.utils.space.ImmutablePoint1D;
import org.asoem.greyfish.utils.space.Points;
import org.junit.Before;
import org.junit.Test;

import javax.annotation.Nullable;
import java.nio.charset.Charset;
import java.util.Collection;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

public class DBSCANTest {

    private CSVParser csvRecords;

    @Before
    public void setUp() throws Exception {
        csvRecords = CSVParser.parse(Resources.getResource(DBSCANTest.class, "iris.csv"),
                Charset.defaultCharset(),
                CSVFormat.newFormat(',').withHeader().withQuote('"').withQuoteMode(QuoteMode.NON_NUMERIC));
    }

    @Test
    public void testCluster() throws Exception {
        // given
        final ImmutableList<ImmutablePoint1D> objects = ImmutableList.copyOf(
                Iterables.transform(csvRecords, new Function<CSVRecord, ImmutablePoint1D>() {
                    @Nullable
                    @Override
                    public ImmutablePoint1D apply(final CSVRecord input) {
                        final double petalLength = Double.parseDouble(input.get("Petal.Length"));
                        return ImmutablePoint1D.at(petalLength);
                    }
                }));
        assert objects.size() == 150;
        final double eps = 0.2;
        final int minPts = 5;
        final DBSCAN<ImmutablePoint1D> dbscan = DBSCAN.create(eps, minPts, Points.euclideanDistance());

        // when
        final DBSCANResult result = dbscan.apply(objects);

        // then
        assertThat((Collection<Object>) result.cluster(), hasSize(2));
    }

    @Test
    public void testAlternativeNeighborSearchAlgorithm() throws Exception {
        // given
        final ImmutableList<ImmutablePoint1D> objects = ImmutableList.copyOf(
                Iterables.transform(csvRecords, new Function<CSVRecord, ImmutablePoint1D>() {
                    @Nullable
                    @Override
                    public ImmutablePoint1D apply(final CSVRecord input) {
                        final double petalLength = Double.parseDouble(input.get("Petal.Length"));
                        return ImmutablePoint1D.at(petalLength);
                    }
                }));
        assert objects.size() == 150;
        final double eps = 0.2;
        final int minPts = 5;
        final NeighborSearch<ImmutablePoint1D> mock = mock(NeighborSearch.class);
        final DBSCAN<ImmutablePoint1D> dbscan = DBSCAN.create(eps, minPts, mock);

        // when
        dbscan.apply(objects);

        // then
        verify(mock, atLeastOnce()).filterNeighbors(eq(objects), any(ImmutablePoint1D.class), eq(eps));
    }
}