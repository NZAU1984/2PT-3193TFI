package mvc.models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

import uml_parser.Dataitem;
import uml_parser.Operation;

/**
 * This class is used to calculate metrics for a specific file (a specific model). One instance is created per file
 * (per model). Metrics for a specific class of the model are returned via a method call on that unique instance.
 *
 * @author Hubert Lemelin
 *
 */
public class Metrics
{
	/**
	 * All types of metrics.
	 *
	 * @author Hubert Lemelin
	 *
	 */
	public enum METRICS
	{
		ANA,
		NOM,
		NOA,
		ITC,
		ETC,
		CAC,
		DIT,
		CLD,
		NOC,
		NOD
		;

		/**
		 * Stored the metrics in an array so we can return one metric with a specific index.
		 */
		protected static final METRICS[] values	= values();

		/**
		 * Returns the metric associated with the index.
		 * @param index
		 * @return
		 */
		public static METRICS getMetricFromIndex(int index)
		{
			if((0 > index) || (index >= values.length))
			{
				return null;
			}

			return values[index];
		}

		/**
		 * Returns the number of metrics.
		 *
		 * @return	The number of metrics.
		 */
		public static int getNumberOfMetrics()
		{
			return values.length;
		}
	}

	/**
	 * HashMap containing the number of times other classes appear as the type of the attributes of the methods of the
	 * current class being analyzed. The key is the name of the class.
	 */
	protected HashMap<String, Integer> ITC;

	/**
	 * HashMap containing the number of times the current class being analyzed apears as the type of the attributes
	 * of the methods of other classes. The key is the name of the class. We use {@link AtomicInteger} to directly
	 * increment the value so we don't have to extract the value from the HashMap, increment it, and store it back.
	 */
	protected HashMap<String, AtomicInteger> ETC;

	/**
	 * HashMap containing the length of the longest path from the current class being analyzed to a superclass (the root
	 * of the inheritance tree). The key is the name of the class.
	 */
	protected HashMap<String, Integer> DIT;

	/**
	 * HashMap containing the length of the longest path from the current class being analyzed to a subclass (the leaf
	 * of the inheritane tree). The key is the name of the class.
	 */
	protected HashMap<String, Integer> CLD;

	/**
	 * HashMap containing the number of local/inherited associations/aggregations of the current class being analyzed.
	 * The key is the name of the class.
	 */
	protected HashMap<String, Integer> CAC;

	// PACKAGE CONSTRUCTOR

	/**
	 * Constructor.
	 *
	 * @param classContainer	The {@link ClassContainer} for which metrics must be calculated.
	 */
	Metrics(ClassContainer[] allClasses)
	{
		/* We store the name of the classes to distinguish between primitive type (ex. Integer) and types that refer to
		 * classes in the model. Notice that we don't check the validity of the types if it is not a class and if it
		 * not a "valid" primitive. */
		HashMap<String, ClassContainer> classNames	= new HashMap<String, ClassContainer>();

		ITC	= new HashMap<String, Integer>();

		ETC	= new HashMap<String, AtomicInteger>();

		DIT	= new HashMap<String, Integer>();

		CLD	= new HashMap<String, Integer>();

		CAC	= new HashMap<String, Integer>();

		for(ClassContainer currentClass : allClasses)
		{
			String className	= currentClass.getName();

			classNames.put(className, currentClass);

			ETC.put(className, new AtomicInteger());

			/* Let's calculate the longest path in the inheritance tree towards the root. */
			calculateLongestSuperclassPath(currentClass);

			/* Let's calculate the longest path in the inheritance tree towards the leaves. */
			calculateLongestSubclassPath(currentClass);

			/* Let's calculate the number of associations/aggregations of current class. */
			calculateNumberOfAssociationsAndAggregations(currentClass);
		}

		for(ClassContainer currentClass : allClasses)
		{
			/* Here we'll loop through all the methods of the model to defined ITC and ETC. */

			String currentClassName	= currentClass.getName();
			int currentClassITC		= 0;

			OperationContainer[] operations	= currentClass.getOperationContainers();

			for(OperationContainer currentOperation : operations)
			{
				Dataitem[] attributes	= currentOperation.getAttributes();

				for(Dataitem currentAttribute : attributes)
				{
					String type	= currentAttribute.getType();

					if(!type.equals(currentClassName) && classNames.containsKey(type))
					{
						/* Current type is a class of the model. We can update ITC and ETC. */

						++currentClassITC;

						/* ETC refers to other classes. We use an AtomicInteger to simplify to increment process. */
						ETC.get(type).incrementAndGet();
					}
				}
			}

			/* ITC refers only to current class, so let's finally store it. */
			ITC.put(currentClassName, currentClassITC);
		}
	}

