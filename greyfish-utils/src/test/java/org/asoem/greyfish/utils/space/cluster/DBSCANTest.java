package org.asoem.greyfish.utils.space.cluster;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableSet;
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
        final ImmutableSet<ImmutablePoint1D> objects = ImmutableSet.copyOf(
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
        final ImmutableSet<ImmutablePoint1D> objects = ImmutableSet.copyOf(
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
}