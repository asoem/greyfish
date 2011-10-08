package org.asoem.greyfish.lang;

import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.junit.Test;

import javax.annotation.Nullable;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * User: christoph
 * Date: 08.10.11
 * Time: 09:42
 */
public class ImmutableMapBuilderTest {

    @Test
    public void immutableMapBuilderTest() {
        // given
        ImmutableMapBuilder<String, Integer> builder = ImmutableMapBuilder.newInstance();

        // when
        Map<String,Integer> map = builder.putAll(ImmutableList.<String>of("A","B"), Functions.<String>identity(), new Function<String, Integer>() {
            @Override
            public Integer apply(@Nullable String s) {
                return 1;
            }
        }).build();

        // then
        assertEquals(ImmutableMap.of("A", 1, "B", 1), map);
    }
}
