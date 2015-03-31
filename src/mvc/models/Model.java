package mvc.models;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Observable;

import mvc.Controller;
import mvc.models.Metrics.METRICS;
import mvc.models.ModelException.ATTRIBUTES;
import mvc.models.ModelException.ERRORS;
import mvc.views.MainWindow;
import uml_parser.Aggregation;
import uml_parser.Association;
import uml_parser.ClassContent;
import uml_parser.Generalization;
import uml_parser.ParsingFailedException;
import uml_parser.Role;
import uml_parser.UmlParser;


/**
 * This class is the link between the program visible to the user ({@link MainWindow} / {@link Controller}) and
 * {@link UmlParser} (which should be viewed as a library). In the MVC design pattern, it is the model which is
 * responsible to get and manipulate data and send the to the view. The model communicates with the view by using
 * the observer design pattern. So there could be more than one view at the time and each could react differently to
 * messages sent. The view sends events to the controller which decides what to do (update the view or ask a bunch of
 * stuff to the model).
 *
 * @author Hubert Lemelin
 *
 */
public class Model extends Observable
{
	// PROTECTED PROPERTIES

	/**
	 * The {@link UmlParser} used to analyse the file provided by the user.
	 */
	protected UmlParser umlParser;

	/**
	 * The {@link uml_parser.Model} used to get the contents of a parser UML schema.
	 */
	protected uml_parser.Model umlModel;

	/**
	 * A HashMap that contains all the {@code ClassContainer}'s by using their name as the key.
	 */
	protected HashMap<String, ClassContainer> classes;

	/* Sorted class names. Used when generating the metrics file. */
	protected ArrayList<String> sortedClassNames;

	/**
	 * The filename of the current model's definition file.
	 */
	protected String filename;

	/**
	 * The charset of the current model's definition file.
	 */
	protected String charset;

	/**
	 * Determines wether or not multiple inheritance is allowed.
	 */
	protected Boolean allowMultipleInheritance	= true;

	// PUBLIC CONSTRUCTOR

	/**
	 * Constructor.
	 */
	public Model()
	{
		umlParser	= UmlParser.getInstance();
	}

	/**
	 * Analyses the specified file.
	 *
	 * @param filename			The filename of the model's definition file.
	 * @throws ModelException	Thrown when a specific error occurs.
	 */
	public void analyseFile(String filename) throws ModelException
	{
		try
		{
			/* First, let's parse the UML model using UmlParser. It will convert the file to a bunch of classes with
			 * their attributes, methods, etc. which will be contained in a class (here, a class extending Collection)
			 * which will implements the uml_parser.Model interface. If it fails, it throws a ParsingFailedException. */
			umlModel	= umlParser.parse(filename, UmlParser.UTF8_ENCODING);

			/* This will check the contents of the parsed model. Whenever an error is detected (for example, a duplicate
			 * method in a class) a ModelException is thrown, which the current method throws back to the caller. */
			analyseModel();
		}
		catch (IOException e)
		{
			throw resetAndReturnException(ERRORS.INVALID_FILE);
		}
		catch (ParsingFailedException e)
		{
			throw resetAndReturnException(ERRORS.PARSING_FAILED);
		}

		this.filename	= filename;
		charset			= UmlParser.UTF8_ENCODING; // TODO hardcoded
	}

	/**
	 * Sets whether or not multiple inheritance is allowed.
	 *
	 * @param val	{@code true} if multiple inheritance is enabled, {@code false} if not.
	 */
	public void setMultipleInheritance(Boolean val)
	{
		allowMultipleInheritance	= val;
	}

	/**
	 * Sends the main details of a class to observers (in this project, the view). Details include attributes, methods
	 * (operations), subclasses, superclasses, associations and aggregations.
	 *
	 * @param className	The class name for which details must be sent.
	 */
	public void sendClassInfo(String className)
	{
		ClassContainer currentClass	= getClass(className);

		if(null == currentClass)
		{
			/* Let's exit if the class does not exist. Maybe an exception should be thrown, but it should not
			 * happen that the model sends an invalid class name. */

			return;
		}

		sendList(ListContainer.ATTRIBUTE_LIST, currentClass.attributesCache);

		sendList(ListContainer.OPERATION_LIST, currentClass.operationsCache);

		sendList(ListContainer.SUBCLASS_LIST, currentClass.subclassesCache);

		sendList(ListContainer.SUPERCLASS_LIST, currentClass.superclassesCache);

		sendList(ListContainer.ASSOCIATION_LIST, currentClass.associationsCache);

		sendList(ListContainer.AGGREGATION_LIST, currentClass.aggregationsCache);

		sendList(ListContainer.METRIC_LIST, currentClass.metricsCache);
	}

