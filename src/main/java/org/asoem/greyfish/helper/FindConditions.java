package org.asoem.sico.helper;

import org.asoem.sico.core.conditions.GFCondition;
import org.asoem.sico.utils.ClassFinder;

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
