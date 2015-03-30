package mvc.models;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

import uml_parser.Dataitem;
import uml_parser.Operation;

public class Metrics
{
	protected static HashMap<String, Integer> ITC;

	protected static HashMap<String, AtomicInteger> ETC;

	protected static HashMap<String, Integer> DIT;

	protected static HashMap<String, Integer> CLD;

	/**
	 * The reference to the {@link ClassContainer} for which metrics must be calculated.
	 */
	ClassContainer classContainer;

	float averageNumberOfArguments	= 0;

	int numberOfLocalAndInheritedMethods	= 0;

	int numberOfLocalAndInheritedAttributes	= 0;

	int numberOfDirectSubclasses	= 0;

	int numberOfDirectAndIndirectSubclasses	= 0;

	// STATIC
	public static void resetAndPrecalculate(ClassContainer[] allClasses)
	{
		HashMap<String, ClassContainer> classNames	= new HashMap<String, ClassContainer>();

		ITC	= new HashMap<String, Integer>();

		ETC	= new HashMap<String, AtomicInteger>();

		DIT	= new HashMap<String, Integer>();

		CLD	= new HashMap<String, Integer>();

		for(ClassContainer currentClass : allClasses)
		{
			String className	= currentClass.getName();

			classNames.put(className, currentClass);
			ITC.put(className, 0);
			ETC.put(className, new AtomicInteger());

			calculateLongestSuperclassPath(currentClass);

			calculateLongestSubclassPath(currentClass);
		}

		for(ClassContainer currentClass : allClasses)
		{
			String currentClassName	= currentClass.getName();
			int currentClassITC			= 0;

			System.out.println("Analyzing argument of class " + currentClassName);

			OperationContainer[] operations	= currentClass.getOperationContainers();

			for(OperationContainer currentOperation : operations)
			{
				Dataitem[] attributes	= currentOperation.getAttributes();

				for(Dataitem currentAttribute : attributes)
				{
					String type	= currentAttribute.getType();

					if(!type.equals(currentClassName) && classNames.containsKey(type))
					{
						System.out.println(".. type " + type + " is not current class and is another class");

						++currentClassITC;

						ETC.get(type).incrementAndGet();
					}
				}
			}

			ITC.put(currentClassName, currentClassITC);
		}

		for(ClassContainer currentClass : allClasses)
		{
			String name=currentClass.getName();
			System.out.println("Class " + name + " : ITC=" + ITC.get(name) + ", ETC=" + ETC.get(name).intValue() + ", DIT=" + DIT.get(name) + ", CLD=" + CLD.get(name));
		}
	}

	protected static int calculateLongestSuperclassPath(ClassContainer startingClass)
	{
		String className	= startingClass.getName();

		if(DIT.containsKey(className))
		{
			return DIT.get(className);
		}

		ClassContainer[] superclasses	= startingClass.getSuperclasses();

		if((null == superclasses) || (0 == superclasses.length))
		{
			DIT.put(className, 0);

			return 0;
		}

		int longestLength	= 0;

		for(ClassContainer superclass : superclasses)
		{
			int currentLength	= calculateLongestSuperclassPath(superclass);

			longestLength	= Math.max(currentLength, longestLength);
		}

		++longestLength;

		DIT.put(className, longestLength);

		return longestLength;
	}

	protected static int calculateLongestSubclassPath(ClassContainer startingClass)
	{
		String className	= startingClass.getName();

		if(CLD.containsKey(className))
		{
			return CLD.get(className);
		}

		ClassContainer[] subclasses	= startingClass.getSubclasses();

		if((null == subclasses) || (0 == subclasses.length))
		{
			CLD.put(className, 0);

			return 0;
		}

		int longestLength	= 0;

		for(ClassContainer subclass : subclasses)
		{
			int currentLength	= calculateLongestSubclassPath(subclass);

			longestLength	= Math.max(currentLength, longestLength);
		}

		++longestLength;

		CLD.put(className, longestLength);

		return longestLength;
	}

	// CONSTRUCTOR

	/**
	 * Constructor.
	 *
	 * @param classContainer	The {@link ClassContainer} for which metrics must be calculated.
	 */
	public Metrics(ClassContainer classContainer)
	{
		this.classContainer	= classContainer;
	}

	/**
	 * Calculates all metrics and build caches.
	 */
	public void calculate()
	{
		if(null == classContainer)
		{
			return;
		}

		System.out.println("Calculating metrics for class " + classContainer.getName());

		/* Average number of arguments of local methods. */
		calculateANA();

		calculateNOMandNOA();

		calculateNOC();

		calculateNOD();



		/* Unlinking classContainer so memory might be freed if no more references point to it. */
		classContainer	= null;
	}

	/**
	 * Calculates the average number of arguments of local methods.
	 */
	protected void calculateANA()
	{
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
			averageNumberOfArguments	= (nArguments + 0.0f) / nOperations;
		}
	}

	/**
	 * Calculates the number of local and inherited methods.
	 */
	protected void calculateNOMandNOA()
	{
		/* This code allows multiple inheritance, meaning that a class can extends two or more classes. This exists
		 * in some languages like C++. One question arises: what happens if a method exists in two or more superclasses?
		 * According to http://www.cprogramming.com/tutorial/multiple_inheritance.html it is allowed and explicit
		 * casting has to be done to access that method in a specific superclass. So we won't throw an error if a
		 * method is defined in more than one superclass. For simplicity, if two superclasses define the same method,
		 * we'll only count it once. */

		ClassContainer[] superclasses	= classContainer.getAllSuperclasses();

		String[] myAttributes	= classContainer.getAttributeNames();

		numberOfLocalAndInheritedAttributes	= myAttributes.length;

		/* Let's get the methods from the class. */
		OperationContainer[] myOperations	= classContainer.getOperationContainers();

		/* At first, the number of methods is equal to the number of methods of the class. */
		numberOfLocalAndInheritedMethods	= myOperations.length;

		//System.out.println("Class " + classContainer.getName() + " contains " +  numberOfLocalAndInheritedMethods + " local methods");

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

			//while(0 != superClasses.size())
			for(ClassContainer currentClass : superclasses)
			{
				//ClassContainer currentClass	= superClasses.removeFirst();

				//System.out.println("==== Comparing with superclass " + currentClass.getName());

				String[] yourAttributes	= currentClass.getAttributeNames();

				for(String yourAttribute : yourAttributes)
				{
					//System.out.println("xxx " + yourAttribute);

					if(!classContainer.containsAttribute(yourAttribute) && !inheritedAttributes.containsKey(yourAttribute))
					{
						//System.out.println("### attrib " + yourAttribute + " added to inherited..");

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
						//System.out.println("### method " + yourOperation.getName() + " (" + yourOperation.getType() + ") added to inherited..");

						++numberOfLocalAndInheritedMethods;

						/* Addind to the HashMap to avoid counting twice. */
						inheritedMethods.put(key, true);
					}
				}

			}
		}
	}

	protected void calculateNOC()
	{
		numberOfDirectSubclasses	= classContainer.getSuperclasses().length;
	}

	protected void calculateNOD()
	{
		numberOfDirectAndIndirectSubclasses	= classContainer.getAllSuperclasses().length;
	}
}
