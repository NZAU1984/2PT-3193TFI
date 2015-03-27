package mvc.models;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.Observable;

import mvc.models.ModelException.ATTRIBUTES;
import mvc.models.ModelException.ERRORS;
import uml_parser.Aggregation;
import uml_parser.Association;
import uml_parser.ClassContent;
import uml_parser.Generalization;
import uml_parser.ParsingFailedException;
import uml_parser.Role;
import uml_parser.UmlParser;


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

	/**
	 * The filename of the current model's definition file.
	 */
	protected String filename;

	/**
	 * The charset of the current model's definition file.
	 */
	protected String charset;

	// PUBLIC CONSTRUCTOR

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

	// PROTECTED METHODS

	protected ClassContainer getClass(String name)
	{
		if(!classes.containsKey(name))
		{
			return null;
		}

		return classes.get(name);
	}

	protected void analyseModel() throws ModelException
	{
		if(null == umlModel)
		{
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

		ArrayList<String> classNames						= new ArrayList<String>();
		Iterator<Entry<String, ClassContainer>>	iterator	= getClassIterator();

		while(iterator.hasNext())
		{
			ClassContainer classContainer = getClassContainerFromIterator(iterator);

			classContainer.buildCaches();

			classNames.add(classContainer.getName());
		}

		Collections.sort(classNames);

		sendList(ListContainer.CLASS_LIST, classNames.toArray(new String[classNames.size()]));
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
	 * Checks whether or not a cycle exists in a class inheritance. It starts from the specified {@link ClassContainer}
	 * and grab all of its immediate parents.  For each of its parents, it grabs that parent's parents and checks if
	 * the latter has already been visited, and then it grabs the parents of those parents, and so on.
	 *
	 * @param startingClass	The class to start from.
	 *
	 * @return	True if there is a cycle, false otherwise.
	 */
	protected boolean classContainsInheritanceCycle(ClassContainer startingClass)
	{
		/* Let's use a HashMap to check of one class has already been visited. More efficiant than checking if
		 * an ArrayList or LinkedList contains an entry. */
		HashMap<String, Boolean> visitedClasses	= new HashMap<String, Boolean>();

		/* Let's use a LinkedList to store classes to visit. We will always visit the first item which allows an access
		 * complexity of O(1). Would be more memory-efficient if it was a single linked list... */
		LinkedList<ClassContainer> classesToVisit	= new LinkedList<ClassContainer>();

		/* Let's start from the current class. */
		classesToVisit.add(startingClass);

		/* Loop until there are no more classes to visit. */
		while(0 != classesToVisit.size())
		{
			ClassContainer currentClass	= classesToVisit.removeFirst();

			if(visitedClasses.containsKey(currentClass.getName()))
			{
				/* The item has already been visited. This means a cycle exists. Let's get out right now. */
				return true;
			}

			/* Let's add the parents of current class to the list of classes to visit. */
			for(ClassContainer parentClass : currentClass.getParents())
			{
				classesToVisit.add(parentClass);
			}

			/* Marks the current class as visited. */
			visitedClasses.put(currentClass.getName(), true);
		}

		/* At this point, no cycle was detected. */
		return false;
	}

	// TODO check logic for errors in associations...
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
				// TODO comment since logic SHOULD BE modified
				firstClass.addAssociation(association, associationName, secondClass.getName(),
						association.getFirstRole().getMultiplicity());

				exceptionInFirstClass	= false;

				secondClass.addAssociation(association, associationName, firstClass.getName(),
						association.getSecondRole().getMultiplicity());
			}
			catch (DuplicateException e)
			{
				throw resetAndReturnException(ERRORS.DUPLICATE_ASSOCIATION)
					.set(ATTRIBUTES.ASSOCIATION, associationName)
					.set(ATTRIBUTES.CLASS, (exceptionInFirstClass ? firstClass.getName() : secondClass.getName()));
			}
		}
	}

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

	protected ModelException resetAndReturnException(ModelException.ERRORS error) throws ModelException
	{
		resetModel();

		return new ModelException(error);
	}

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
