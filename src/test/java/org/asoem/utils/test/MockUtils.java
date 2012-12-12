package org.asoem.utils.test;

import org.asoem.greyfish.utils.base.DeepCloneable;
import org.asoem.greyfish.utils.base.DeepCloner;
import org.mockito.internal.util.MockUtil;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.spy;

/**
 * User: christoph
 * Date: 11.12.12
 * Time: 17:13
 */
public class MockUtils {
    private MockUtils() {}

    public static DeepCloner mockAwareCloner() {
        final DeepCloner clonerMock = spy(DeepCloner.newInstance());
        given(clonerMock.getClone(any(DeepCloneable.class))).willAnswer(new Answer<DeepCloneable>() {
            @Override
            public DeepCloneable answer(InvocationOnMock invocation) throws Throwable {
                final Object o = invocation.getArguments()[0];
                if (new MockUtil().isMock(o)) {
                    return (DeepCloneable) o;
                }
                else
                    return (DeepCloneable) invocation.callRealMethod();
            }
        });
        given(clonerMock.getClone(any(DeepCloneable.class), any(Class.class))).willAnswer(new Answer<DeepCloneable>() {
            @Override
            public DeepCloneable answer(InvocationOnMock invocation) throws Throwable {
                final Object o = invocation.getArguments()[0];
                if (new MockUtil().isMock(o)) {
                    return (DeepCloneable) o;
                }
                else
                    return (DeepCloneable) invocation.callRealMethod();
            }
        });
        return clonerMock;
    }
}
