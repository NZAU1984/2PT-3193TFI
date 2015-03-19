package mvc;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Observable;

import mvc.ModelException.SECTION;
import mvc.ModelException.TYPE;
import uml_parser.Aggregation;
import uml_parser.Association;
import uml_parser.ClassContent;
import uml_parser.Dataitem;
import uml_parser.Generalization;
import uml_parser.Operation;
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
	 * The {@link ErrorCenter} used to send {@link mvc.ErrorCenter.Error}'s to observers.
	 */
	protected ErrorCenter errorCenter;

	/**
	 * The {@link uml_parser.Model} used to get the contents of a parser UML schema.
	 */
	protected uml_parser.Model umlModel;

	/**
	 * A HashMap that contains all the {@code ClassContainer}'s by using their name as the key.
	 */
	protected HashMap<String, ClassContainer> classes;

	protected String filename;

	protected String charset;

	// PUBLIC CONSTRUCTOR

	public Model()
	{
		umlParser	= UmlParser.getInstance();
		errorCenter	= new ErrorCenter();
	}

	public void analyseFile(String filename) throws ModelException
	{
		try
		{
			umlModel	= umlParser.parse(filename, UmlParser.UTF8_ENCODING);

			analyseModel();
		}
		catch (IOException e)
		{
			sendError(ErrorCenter.INVALID_FILE);
		}
		catch (ParsingFailedException e)
		{
			sendError(ErrorCenter.PARSING_FAILED);
		}

		this.filename	= filename;

		charset	= UmlParser.UTF8_ENCODING;
	}

	public void sendClassInfo(String className)
	{
		ClassContainer currentClass	= getClass(className);

		if(null == currentClass)
		{
			return;
		}

		sendList(ListContainer.ATTRIBUTE_LIST, currentClass.attributesCache);

		sendList(ListContainer.OPERATION_LIST, currentClass.operationsCache);

		sendList(ListContainer.SUBCLASS_LIST, currentClass.subclassesCache);

		sendList(ListContainer.SUPERCLASS_LIST, currentClass.superclassesCache);

		sendList(ListContainer.ASSOCIATION_LIST, currentClass.associationsCache);

		sendList(ListContainer.AGGREGATION_LIST, currentClass.aggregationsCache);
	}

	public void sendAggregationDetails(String className, int index)
	{
		ClassContainer currentClass	= getClass(className);

		if((null == currentClass) || (index < 0) || (index > (currentClass.aggregationsCache.length - 1)))
		{
			return;
		}

		setChanged();

		notifyObservers(umlParser.getSubstringFromFile(filename, charset,
				currentClass.aggregationsCache[index].aggregation));
	}

	public void sendAssociationDetails(String className, int index)
	{
		ClassContainer currentClass	= getClass(className);

		if((null == currentClass) || (index < 0) || (index > (currentClass.associationsCache.length - 1)))
		{
			return;
		}

		setChanged();

		notifyObservers(umlParser.getSubstringFromFile(filename, charset,
				currentClass.associationsCache[index].association));
	}

	// PROTECTED METHODS

	protected void sendError(long errorNumber)
	{
		sendError(errorNumber, null);
	}

	protected void sendError(long errorNumber, String details)
	{
		setChanged();

		notifyObservers(errorCenter.newError(errorNumber, details));
	}

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

		classes	= new HashMap<String, ClassContainer>();

		ArrayList<String> classNames	= new ArrayList<String>();

		for(ClassContent currentClass : umlModel.getClasses())
		{
			String className	= currentClass.getIdentifier();

			if(null != getClass(className))
			{
				throwErrorAndClean(ModelException.SECTION.CLASS, ModelException.TYPE.DUPLICATE, className);

				return;
			}

			classNames.add(className);

			ClassContainer classContainer	= new ClassContainer(currentClass);

			for(Dataitem attribute : currentClass.getAttributes())
			{
				try
				{
					classContainer.addAttribute(attribute.getIdentifier(), attribute.getType());
				}
				catch (DuplicateException e)
				{
					sendError(ErrorCenter.DUPLICATE_ATTRIBUTE_NAME,
							(new StringBuilder())
								.append(attribute.getIdentifier())
								.append(" dans la classe ")
								.append(currentClass.getIdentifier())
								.toString()
					);

					resetModel();

					return;
				}
			}

			for(Operation operation : currentClass.getOperations())
			{
				StringBuilder sb	= new StringBuilder();

				Dataitem[] attributes	= operation.getAttributes();

				for(int i = 0, iMax = attributes.length; i < iMax; ++i)
				{
					if(0 != i)
					{
						sb.append(", ");
					}

					sb.append(attributes[i].getType());
				}

				try
				{
					classContainer.addOperation(operation.getIdentifier(), operation.getType(), sb.toString());
				}
				catch (DuplicateException e)
				{
					sendError(ErrorCenter.DUPLICATE_OPERATION,
							(new StringBuilder())
								.append("'")
								.append(sb.toString())
								.append("'")
								.append(" de la méthode ")
								.append(operation.getIdentifier())
								.append(" retournant '")
								.append(operation.getType())
								.append("'")
								.append(" dans la classe ")
								.append(currentClass.getIdentifier())
								.toString()
					);

					resetModel();

					return;
				}
			}

			classes.put(className, classContainer);
		}

		for(Generalization generalization : umlModel.getGeneralizations())
		{
			String parentClassName	= generalization.getIdentifier();

			ClassContainer parentClass	= null;

			if(null == (parentClass = getClass(parentClassName)))
			{
				sendError(ErrorCenter.UNKNOWN_GENERALIZATION_PARENT_CLASS, (new StringBuilder())
						.append(parentClassName)
						.toString());

				resetModel();

				return;
			}

			for(String childClassName : generalization.getSubclassNames())
			{
				ClassContainer childClass	= null;

				if(null == (childClass = getClass(childClassName)))
				{
					sendError(ErrorCenter.UNKNOWN_GENERALIZATION_CHILD_CLASS, (new StringBuilder())
							.append(childClassName)
							.append(" de la généralisation (classe parent) ")
							.append(parentClassName)
							.toString());

					resetModel();

					return;
				}

				parentClass.addChildClass(childClass);
				childClass.addParentClass(parentClass);
			}
		}

		Iterator<Entry<String, ClassContainer>>	iterator = classes.entrySet().iterator();

		while(iterator.hasNext())
		{
			Map.Entry<String, ClassContainer> item	= iterator.next();

			if(classContainsInheritanceCycle(item.getValue()))
			{
				sendError(ErrorCenter.INHERITANCE_CYCLE_DETECTED);

				resetModel();

				return;
			}
		}

		for(Association association : umlModel.getAssociations())
		{
			ClassContainer firstClass	= null;
			ClassContainer secondClass	= null;

			if(null == (firstClass = getClass(association.getFirstRole().getIdentifier())))
			{
				sendError(ErrorCenter.UNKNOWN_ASSOCIATION_CLASS, association.getFirstRole().getIdentifier());
			}
			else if(null == (secondClass = getClass(association.getSecondRole().getIdentifier())))
			{
				sendError(ErrorCenter.UNKNOWN_ASSOCIATION_CLASS, association.getSecondRole().getIdentifier());
			}

			if((null == firstClass) || (null == secondClass))
			{
				resetModel();

				return;
			}

			String associationName	= association.getIdentifier();

			try
			{
				firstClass.addAssociation(association, associationName, secondClass.getName(),
						association.getFirstRole().getMultiplicity());
				secondClass.addAssociation(association, associationName, firstClass.getName(),
						association.getSecondRole().getMultiplicity());
			}
			catch (DuplicateException e)
			{
				sendError(ErrorCenter.DUPLICATE_ASSOCIATION, associationName);
			}
		}

		for(Aggregation aggregation : umlModel.getAggregations())
		{
			String containerName			= aggregation.getRole().getIdentifier();
			ClassContainer containerClass	= null;

			if(null == (containerClass = getClass(containerName)))
			{
				sendError(ErrorCenter.UNKNOWN_AGGREGATION_CONTAINER_CLASS, containerName);
			}

			StringBuilder sb	= new StringBuilder();
			Role[] parts		= aggregation.getPartRoles();

			for(int i = 0, iMax = parts.length; i < iMax; ++i)
			{
				String partName	= parts[i].getIdentifier();

				ClassContainer partClass	= null;

				if(null == (partClass = getClass(partName)))
				{
					sendError(ErrorCenter.UNKNOWN_AGGREGATION_PART_CLASS, partName);

					resetModel();

					return;
				}

				partClass.addAggregation(aggregation, false, containerName);

				if(0 != i)
				{
					sb.append(", ");
				}

				sb.append(partName);
			}

			containerClass.addAggregation(aggregation, true, sb.toString());
		}

		Iterator<Entry<String, ClassContainer>>	iterator1 = classes.entrySet().iterator();

		while(iterator1.hasNext())
		{
			Entry<String, ClassContainer> entry	= iterator1.next();

			entry.getValue().buildCaches();
		}

		Collections.sort(classNames);

		sendList(ListContainer.CLASS_LIST, classNames.toArray(new String[classNames.size()]));
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

	protected void sendList(long id, Object[] list)
	{
		setChanged();

		notifyObservers(ListContainer.newList(id, list));
	}

	protected void throwErrorAndClean(SECTION section, TYPE duplicate, String details) throws ModelException
	{
		resetModel();

		throw new ModelException(section, duplicate, details);
	}

	protected void resetModel()
	{
		umlModel	= null;

		classes.clear();

		classes		= null;
	}

	// PRIVATE INNER CLASSES

	private class ClassContainer
	{
		// PROTECTED PROPERTIES
		protected ClassContent	classContent;

		protected ArrayList<ClassContainer> parentClasses;

		protected ArrayList<ClassContainer> childClasses;

		protected HashMap<String, String> attributes;

		protected HashMap<String, ArrayList<OperationContainer>> operations;

		protected HashMap<String, AssociationContainer> associations;

		protected ArrayList<AggregationContainer> aggregations;

		protected String[] attributesCache;
		protected String[] operationsCache;
		protected String[] subclassesCache;
		protected String[] superclassesCache;
		protected mvc.AssociationContainer[] associationsCache;
		protected mvc.AggregationContainer[] aggregationsCache;

		protected ClassContainer(ClassContent classContent)
		{
			this.classContent	= classContent;

			parentClasses	= new ArrayList<ClassContainer>();
			childClasses	= new ArrayList<ClassContainer>();
			attributes		= new HashMap<String, String>();
			operations		= new HashMap<String, ArrayList<OperationContainer>>();
			associations	= new HashMap<String, AssociationContainer>();
			aggregations	= new ArrayList<Model.AggregationContainer>();
		}

		@Override
		public String toString()
		{
			return classContent.getIdentifier();
		}

		protected void addParentClass(ClassContainer parentClass)
		{
			parentClasses.add(parentClass);
		}

		protected void addChildClass(ClassContainer childClass)
		{
			childClasses.add(childClass);
		}

		protected void addAttribute(String name, String type) throws DuplicateException
		{
			if(attributes.containsKey(name))
			{
				throw new DuplicateException();
			}

			attributes.put(name, type);
		}

		protected void addOperation(String name, String type, String signature) throws DuplicateException
		{
			ArrayList<OperationContainer> ocArrayList	= null;
			OperationContainer operationContainer		= new OperationContainer(type, signature);

			if(operations.containsKey(name))
			{
				ocArrayList	= operations.get(name);

				for(OperationContainer otherOperation : ocArrayList)
				{
					if(otherOperation.equals(operationContainer))
					{
						throw new DuplicateException();
					}
				}
			}
			else
			{
				ocArrayList	= new ArrayList<OperationContainer>();

				operations.put(name, ocArrayList);
			}

			ocArrayList.add(operationContainer);
		}

		protected void addAssociation(Association association, String name, String with, String multiplicity)
				throws DuplicateException
		{
			if(associations.containsKey(name))
			{
				throw new DuplicateException();
			}

			associations.put(name, new AssociationContainer(association, with, multiplicity));
		}

		protected void addAggregation(Aggregation aggregation, boolean isContainer, String details)
		{
			aggregations.add(new AggregationContainer(aggregation, isContainer, details));
		}

		protected String getName()
		{
			return classContent.getIdentifier();
		}

		protected ClassContainer[] getParents()
		{
			return parentClasses.toArray(new ClassContainer[parentClasses.size()]);
		}

		protected void buildCaches()
		{
			if((null == attributes) || (null == operations) || (null == childClasses) || (null == parentClasses))
			{
				return;
			}

			{
				ArrayList<String> temp	= new ArrayList<String>();


				Iterator<Entry<String, String>>	iterator = attributes.entrySet().iterator();

				while(iterator.hasNext())
				{
					Entry<String, String> val	= iterator.next();

					temp.add(val.getValue() + " " + val.getKey());
				}

				Collections.sort(temp);

				attributesCache	= temp.toArray(new String[temp.size()]);

				attributes.clear();

				attributes	= null;
			}

			{
				ArrayList<String> temp	= new ArrayList<String>();

				Iterator<Entry<String, ArrayList<OperationContainer>>>	iterator = operations.entrySet().iterator();

				while(iterator.hasNext())
				{
					Entry<String, ArrayList<OperationContainer>> val	= iterator.next();

					String opName	= val.getKey();

					for(OperationContainer oc : val.getValue())
					{
						temp.add(oc.type + " " + opName + "(" + oc.signature + ")");
					}
				}

				Collections.sort(temp);

				operationsCache	= temp.toArray(new String[temp.size()]);

				operations.clear();

				operations	= null;
			}

			{
				ArrayList<String>	temp	= new ArrayList<String>();

				for(ClassContainer currentClass : childClasses)
				{
					temp.add(currentClass.getName());
				}

				subclassesCache	= temp.toArray(new String[temp.size()]);

				childClasses.clear();

				childClasses	= null;
			}

			{
				ArrayList<String>	temp	= new ArrayList<String>();

				for(ClassContainer currentClass : parentClasses)
				{
					temp.add(currentClass.getName());
				}

				superclassesCache	= temp.toArray(new String[temp.size()]);

				parentClasses.clear();

				parentClasses	= null;
			}

			{
				ArrayList<mvc.AssociationContainer> temp	= new ArrayList<mvc.AssociationContainer>();

				Iterator<Entry<String, AssociationContainer>>	iterator = associations.entrySet().iterator();

				while(iterator.hasNext())
				{
					Entry<String, AssociationContainer> val	= iterator.next();

					temp.add(new mvc.AssociationContainer(val.getValue().association, val.getKey()));
				}

				associationsCache	= temp.toArray(new mvc.AssociationContainer[temp.size()]);

				// TODO clear associations + NULL
			}

			{
				ArrayList<mvc.AggregationContainer> temp	= new ArrayList<mvc.AggregationContainer>();

				for(AggregationContainer ac : aggregations)
				{
					temp.add(new mvc.AggregationContainer(ac.aggregation,
							(ac.isContainer ? "Contient : " : "Partie de : ") + ac.details));
				}

				aggregationsCache	= temp.toArray(new mvc.AggregationContainer[temp.size()]);

				// TODO clear aggregations + NULL
			}
		}

		public String[] getAttributes()
		{
			if(null == attributesCache)
			{
				buildCaches();
			}

			return attributesCache;
		}
	}

	private class OperationContainer
	{
		protected final String type;

		protected final String signature;

		protected OperationContainer(String type, String signature)
		{
			this.type		= type;
			this.signature	= signature;
		}

		protected boolean equals(OperationContainer oc)
		{
			return (type.equals(oc.type)) && (signature.equals(oc.signature));
		}
	}

	private class AssociationContainer
	{
		protected final Association association;

		protected final String with;

		protected final String multiplicity;

		protected AssociationContainer(Association association, String with, String multiplicity)
		{
			this.association	= association;
			this.with			= with;
			this.multiplicity	= multiplicity;
		}
	}

	private class AggregationContainer
	{
		protected final Aggregation	aggregation;

		protected final boolean isContainer;

		protected final String details;

		private AggregationContainer(Aggregation aggregation, boolean isContainer, String details)
		{
			this.aggregation	= aggregation;
			this.isContainer	= isContainer;
			this.details		= details;
		}
	}

	private class DuplicateException extends Exception
	{
		protected DuplicateException()
		{
			super();
		}

		protected DuplicateException(String message)
		{
			super(message);
		}
	}
}
