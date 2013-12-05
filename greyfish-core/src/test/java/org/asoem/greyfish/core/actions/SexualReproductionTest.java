package org.asoem.greyfish.core.actions;

/**
 * User: christoph Date: 21.02.12 Time: 18:34
 */
public class SexualReproductionTest {

    /**
     @Test public void testSerialization() throws Exception {
     // given
     final SexualReproduction<Basic2DAgent> action = SexualReproduction.<Basic2DAgent>builder()
     .name("test")
     .clutchSize(Callbacks.constant(1))
     .spermSupplier(Callbacks.constant(Lists.<Chromosome>newArrayList()))
     .spermSelectionStrategy(ElementSelectionStrategies.<Chromosome>randomSelection())
     .spermFitnessCallback(Callbacks.constant(0.42))
     .onSuccess(Callbacks.emptyCallback())
     .executedIf(new AlwaysTrueCondition<Basic2DAgent>())
     .build();

     // when
     final SexualReproduction<Basic2DAgent> copy = Persisters.copyAsync(action, Persisters.javaSerialization());

     // then
     assertThat(copy, is(equalTo(action)));
     }
     **/
}