	// PACKAGE METHODS

	/**
	 * Calculates all metrics and build caches.
	 */
	String[][] getMetrics(ClassContainer classContainer)
	{
		if(null == classContainer)
		{
			return null;
		}

		ArrayList<String[]> metrics	= new ArrayList<String[]>();

		String className	= classContainer.getName();

		OperationAttributeNumberContainer NOMandNOA = calculateNOMandNOA(classContainer);

		/* Average number of arguments of local methods. */
		metrics.add(new String[] {"ANA", String.valueOf(calculateANA(classContainer))});

		/* Number of local/inherited methods. */
		metrics.add(new String[] {"NOM", String.valueOf(NOMandNOA.numberOfLocalAndInheritedMethods)});

		/* Number of local/inherited attributes. */
		metrics.add(new String[] {"NOA", String.valueOf(NOMandNOA.numberOfLocalAndInheritedAttributes)});

		/* Number of times other classes appear as type of arguments of current class's methods. */
		metrics.add(new String[] {"ITC", String.valueOf(ITC.get(className))});

		/* Number of times current class appears as type of other classes' attributes. */
		metrics.add(new String[] {"ETC", String.valueOf(ETC.get(className))});

		/* Number of local/inherited associations/aggregations. */
		metrics.add(new String[] {"CAC", String.valueOf(CAC.get(className))});

		/* Longest path length to superclasses. */
		metrics.add(new String[] {"DIT", String.valueOf(DIT.get(className))});

		/* Longest path length to subclases. */
		metrics.add(new String[] {"CLD", String.valueOf(CLD.get(className))});

		/* Number of direct subclasses. */
		metrics.add(new String[] {"NOC", String.valueOf(calculateNOC(classContainer))});

		/* Number of direct/indirect subclasses. */
		metrics.add(new String[] {"NOD", String.valueOf(calculateNOD(classContainer))});

		return metrics.toArray(new String[metrics.size()][2]);
	}

	// PROTECTED METHODS

	/**
	 * Calculates the length of the longest path from the current class to the root in the inheritance tree. It is a
	 * recursive process which uses caches to speed things up.
	 *
	 * @param startingClass	The class from which we start.
	 *
	 * @return	The length of the longest path.
	 */
	protected int calculateLongestSuperclassPath(ClassContainer startingClass)
	{
		String className			= startingClass.getName();

		if(DIT.containsKey(className))
		{
			/* If already calculated, let's simply return it. */

			return DIT.get(className);
		}

		ClassContainer[] superclasses	= startingClass.getSuperclasses();

		if((null == superclasses) || (0 == superclasses.length))
		{
			/* No superclases = length of 0. */

			DIT.put(className, 0);

			return 0;
		}

		int longestSuperclassPath	= 0;

		for(ClassContainer superclass : superclasses)
		{
			/* Selects the longest path from direct superclasses. Recursion occurs here. */

			int currentLength	= calculateLongestSuperclassPath(superclass);

			longestSuperclassPath	= Math.max(currentLength, longestSuperclassPath);
		}

		/* +1 because the current class is one level below its superclasses. */
		++longestSuperclassPath;

		DIT.put(className, longestSuperclassPath);

		return longestSuperclassPath;
	}

