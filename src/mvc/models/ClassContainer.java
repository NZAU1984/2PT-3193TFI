package mvc.models;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map.Entry;

import mvc.models.ModelException.ATTRIBUTES;
import mvc.models.ModelException.ERRORS;
import uml_parser.Aggregation;
import uml_parser.Association;
import uml_parser.ClassContent;
import uml_parser.Dataitem;
import uml_parser.Operation;
import uml_parser.UmlParser;

/**
 * This class calculates and hold all the information necessary for every class in a model.
 *
 * @author Hubert Lemelin
 *
 */
class ClassContainer
{
	// PROTECTED PROPERTIES

	/**
	 * The reference to the instance of {@link ClassContent} returned by {@link UmlParser}.
	 */
	protected ClassContent	classContent;

	/**
	 * Name of the class.
	 */
	protected String name;

	/**
	 * List of direct superclasses.
	 */
	protected ArrayList<ClassContainer> superclasses;

	/**
	 * List of direct subclasses.
	 */
	protected ArrayList<ClassContainer> subclasses;

	/**
	 * List of attributes. We use a HashMap to easily check if an attribute already exists.
	 */
	protected HashMap<String, String> attributes;

	/**
	 * List of methods. At the outer level, in the HashMap, we use the name of the operation as the key. The the inner
	 * level, in an {@link ArrayList} we store all the {@link OperationContainer} instances of the operations with
	 * the same name as the key (the name of the operation). It is easier this way to check if a duplicate of an
	 * operation exists.
	 */
	protected HashMap<String, ArrayList<OperationContainer>> operations;

	/**
	 * List of association. We use an HashMap to detect duplicate association names.
	 */
	protected HashMap<String, InnerAssociationContainer> associations;

	/**
	 * List of aggregations.
	 */
	protected ArrayList<InnerAggregationContainer> aggregations;

	/**
	 * Cache of the names of the attributes.
	 */
	protected String[] attributeNamesCache;

	/**
	 * Cache of all the {@link ClassContainer} instances of all direct/indirect superclasses. Using a cache reduce
	 * the number of computation required.
	 */
	protected ClassContainer[] allSuperclassesCache;

	/**
	 * Cache of all the {@link ClassContainer} instances of all direct/indirect subclasses. Using a cache reduce
	 * the number of computation required.
	 */
	protected ClassContainer[] allSubclassesCache;

	/**
	 * Cache of the type/name of the attributes. Used by the model to send data to the view. This way, we can unlink
	 * the attribute objects and only keep a {@link String} array. It frees some memory.
	 */
	protected String[] attributesCache;

	/**
	 * Cache of the type/name/signature of the methods. Used by the model to send data to the view. This way, we can
	 * unlink the operation objects and only keep a {@link String} array. It frees some memory.
	 */
	protected String[] operationsCache;

	/**
	 * Cache of the names of the superclasses. Used by the model to send data to the view. This way, delete the
	 * structure holding the references to other {@link ClassContainer} (which won't be deleted) and only keep a
	 * {@link String} array. It frees some memory.
	 */
	protected String[] subclassesCache;

	/**
	 * Cache of the names of the subclasses. Used by the model to send data to the view. This way, delete the
	 * structure holding the references to other {@link ClassContainer} (which won't be deleted) and only keep a
	 * {@link String} array. It frees some memory.
	 */
	protected String[] superclassesCache;

	/**
	 * Cache of the associations by using a very simple container class. Used by the model to send data to the view.
	 * This way, we can delete the structure holding the data of associations. It frees some memory.
	 */
	protected AssociationContainer[] associationsCache;

	/**
	 * Cache of the aggregations by using a very simple container class. Used by the model to send data to the view.
	 * This way, we can delete the structure holding the data of associations. It frees some memory.
	 */
	protected AggregationContainer[] aggregationsCache;

	/**
	 * Cache of the calculated metrics. Used by the model to send data to the view.
	 */
	protected String[] metricsCache;

	/**
	 * An array of arrays of {@link String}. Each outer level is one metric. In the inner level are only two elements:
	 * the name of the metric and the value of the metric.
	 */
	protected String[][] metrics;

