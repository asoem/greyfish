package org.asoem.greyfish.utils;

import com.google.common.base.Preconditions;
import javolution.util.FastList;
import org.asoem.greyfish.lang.Functor;

public class FastLists {

	public static <E> void foreach(FastList<E> list, Functor<E> functor) {
		Preconditions.checkNotNull(list);
		Preconditions.checkNotNull(functor);
		for (FastList.Node<E> n = list.head(), end = list.tail(); (n = n.getNext()) != end;) {
			assert (n != null);
			functor.update(n.getValue());
		}
	}
}
