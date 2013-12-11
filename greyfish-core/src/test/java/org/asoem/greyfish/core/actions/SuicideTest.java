package org.asoem.greyfish.core.actions;

import com.google.common.base.Function;
import org.asoem.greyfish.core.conditions.AlwaysTrueCondition;
import org.asoem.greyfish.impl.agent.Basic2DAgent;
import org.asoem.greyfish.utils.persistence.Persisters;
import org.hamcrest.Matcher;
import org.junit.Test;

import javax.annotation.Nullable;

import static org.asoem.greyfish.core.test.GreyfishMatchers.has;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * User: christoph Date: 16.10.12 Time: 17:40
 */
public class SuicideTest {
    @Test
    public void testSerialization() throws Exception {
        // given
        final Suicide<Basic2DAgent> suicide = Suicide.<Basic2DAgent>builder()
                .name("foo")
                .executedIf(AlwaysTrueCondition.<Basic2DAgent>builder().build())
                .build();

        // when
        final Suicide<Basic2DAgent> copy = Persisters.copyAsync(suicide, Persisters.javaSerialization());

        // then
        assertThat(copy, is(sameAs(suicide)));
    }

    private Matcher<Suicide<?>> sameAs(final Suicide<?> suicide) {
        return allOf(
                is(notNullValue()),
                has("name", new Function<Suicide<?>, String>() {
                    @Nullable
                    @Override
                    public String apply(final Suicide<?> input) {
                        return input.getName();
                    }
                }, equalTo(suicide.getName())),
                has("condition", new Function<Suicide<?>, Object>() {
                    @Nullable
                    @Override
                    public Object apply(final Suicide<?> input) {
                        return input.getCondition();
                    }
                }, is(instanceOf(suicide.getCondition().getClass())))
        );
    }
}