	/**
	 * Constructor.
	 *
	 * @param classContent		The reference to the {@link ClassContent} returned by {@link UmlParser}.
	 *
	 * @throws ModelException	Thrown whenever an error occurs.
	 */
	protected ClassContainer(ClassContent classContent) throws ModelException
	{
		this.classContent	= classContent;
		name				= classContent.getIdentifier();
		superclasses		= new ArrayList<ClassContainer>();
		subclasses			= new ArrayList<ClassContainer>();
		attributes			= new HashMap<String, String>();
		operations			= new HashMap<String, ArrayList<OperationContainer>>();
		associations		= new HashMap<String, InnerAssociationContainer>();
		aggregations		= new ArrayList<InnerAggregationContainer>();

		/* Creates and checks attributes. A ModelException is thrown if a duplicate attribute name is found. */
		createAndCheckAttributes();

		/* Creates and checks methods (operations). A ModelException is thrown if a duplicate operation
		 * name/signature is found. */
		createAndCheckOperations();
	}

	// PACKAGE METHODS

	/**
	 * Returns the name of the class.
	 *
	 * @return	The name of the class.
	 */
	String getName()
	{
		return name;
	}

	/**
	 * Adds a superclass to the list of superclasses.
	 *
	 * @param superClass	The instance of the superclass.
	 */
	void addSuperclass(ClassContainer superClass)
	{
		if(superclasses.contains(superClass))
		{
			/* If superclass already defined, let's simply exit. Let's not throw an exception. */

			return;
		}

		superclasses.add(superClass);
	}

	/**
	 * Adds a subclass to the list of subclasses.
	 *
	 * @param subClass	The instance of the subclass.
	 */
	void addSubclass(ClassContainer subClass)
	{
		if(subclasses.contains(subClass))
		{
			/* If subclass already defined, let's simply exit. Let's not throw an exception. */

			return;
		}

		subclasses.add(subClass);
	}

	/**
	 * Adds an association to the class.
	 *
	 * @param association	The instance of {@link Association} containing the data about the associaiton to be added.
	 * @param name			The name of the association.
	 * @param with			The name of the other class.
	 * @param multiplicity	The multiplicity of the association.
	 *
	 * @throws DuplicateException	Thrown if an association with the same name already exists.
	 */
	void addAssociation(Association association, String name, String with, String multiplicity)
			throws DuplicateException
	{
		if(associations.containsKey(name))
		{
			/* Association with the same name already exists. */

			throw new DuplicateException();
		}

		associations.put(name, new InnerAssociationContainer(association, with, multiplicity));
	}

	/**
	 * Adds an aggregation to the class.
	 *
	 * @param aggregation	The instance of {@link Aggregation} containing the data about the aggregation to be added.
	 * @param isContainer	{@code true} if the current class is the "container class", {@code false} if the current
	 * 						class if a "part class" of the aggregation.
	 * @param details		If the current class is the container, it contains a {@link String} of all its "part
	 * 						classes". Otherwise, if the current class is a "part", it contains the name of the
	 * 						"container class".
	 */
	void addAggregation(Aggregation aggregation, boolean isContainer, String details)
	{
		aggregations.add(new InnerAggregationContainer(aggregation, isContainer, details));
	}

	/**
	 * Returns the number of direct superclasses.
	 *
	 * @return	The number of direct superclasses.
	 */
	int getNumberOfSuperClasses()
	{
		if(null != superclassesCache)
		{
			return superclassesCache.length;
		}

		if(null == superclasses)
		{
			return 0;
		}

		return superclasses.size();
	}

	/**
	 * Returns the {@link ClassContainer} instances of direct superclasses.
	 *
	 * @return The {@link ClassContainer} instances of direct superclasses.
	 */
	ClassContainer[] getSuperclasses()
	{
		if(null == superclasses)
		{
			return new ClassContainer[0];
		}

		return superclasses.toArray(new ClassContainer[superclasses.size()]);
	}