	/**
	 * Sends details of an aggregation, contained in a class, to observers (here, the view). The index in the view is
	 * the same as the index in the {@link ClassContainer} since the {@link Model} sends the list in the order it is
	 * defined and the view does not change it.
	 *
	 * @param className	The name of the class containing the aggregation.
	 * @param index		The index of the aggregation in the class.
	 */
	public void sendAggregationDetails(String className, int index)
	{
		ClassContainer currentClass	= getClass(className);

		if((null == currentClass) || (index < 0) || (index > (currentClass.aggregationsCache.length - 1)))
		{
			/* Let's exit if the class or the aggregation does not exist. Maybe an exception should be thrown, but it
			 * should not happen that the model sends an invalid aggregation. */

			return;
		}

		/* Sends a string to observers (the view) containing a substring from the model's file. Please refer to
		 * UmlParser.getSubstringFromFile() for details. */
		sendString(umlParser.getSubstringFromFile(filename, charset,
				currentClass.aggregationsCache[index].aggregation));
	}

	/**
	 * Sends details of an association, contained in a class, to observers (here, the view). The index in the view is
	 * the same as the index in the {@link ClassContainer} since the {@link Model} sends the list in the order it is
	 * defined and the view does not change it.
	 *
	 * @param className	The name of the class containing the aggregation.
	 * @param index		The index of the association in the class.
	 */
	public void sendAssociationDetails(String className, int index)
	{
		ClassContainer currentClass	= getClass(className);

		if((null == currentClass) || (index < 0) || (index > (currentClass.associationsCache.length - 1)))
		{
			/* Let's exit if the class or the association does not exist. Maybe an exception should be thrown, but it
			 * should not happen that the model sends an invalid association. */

			return;
		}

		/* Sends a string to observers (the view) containing a substring from the model's file. Please refer to
		 * UmlParser.getSubstringFromFile() for details. */
		sendString(umlParser.getSubstringFromFile(filename, charset,
				currentClass.associationsCache[index].association));
	}

	/**
	 * Returns all the metrics of the classes contained in the currently parsed file.
	 *
	 * @return	An array of array of strings. Each outer array element (a "line") is for a class and an inner array is
	 *     for the name of the class followed by the values of the metrics.
	 */
	public String[][] getMetrics()
	{
		if((null == classes) || (null == sortedClassNames))
		{
			return null;
		}

		int nMetrics		= METRICS.getNumberOfMetrics();

		/* +1 because the first cell is the name of the class. */
		int innerSize		= 1 + nMetrics;

		String[][] metrics	= new String[classes.size()][innerSize];

		int n = 0;

		for(String className : sortedClassNames)
		{
			ClassContainer currentClass	= getClass(className);

			if(null == currentClass)
			{
				continue;
			}

			String[] currentLine	= new String[innerSize];

			/* First cell in current line = name of class. */
			currentLine[0]	= className;

			String[][]	classMetrics	= currentClass.metrics;

			for(int i = 0; i < nMetrics; ++i)
			{
				/* All other cells in current line = one specific metric value. */

				currentLine[i + 1]	= classMetrics[i][1];
			}

			metrics[n++]	= currentLine;
		}

		return metrics;
	}

	// PROTECTED METHODS

	/**
	 * Returns the {@link ClassContainer} corresponding to {@code name}.
	 *
	 * @param name	The name of the class.
	 *
	 * @return	The {@link ClassContainer} if it exists, {@code null} otherwise.
	 */
	protected ClassContainer getClass(String name)
	{
		if(!classes.containsKey(name))
		{
			return null;
		}

		return classes.get(name);
	}

