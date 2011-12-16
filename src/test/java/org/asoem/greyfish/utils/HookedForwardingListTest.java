package org.asoem.greyfish.utils;

import org.asoem.greyfish.utils.collect.HookedForwardingList;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.*;

import static org.mockito.Mockito.verify;

/**
 * User: christoph
 * Date: 18.09.11
 * Time: 15:15
 */
@RunWith(MockitoJUnitRunner.class)
public class HookedForwardingListTest {

    private static class ListElement {
        public void wasPassedToTheBeforeAdditionHook() {}
        public void wasPassedToTheAfterAdditionHook() {}
        public void wasPassedToTheBeforeRemovalHook() {}
        public void wasPassedToTheAfterRemovalHook() {}
        public void wasPassedToTheBeforeReplacementHook() {}
        public void wasPassedToTheAfterReplacementHook() {}
    }

    HookedForwardingList<ListElement> list;
    @Mock ListElement element;
    @Mock ListElement element1;

    @Before
    public void initList() {
        list = new HookedForwardingList<ListElement>() {
            private final List<ListElement> delegate = new ArrayList<ListElement>();

            @Override
            protected void beforeAddition(ListElement element) {
                element.wasPassedToTheBeforeAdditionHook();
            }

            @Override
            protected void afterAddition(ListElement element) {
                element.wasPassedToTheAfterAdditionHook();
            }

            @Override
            protected void beforeRemoval(ListElement element) {
                element.wasPassedToTheBeforeRemovalHook();
            }

            @Override
            protected void afterRemoval(ListElement element) {
                element.wasPassedToTheAfterRemovalHook();
            }

            @Override
            protected void beforeReplacement(ListElement oldElement, ListElement newElement) {
                oldElement.wasPassedToTheBeforeReplacementHook();
            }

            @Override
            protected void afterReplacement(ListElement oldElement, ListElement newElement) {
                oldElement.wasPassedToTheAfterReplacementHook();
            }

            @Override
            protected List<ListElement> delegate() {
                return delegate;
            }
        };
    }

    @Test
    public void testAdd() throws Exception {
        // when
        list.add(element);

        // then
        verify(element).wasPassedToTheBeforeAdditionHook();
        verify(element).wasPassedToTheAfterAdditionHook();
    }

    @Test
    public void testAddAll() throws Exception {
        // when
        list.addAll(Arrays.asList(element, element1));

        // then
        verify(element).wasPassedToTheBeforeAdditionHook();
        verify(element).wasPassedToTheAfterAdditionHook();
        verify(element1).wasPassedToTheBeforeAdditionHook();
        verify(element1).wasPassedToTheAfterAdditionHook();
    }

    @Test
    public void testRemove() throws Exception {
        // given
        list.add(element);

        // when
        list.remove(element);

        // then
        verify(element).wasPassedToTheBeforeRemovalHook();
        verify(element).wasPassedToTheAfterRemovalHook();
    }

    @Test
    public void testListIterator() throws Exception {
        // given
        list.add(element);
        ListIterator<ListElement> iterator = list.listIterator();
        iterator.next();

        // when
        iterator.remove();

        // then
        verify(element).wasPassedToTheBeforeRemovalHook();
        verify(element).wasPassedToTheAfterRemovalHook();
    }

    @Test
    public void testIterator() throws Exception {
        // given
        list.add(element);
        Iterator<ListElement> iterator = list.iterator();
        iterator.next();

        // when
        iterator.remove();

        // then
        verify(element).wasPassedToTheBeforeRemovalHook();
        verify(element).wasPassedToTheAfterRemovalHook();
    }

    @Test
    public void testSet() throws Exception {
        // given
        list.add(element);

        // when
        list.set(0, element1);

        // then
        verify(element).wasPassedToTheBeforeReplacementHook();
        verify(element).wasPassedToTheAfterReplacementHook();
    }

    @Test
    public void testClear() throws Exception {
        // given
        list.add(element);
        list.add(element1);

        // when
        list.clear();

        // then
        verify(element).wasPassedToTheBeforeRemovalHook();
        verify(element).wasPassedToTheAfterRemovalHook();
        verify(element1).wasPassedToTheBeforeRemovalHook();
        verify(element1).wasPassedToTheAfterRemovalHook();
    }
}