	/**
	 * Calculates the length of the longest path from the current class to the leaves in the inheritance tree. It is a
	 * recursive process which uses caches to speed things up.
	 *
	 * @param startingClass	The class from which we start.
	 *
	 * @return	The length of the longest path.
	 */
	protected int calculateLongestSubclassPath(ClassContainer startingClass)
	{
		String className	= startingClass.getName();

		if(CLD.containsKey(className))
		{
			/* If already calculated, let's simply return it. */

			return CLD.get(className);
		}

		ClassContainer[] subclasses	= startingClass.getSubclasses();

		if((null == subclasses) || (0 == subclasses.length))
		{
			/* No subclasses = length of 0. */

			CLD.put(className, 0);

			return 0;
		}

		int longestLength	= 0;

		for(ClassContainer subclass : subclasses)
		{
			/* Selects the longest path from direct subclasses. Recursion occurs here. */

			int currentLength	= calculateLongestSubclassPath(subclass);

			longestLength	= Math.max(currentLength, longestLength);
		}

		/* +1 because the current class is one level above its subclasses. */
		++longestLength;

		CLD.put(className, longestLength);

		return longestLength;
	}

	/**
	 * Calculates the nuber of local/inherited associations and aggregations. Simply adds things up.
	 *
	 * @param startingClass	The class from which we start.
	 *
	 * @return	The number of local/inherited associations and aggregations.
	 */
	protected int calculateNumberOfAssociationsAndAggregations(ClassContainer startingClass)
	{
		String className			= startingClass.getName();

		if(CAC.containsKey(className))
		{
			/* If already calculated, let's simply return it. */

			return CAC.get(className);
		}

		/* First we use the number of associations and aggregations of the current class. */
		int numberOfAssociationsAndAggregations= startingClass.getNumberOfAggregations() + startingClass.getNumberOfAssocitions();

		ClassContainer[] superclasses	= startingClass.getSuperclasses();

		if((null == superclasses) || (0 == superclasses.length))
		{
			/* No superclasses = no recursion needed. */

			CAC.put(className, numberOfAssociationsAndAggregations);

			return numberOfAssociationsAndAggregations;
		}

		for(ClassContainer superclass : superclasses)
		{
			/* Let's simply increment the value with the value of each superclass. Recursion occurs here.*/

			numberOfAssociationsAndAggregations	+= calculateNumberOfAssociationsAndAggregations(superclass);
		}

		CAC.put(className, numberOfAssociationsAndAggregations);

		return numberOfAssociationsAndAggregations;
	}

	/**
	 * Calculates the average number of arguments of local methods.
	 *
	 * @param classContainer	The class in which the calculation has to be made.
	 *
	 * @return	The average number of attributes of the methods.
	 */
	protected float calculateANA(ClassContainer classContainer)
	{
		float averageNumberOfArguments	= 0;

		Operation[] operations	= classContainer.getOperations();

		/* Total number of operations. */
		int nOperations	= 0;

		/* Total number of arguments. */
		int nArguments	= 0;

		for(Operation operation : operations)
		{
			++nOperations;

			nArguments	+= operation.getAttributes().length;
		}

		if(0 != nOperations)
		{
			/* To avoid division by zero. */

			averageNumberOfArguments	= (nArguments + 0.0f) / nOperations;
		}

		return averageNumberOfArguments;
	}