	/**
	 * Transform a UML definition file into a collection of objects.
	 *
	 * @throws ModelException	Thrown when an error occurs.
	 */
	protected void analyseModel() throws ModelException
	{
		if(null == umlModel)
		{
			/* Should not happen, but NullPointerException is not our friend. */
			return;
		}

		/* This will reset the property if it previously contained data. */
		classes	= new HashMap<String, ClassContainer>();

		try
		{
			/* Creates classes and for each class, it will create attributes and methods (operaitons). A ModelException will
			 * be thrown if any of the following occurs:
			 * - Duplicate class name
			 * - Duplicate attribute name in a specific class
			 * - Duplicate method (operation) name/signature. */
			createAndCheckClasses();

			/* Links superclasses with their subclasses. Throws an error if any of the classes does not exist or an
			 * inheritance cycle is detected. */
			createAndCheckGeneralizations();

			/* Links classes through defined associations. */
			createAndCheckAssociations();

			/* Links classes through defined aggregations. */
			createAndCheckAggregations();
		}
		catch(ModelException e)
		{
			resetModel();

			throw e;
		}

		/* Below: let's loop through all classes and build caches and add their name to an ArrayList so we can sort them
		 * and send them to observers (the view). */

		{
			ClassContainer[] allClasses	= classes.values().toArray(new ClassContainer[classes.size()]);

			Metrics metrics	= new Metrics(allClasses);

			Iterator<Entry<String, ClassContainer>>	iterator	= getClassIterator();

			while(iterator.hasNext())
			{
				ClassContainer classContainer = getClassContainerFromIterator(iterator);

				classContainer.setMetrics(metrics.getMetrics(classContainer));
			}
		}

		{
			sortedClassNames									= new ArrayList<String>();
			Iterator<Entry<String, ClassContainer>>	iterator	= getClassIterator();

			while(iterator.hasNext())
			{
				ClassContainer classContainer = getClassContainerFromIterator(iterator);

				classContainer.buildCaches();

				sortedClassNames.add(classContainer.getName());
			}

			Collections.sort(sortedClassNames);

			sendList(ListContainer.CLASS_LIST, sortedClassNames.toArray(new String[sortedClassNames.size()]));

			/* Let's unlink the model so the garbage collector can clear some memory. */
			umlModel	= null;
		}
	}

	/**
	 * Create and checks classes from the model. Classes are added to a map, {@code classes}, and no duplicate class
	 * name can occur. Also, for every class, no duplicate attribute name or operation name/signature can occur.
	 *
	 * @throws ModelException	Thrown if a duplicate class name is found or if, for one class, a duplicate attribute
	 * name or operation name/signature is found.
	 */
	protected void createAndCheckClasses() throws ModelException
	{
		/* Let's loop through all the ClassContent's stored in umlModel. */
		for(ClassContent currentClass : umlModel.getClasses())
		{
			String className	= currentClass.getIdentifier();

			if(null != getClass(className))
			{
				/* Duplicate class name, let's throw an error. */

				throw resetAndReturnException(ERRORS.DUPLICATE_CLASS)
					.set(ATTRIBUTES.CLASS, className);
			}

			/* Let's add a ClassContainer to the classes map. The constructor of ClassContainer is responsible to create
			 * and check attributes and methods (operations). If an error occurs, a ModelException is thrown. */
			classes.put(className, new ClassContainer(currentClass));
		}
	}

	/**
	 * Links superclasses with their subclasses and vice-versa.
	 *
	 * @throws ModelException	Thrown whenever a superclass or a subclass does not exist.
	 */
	protected void createAndCheckGeneralizations() throws ModelException
	{
		/* Let's loop through the generalizations contained in the model. */
		for(Generalization generalization : umlModel.getGeneralizations())
		{
			String superclassName	= generalization.getSuperclassName();

			ClassContainer superClass	= null;

			if(null == (superClass = getClass(superclassName)))
			{
				/* Superclass does not exist, let's throw an exception. */

				throw resetAndReturnException(ERRORS.UNKNOWN_GENERALIZATION_SUPERCLASS)
					.set(ATTRIBUTES.SUPERCLASS, superclassName);
			}

			/* Let's loop through the generalization subclasses. */
			for(String subclassName : generalization.getSubclassNames())
			{
				ClassContainer subClass	= null;

				if(null == (subClass = getClass(subclassName)))
				{
					/* Subclass does not exist, let's throw an exception. */
					throw resetAndReturnException(ERRORS.UNKNOWN_GENERALIZATION_SUBCLASS)
						.set(ATTRIBUTES.SUPERCLASS, superclassName)
						.set(ATTRIBUTES.SUBCLASS, subclassName);
				}

				if(!allowMultipleInheritance && (0 < subClass.getNumberOfSuperClasses()))
				{
					throw resetAndReturnException(ERRORS.MULTIPLE_INHERITANCE_NOT_ALLOWED);
				}

				/* Let's link the superclass and the subclass together. */
				superClass.addSubclass(subClass);
				subClass.addSuperclass(superClass);
			}
		}

		/* Let's iterate through all the classes and check, for each, if an inheritance cycle exists. */

		Iterator<Entry<String, ClassContainer>>	iterator = getClassIterator();

		while(iterator.hasNext())
		{
			if(classContainsInheritanceCycle(getClassContainerFromIterator(iterator)))
			{
				/* Inheritance cycle detected, let's throw an exception. */

				throw resetAndReturnException(ERRORS.INHERITANCE_CYCLE);
			}
		}
	}