	/**
	 * Returns the {@link ClassContainer} instances of direct/indirect superclasses.
	 * @return	The {@link ClassContainer} instances of direct/indirect superclasses.
	 */
	ClassContainer[] getAllSuperclasses()
	{
		if(null != allSuperclassesCache)
		{
			return allSuperclassesCache;
		}

		LinkedList<ClassContainer> allSuperclasses	= new LinkedList<ClassContainer>();
		LinkedList<ClassContainer> classesToVisit	= new LinkedList<ClassContainer>();

		for(ClassContainer superclass : superclasses)
		{
			classesToVisit.add(superclass);
		}

		while(0 != classesToVisit.size())
		{
			ClassContainer currentClass	= classesToVisit.removeFirst();

			if(!allSuperclasses.contains(currentClass))
			{
				allSuperclasses.add(currentClass);

				if(null != currentClass.superclasses)
				{
					for(ClassContainer superclass : currentClass.superclasses)
					{
						classesToVisit.add(superclass);
					}
				}
			}
		}

		allSuperclassesCache	= allSuperclasses.toArray(new ClassContainer[allSuperclasses.size()]);

		return allSuperclassesCache;
	}

	/**
	 * Returns the {@link ClassContainer} instances of direct subclasses.
	 *
	 * @return The {@link ClassContainer} instances of direct subclasses.
	 */
	ClassContainer[] getSubclasses()
	{
		return subclasses.toArray(new ClassContainer[subclasses.size()]);
	}

	/**
	 * Returns the {@link ClassContainer} instances of direct/indirect subclasses.
	 * @return	The {@link ClassContainer} instances of direct/indirect subclasses.
	 */
	ClassContainer[] getAllSubclasses()
	{
		if(null != allSubclassesCache)
		{
			return allSubclassesCache;
		}

		LinkedList<ClassContainer> allSubclasses	= new LinkedList<ClassContainer>();
		LinkedList<ClassContainer> classesToVisit	= new LinkedList<ClassContainer>();

		for(ClassContainer subclasses : subclasses)
		{
			classesToVisit.add(subclasses);
		}

		while(0 != classesToVisit.size())
		{
			ClassContainer currentClass	= classesToVisit.removeFirst();

			if(!allSubclasses.contains(currentClass))
			{
				allSubclasses.add(currentClass);

				if(null != currentClass.subclasses)
				{
					for(ClassContainer subclass : currentClass.subclasses)
					{
						classesToVisit.add(subclass);
					}
				}
			}
		}

		allSubclassesCache	= allSubclasses.toArray(new ClassContainer[allSubclasses.size()]);

		return allSubclassesCache;
	}

	/**
	 * Returns all the {@OperationContainer} instances of the operations.
	 *
	 * @return	All the {@OperationContainer} instances of the operations.
	 */
	OperationContainer[] getOperationContainers()
	{
		LinkedList<OperationContainer> cache	= new LinkedList<OperationContainer>();

		int n	= 0;

		Iterator<Entry<String, ArrayList<OperationContainer>>>	iterator = operations.entrySet().iterator();

		while(iterator.hasNext())
		{
			Entry<String, ArrayList<OperationContainer>> val	= iterator.next();

			for(OperationContainer oc : val.getValue())
			{
				cache.add(oc);

				++n;
			}
		}

		return cache.toArray(new OperationContainer[n]);
	}

	/**
	 * Returns the number of associations.
	 *
	 * @return	The numner of associations.
	 */
	int getNumberOfAssocitions()
	{
		return associations.size();
	}

	/**
	 * Returns the number of aggregations.
	 *
	 * @return	The number of aggregations.
	 */
	int getNumberOfAggregations()
	{
		return aggregations.size();
	}

	/**
	 * Sets the (calculated) metrics. This is called by the {@link Model}.
	 *
	 * @param metrics	The calculated metrics. One metric per row. Frist inner index = metric name, second inner index
	 * 					= value.
	 */
	void setMetrics(String[][] metrics)
	{
		this.metrics	= metrics;
	}

	/**
	 * Returns the (calculated) metrics.
	 *
	 * @return	The calculated metrics. One metric per row. Frist inner index = metric name, second inner index
	 * 			= value.
	 */
	String[][] getMetrics()
	{
		return metrics;
	}

	/**
	 * Checks if the class contains an attribute with the provided name.
	 *
	 * @param name	The name of the attribute.
	 *
	 * @return	{@code true} if an attribute of the specified name exists, {@code false} otherwise.
	 */
	boolean containsAttribute(String name)
	{
		return attributes.containsKey(name);
	}

