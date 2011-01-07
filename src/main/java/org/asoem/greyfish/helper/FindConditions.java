package org.asoem.greyfish.helper;

import org.asoem.greyfish.core.conditions.GFCondition;
import org.asoem.greyfish.utils.ClassFinder;

public class FindConditions {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		final ClassFinder classFinder = ClassFinder.getInstance();
		
		try {
			final Class<?>[] classes = classFinder.getAll(GFCondition.class.getPackage().getName());
			for (Class<?> class1 : classes) {
				System.out.println(class1.getName());
			}
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

}