	/**
	 * Checks whether or not a cycle exists in a class inheritance.
	 *
	 * @param startingClass	The class to start from.
	 *
	 * @return	True if there is a cycle, false otherwise.
	 */
	protected boolean classContainsInheritanceCycle(ClassContainer startingClass)
	{
		/* Let's use a "helper" method to which we'll pass an empty HashMap which will contain, as keys, all the visited
		 * classes. This is a recursive process. For every class, there's a new call with each of its superclasses with
		 * a copy (a clone) of the HashMap to mark visited classes on that path without affecting marked classes
		 * of other paths. */
		if(inheritancePathContainsCycle(startingClass, new HashMap<String, Boolean>()))
		{
			/* A cycle was detected by visiting one of the class's superclass paths. */

			return true;
		}

		/* No cycle detected. */

		return false;
	}

	/**
	 * Checks, for every supeclass of a class, if going on that inheritance path a cycle exists. For every superclass,
	 * it calls this method again by adding the currentc class to a list of visited classes. If a superclass is
	 * already marked as visited or visiting that class's inheritance path returns true, then a cycle exists.
	 *
	 * @param startingClass		The class to start from.
	 * @param visitedClasses	A HashMap of classes already visited (keys are the names of classes).
	 *
	 * @return	True if a cycle exists, false otherwise.
	 */
	protected boolean inheritancePathContainsCycle(ClassContainer startingClass, HashMap<String, Boolean> visitedClasses)
	{
		/* Let's clone the HashMap. Otherwise it would always use the same object and a class could be mistakingly
		 * marked as visited (for example, in diamond inheritance with the two paths having a different length. */
		HashMap<String, Boolean> visitedClassesClone	= new HashMap<String, Boolean>(visitedClasses);

		/* Let's mark the current class (startingClass) as visited by adding is to the HashMap. */
		visitedClassesClone.put(startingClass.getName(), true);

		for(ClassContainer superclass : startingClass.getSuperclasses())
		{
			/* If a superclass is already marked as visited or its inheritance contains a cycle, let's return true, a
			 * cycle does exist. */
			if(visitedClassesClone.containsKey(superclass.getName()) || inheritancePathContainsCycle(superclass, visitedClassesClone))
			{
				return true;
			}
		}

		/* At this point, no cycle detected, let's return false. */

		return false;
	}

	/**
	 * Links two classes that are "associated" together.
	 *
	 * @throws ModelException	Thrown if one of the classes in the association does not exist or if an association
	 * between the two classes already exists.
	 */
	void createAndCheckAssociations() throws ModelException
	{
		/* Let's loop through the associations contained in the model. */
		for(Association association : umlModel.getAssociations())
		{
			ClassContainer firstClass		= null;
			ClassContainer secondClass		= null;
			ModelException modelException	= null;

			if(null == (firstClass = getClass(association.getFirstRole().getIdentifier())))
			{
				/* If first class does not exist, let's create an exception, but we'll throw it later. */

				modelException	= new ModelException(ERRORS.UNKNOWN_ASSOCIATION_CLASS)
					.set(ATTRIBUTES.FIRST_CLASS, association.getFirstRole().getIdentifier());
			}

			if(null == (secondClass = getClass(association.getSecondRole().getIdentifier())))
			{
				/* If second class does not exist... */
				if(null == modelException)
				{
					/* ... and modelException does not exist, let's create a new one. */

					modelException	= new ModelException(ERRORS.UNKNOWN_ASSOCIATION_CLASS)
						.set(ATTRIBUTES.FIRST_CLASS, association.getSecondRole().getIdentifier());
				}
				else
				{
					/* ... and modelException exists, let's append the second class name to it. */
					modelException.set(ATTRIBUTES.SECOND_CLASS, association.getSecondRole().getIdentifier());
				}
			}

			if(null != modelException)
			{
				/* There is an exception, so let's clean the model and throw the exception. */

				resetModel();

				throw modelException;
			}

			String associationName			= association.getIdentifier();
			boolean exceptionInFirstClass	= true;

			try
			{
				/* Tries to add the association to the first class. If it already contains an association with the same
				 * name, it throws an exception. */
				firstClass.addAssociation(association, associationName, secondClass.getName(),
						association.getFirstRole().getMultiplicity());

				/* If no exception thrown above, we toggle this value so if an exception is detected, we'll know it came
				 * from the second class. */
				exceptionInFirstClass	= false;

				/* Tries to add the association to the second class. If it already contains an association with the same
				 * name, it throws an exception. */
				secondClass.addAssociation(association, associationName, firstClass.getName(),
						association.getSecondRole().getMultiplicity());
			}
			catch (DuplicateException e)
			{
				/* Throws the exception and specifies the class from which the error occured. */

				throw resetAndReturnException(ERRORS.DUPLICATE_ASSOCIATION)
					.set(ATTRIBUTES.ASSOCIATION, associationName)
					.set(ATTRIBUTES.CLASS, (exceptionInFirstClass ? firstClass.getName() : secondClass.getName()));
			}
		}
	}