	/**
	 * Checks if the class contains an operation with the provided name/signature.
	 *
	 * @param name		The name of the operation.
	 * @param signature	The signature of the operation.
	 *
	 * @return	{@code true} if an operation of the specified name/signature exists, {@code false} otherwise.
	 */
	boolean containsMethod(String name, String signature)
	{
		ArrayList<OperationContainer> methodSignatures	= operations.get(name);

		if(null != methodSignatures)
		{
			for(OperationContainer oc : methodSignatures)
			{
				if(signature.equals(oc.getSignature()))
				{
					return true;
				}
			}
		}

		return false;
	}

	/**
	 * When the {@link Model} has finished all the calculations of all the classes, it asks every class to build caches
	 * to free memory and to minimize compuation.
	 */
	protected void buildCaches()
	{
		if((null == attributes) || (null == operations) || (null == subclasses) || (null == superclasses) || (null == metrics))
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
					temp.add(oc.getType() + " " + opName + "(" + oc.signature + ")");
				}
			}

			Collections.sort(temp);

			operationsCache	= temp.toArray(new String[temp.size()]);

			operations.clear();

			operations	= null;
		}

		{
			ArrayList<String>	temp	= new ArrayList<String>();

			for(ClassContainer currentClass : subclasses)
			{
				temp.add(currentClass.getName());
			}

			subclassesCache	= temp.toArray(new String[temp.size()]);

			subclasses.clear();

			subclasses	= null;
		}

		{
			ArrayList<String>	temp	= new ArrayList<String>();

			for(ClassContainer currentClass : superclasses)
			{
				temp.add(currentClass.getName());
			}

			superclassesCache	= temp.toArray(new String[temp.size()]);

			superclasses.clear();

			superclasses	= null;
		}

		{
			ArrayList<mvc.models.AssociationContainer> temp	= new ArrayList<mvc.models.AssociationContainer>();

			Iterator<Entry<String, InnerAssociationContainer>>	iterator = associations.entrySet().iterator();

			while(iterator.hasNext())
			{
				Entry<String, InnerAssociationContainer> val	= iterator.next();

				temp.add(new AssociationContainer(val.getValue().association, val.getKey()));
			}

			associationsCache	= temp.toArray(new mvc.models.AssociationContainer[temp.size()]);

			associations.clear();

			associations	= null;
		}

		{
			ArrayList<mvc.models.AggregationContainer> temp	= new ArrayList<mvc.models.AggregationContainer>();

			for(InnerAggregationContainer ac : aggregations)
			{
				temp.add(new AggregationContainer(ac.aggregation,
						(ac.isContainer ? "Contient : " : "Partie de : ") + ac.details));
			}

			aggregationsCache	= temp.toArray(new mvc.models.AggregationContainer[temp.size()]);

			aggregations.clear();

			aggregations	= null;
		}

		{
			metricsCache	= new String[metrics.length];

			int n = 0;

			for(String[] metric : metrics)
			{
				metricsCache[n++]	= metric[0] + " : " + metric[1];
			}
		}

		/* Hopefully this will free "lots" of memory... */
		classContent	= null;
	}

	// PROTECTED METHODS

	/**
	 * Adds an attribute to the class.
	 *
	 * @param name	Name of the attribute.
	 * @param type	Type of the attribute.
	 *
	 * @throws DuplicateException	Thrown if the attribute already exists.
	 */
	protected void addAttribute(String name, String type) throws DuplicateException
	{
		if(attributes.containsKey(name))
		{
			/* Duplicate. We don't care about the type since no two attributes can share the same name regardless of
			 * their types. */

			throw new DuplicateException();
		}

		attributes.put(name, type);
	}

	/**
	 * Adds an operation to the class.
	 *
	 * @param operation	The {@link Operation} instance containing the information about the operation to be added.
	 *
	 * @throws DuplicateException	Thrown if the operation (name/signature) already exists.
	 */
	protected void addOperation(Operation operation) throws DuplicateException
	{
		ArrayList<OperationContainer> ocArrayList	= null;
		OperationContainer operationContainer		= new OperationContainer(operation);
		String name									= operation.getIdentifier();

		if(operations.containsKey(name))
		{
			/* There already exists at least one operation with the same name. */

			/* Let's fetch the row. */
			ocArrayList	= operations.get(name);

			for(OperationContainer otherOperation : ocArrayList)
			{
				/* Let's loop through all the operations with the same name and check their signature with the operation
				 * to be added. */

				if(otherOperation.isSignatureIdentical(operationContainer))
				{
					/* Duplicate name/signature found. We don't care about the return type since no two operations can
					 * share the same name and signature regardless of their return types. */

					throw new DuplicateException();
				}
			}
		}
		else
		{
			/* No other operation already exists with the name. We create a new row. */

			ocArrayList	= new ArrayList<OperationContainer>();

			/* Let's add the newly created row to the list of all operations. */
			operations.put(name, ocArrayList);
		}

		/* No error so let's add the operation to its row. */
		ocArrayList.add(operationContainer);
	}

	/**
	 * Creates, and checks, attributes. This is called directly by the constructor.
	 *
	 * @throws ModelException	Thrown if a duplicate error occurs.
	 */
	protected void createAndCheckAttributes() throws ModelException
	{
		for(Dataitem attribute : classContent.getAttributes())
		{
			try
			{
				addAttribute(attribute.getIdentifier(), attribute.getType());
			}
			catch (DuplicateException e)
			{
				/* Duplicate attribute. */

				throw createException(ERRORS.DUPLICATE_ATTRIBUTE)
					.set(ATTRIBUTES.ATTRIBUTE, attribute.getIdentifier())
					.set(ATTRIBUTES.CLASS, getName());
			}
		}
	}

	/**
	 * Created, and checks, operations. This is called directly by the constructor.
	 *
	 * @throws ModelException	Thrown if a duplicate error occurs.
	 */
	protected void createAndCheckOperations() throws ModelException
	{
		for(Operation operation : classContent.getOperations())
		{
			try
			{
				addOperation(operation);
			}
			catch (DuplicateException e)
			{
				/* Duplicate operation. */

				throw createException(ERRORS.DUPLICATE_OPERATION)
					.set(ATTRIBUTES.OPERATION_NAME, operation.getIdentifier())
					.set(ATTRIBUTES.OPERATION_TYPE, operation.getType())
					.set(ATTRIBUTES.OPERATION_SIGNATURE, OperationContainer.formatSignature(operation))
					.set(ATTRIBUTES.CLASS, getName());
			}
		}
	}


	/**
	 * Returns all the {@link Operation} instances of the operations.
	 *
	 * @return All the {@link Operation} instances of the operations.
	 */
	protected Operation[] getOperations()
	{
		if(null == classContent)
		{
			return new Operation[0];
		}

		return classContent.getOperations();
	}

	/**
	 * Returns all the names of the attributes.
	 *
	 * @return	All the names of the attributes.
	 */
	public String[] getAttributeNames()
	{
		if(null != attributeNamesCache)
		{
			return attributeNamesCache;
		}

		int pos = 0;

		attributeNamesCache	= new String[attributes.size()];

		Iterator<Entry<String, String>>	iterator = attributes.entrySet().iterator();

		while(iterator.hasNext())
		{
			Entry<String, String> val	= iterator.next();

			attributeNamesCache[pos++]	= val.getKey();
		}

		return attributeNamesCache;
	}

	/**
	 * Creates a new {@link ModelException}. It could be used to free some memory but since an exception thrown here
	 * will be caught by the {@link Model}, all references to 'this' will be lost since the {@link Model} resets itself
	 * whenever an exception occurs.
	 *
	 * @param error	The type of the error.
	 *
	 * @return	The new exception.
	 */
	protected ModelException createException(ModelException.ERRORS error)
	{
		return new ModelException(error);
	}
}

/**
 * Simple container class for associations.
 *
 * @author Hubert Lemelin
 *
 */
class InnerAssociationContainer
{
	protected final Association association;

	protected final String with;

	protected final String multiplicity;

	InnerAssociationContainer(Association association, String with, String multiplicity)
	{
		this.association	= association;
		this.with			= with;
		this.multiplicity	= multiplicity;
	}
}

/*8
 * Simple container class for aggregations.
 */
class InnerAggregationContainer
{
	protected final Aggregation	aggregation;

	protected final boolean isContainer;

	protected final String details;

	InnerAggregationContainer(Aggregation aggregation, boolean isContainer, String details)
	{
		this.aggregation	= aggregation;
		this.isContainer	= isContainer;
		this.details		= details;
	}
}