	/**
	 * Calculates the number of local and inherited methods.
	 *
	 * @param classContainer	The class in which calculation has to be made.
	 *
	 * @return	And instance of {@link OperationAttributeNumberContainer} which is a simple container.
	 */
	protected OperationAttributeNumberContainer calculateNOMandNOA(ClassContainer classContainer)
	{
		/* This code allows multiple inheritance, meaning that a class can extends two or more classes. This exists
		 * in some languages like C++. One question arises: what happens if a method exists in two or more superclasses?
		 * According to http://www.cprogramming.com/tutorial/multiple_inheritance.html it is allowed and explicit
		 * casting has to be done to access that method in a specific superclass. So we won't throw an error if a
		 * method is defined in more than one superclass. For simplicity, if two superclasses define the same method,
		 * we'll only count it once. */

		OperationAttributeNumberContainer container	= new OperationAttributeNumberContainer();

		ClassContainer[] superclasses	= classContainer.getAllSuperclasses();

		String[] myAttributes	= classContainer.getAttributeNames();

		int numberOfLocalAndInheritedAttributes	= myAttributes.length;

		/* Let's get the methods from the class. */
		OperationContainer[] myOperations	= classContainer.getOperationContainers();

		/* At first, the number of methods is equal to the number of methods of the class. */
		int numberOfLocalAndInheritedMethods	= myOperations.length;

		/* Let's get the superclasses of the class. Since we allow multiple inheritance, there might be more than one
		 * immediate superclass. */
		//ClassContainer[] superClassesFromClass	= classContainer.getSuperclasses();

		if((null != superclasses) && (0 < superclasses.length))
		{
			/* We'll use a HashMap to store the inherited methods. This will be useful in cases such a the following:
			 *     - Class C extends class B
			 *     - Class B extends class A
			 *     - B has a method x()
			 *     - A has a method x()
			 *     - Starting from C, we count its number of methods.
			 *     - Then we go to B and detect method x(). We check if it exists in C. If it does, we don't count it since
			 *     it was already counted. Otherwise, we increment the number of operations by 1. Here it doesn't, so +1.
			 *     - Then we go to A and detect method x(). We check if it exists in C.  (same as above) It doesn't exist
			 *     in C so we increment by 1.
			 *     - We incremented twice for method x() which class C inherited from class B.
			 *     - We wrongly counted the method x() twice. It should have been counted only once.
			 *
			 *     By checking the presence of a method, we avoid counting it twice. We use a HashMap because checking the
			 *     presence of a key is around O(1) (sometimes more than O(1) but always less than O(n)). */
			HashMap<String, Boolean> inheritedMethods	= new HashMap<String, Boolean>();

			HashMap<String, Boolean> inheritedAttributes	= new HashMap<String, Boolean>();

			for(ClassContainer currentClass : superclasses)
			{
				String[] yourAttributes	= currentClass.getAttributeNames();

				for(String yourAttribute : yourAttributes)
				{
					if(!classContainer.containsAttribute(yourAttribute) && !inheritedAttributes.containsKey(yourAttribute))
					{
						++numberOfLocalAndInheritedAttributes;

						inheritedAttributes.put(yourAttribute, true);
					}
				}

				OperationContainer[] yourOperations	= currentClass.getOperationContainers();

				/* Let's loop through the superclass's methods. */
				for(OperationContainer yourOperation : yourOperations)
				{
					/* We know that identifiers can't contain '::'... */
					String key	= yourOperation.getName() + "::" + yourOperation.getSignature() + "::"
							+ yourOperation.getType();

					if(!classContainer.containsMethod(yourOperation.getName(), yourOperation.getSignature())
							&& !inheritedMethods.containsKey(key))
					{
						/* Method exists in superclass but not in the (sub) class and was not already inherited. Let's
						 * count it. */
						++numberOfLocalAndInheritedMethods;

						/* Addind to the HashMap to avoid counting twice. */
						inheritedMethods.put(key, true);
					}
				}

			}
		}

		container.numberOfLocalAndInheritedAttributes	= numberOfLocalAndInheritedAttributes;
		container.numberOfLocalAndInheritedMethods		= numberOfLocalAndInheritedMethods;

		return container;
	}

	/**
	 * Returns the number of direct subclasses.
	 *
	 * @param classContainer	The current class.
	 *
	 * @return	The number of direct subclasses.
	 */
	protected int calculateNOC(ClassContainer classContainer)
	{
		return classContainer.getSubclasses().length;
	}

	/**
	 * Returns the number of direct/indirect subclasss.
	 *
	 * @param classContainer	The current class.
	 *
	 * @return	The number of direct/indirect classes.
	 */
	protected int calculateNOD(ClassContainer classContainer)
	{
		return classContainer.getAllSubclasses().length;
	}

	/**
	 * A simple container for 2 integers returned by one method.
	 *
	 * @author Hubert Lemelin
	 *
	 */
	protected class OperationAttributeNumberContainer
	{
		int numberOfLocalAndInheritedMethods;
		int numberOfLocalAndInheritedAttributes;
	}
}