	/**
	 * Links classed that are linked together via aggregations. An aggregation has a "container" class and one or more
	 * "part" classes.
	 *
	 * @throws ModelException	Thrown whenever an error occurs.
	 */
	void createAndCheckAggregations() throws ModelException
	{
		/* Let's loop through the aggregations contained in the model. */
		for(Aggregation aggregation : umlModel.getAggregations())
		{
			String containerName			= aggregation.getRole().getIdentifier();
			ClassContainer containerClass	= null;

			if(null == (containerClass = getClass(containerName)))
			{
				/* The container class does not exist, let's throw an exception. */

				throw resetAndReturnException(ERRORS.UNKNOWN_AGGREGATION_CONTAINER_CLASS)
					.set(ATTRIBUTES.CONTAINER_CLASS, containerName);
			}

			StringBuilder partClassesSb	= new StringBuilder();
			Role[] parts				= aggregation.getPartRoles();

			/* Let's loop through the part classes. */
			for(int i = 0, iMax = parts.length; i < iMax; ++i)
			{
				String partName	= parts[i].getIdentifier();

				ClassContainer partClass	= null;

				if(null == (partClass = getClass(partName)))
				{
					/* Part class does not exist, let's throw an exception. */

					throw resetAndReturnException(ERRORS.UNKNOWN_AGGREGATION_PART_CLASS)
						.set(ATTRIBUTES.CONTAINER_CLASS, containerName)
						.set(ATTRIBUTES.PART_CLASS, partName);
				}

				/* Let's add the aggregation to the part class. */
				partClass.addAggregation(aggregation, false, containerName);

				/* Below: let's add the classname to the StringBuilder containing the names of all part classes. */

				if(0 != i)
				{
					partClassesSb.append(", ");
				}

				partClassesSb.append(partName);
			}

			/* Let's add the aggregation to the container class. */
			containerClass.addAggregation(aggregation, true, partClassesSb.toString());
		}
	}

	/**
	 * Returns an {@link Iterable} that iterates over the entry set of {@code classes}.
	 *
	 * @return	The iterator.
	 */
	Iterator<Entry<String, ClassContainer>> getClassIterator()
	{
		return classes.entrySet().iterator();
	}

	/**
	 * Directly return the instance of {@link ClassContainer} from the class iterator.
	 *
	 * @param iterator	The iterator.
	 *
	 * @return	The instance of {@link ClassContainer} at current position in the iterator.
	 */
	ClassContainer getClassContainerFromIterator(Iterator<Entry<String, ClassContainer>> iterator)
	{
		return iterator.next().getValue();
	}

	/**
	 * Sends a list through a {@link ListContainer} instance to observers.
	 *
	 * @param id	The list type (id).
	 * @param list	An object array containing the elements of the list.
	 */
	protected void sendList(long id, Object[] list)
	{
		setChanged();

		notifyObservers(ListContainer.newList(id, list));
	}

	/**
	 * Sends a {@link String} to observers.
	 *
	 * @param string	The string to be sent.
	 */
	protected void sendString(String string)
	{
		setChanged();

		notifyObservers(string);
	}

	/**
	 * Resets the model (to try to reduce memory) and returns a new exception.
	 *
	 * @param error	The type of the error.
	 *
	 * @return	A new instance of {@link ModelException}.
	 */
	protected ModelException resetAndReturnException(ModelException.ERRORS error)
	{
		resetModel();

		return new ModelException(error);
	}

	/**
	 * Resets the model by setting some properties to null so the garbage collector might be able to free some memory.
	 */
	protected void resetModel()
	{
		umlModel	= null;

		if(null == classes)
		{
			return;
		}

		classes.clear();

		classes		= null;
	}
}
