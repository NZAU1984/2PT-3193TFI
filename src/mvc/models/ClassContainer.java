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

//PRIVATE INNER CLASSES

	class ClassContainer
	{
		// PROTECTED PROPERTIES
		protected ClassContent	classContent;

		protected ArrayList<ClassContainer> superclasses;

		protected ArrayList<ClassContainer> subclasses;

		protected HashMap<String, String> attributes;

		protected HashMap<String, ArrayList<OperationContainer>> operations;

		protected HashMap<String, InnerAssociationContainer> associations;

		protected ArrayList<InnerAggregationContainer> aggregations;

		protected String[] attributeNamesCache;
		protected ClassContainer[] allSuperclassesCache;

		protected String[] attributesCache;
		protected String[] operationsCache;
		protected String[] subclassesCache;
		protected String[] superclassesCache;
		protected AssociationContainer[] associationsCache;
		protected AggregationContainer[] aggregationsCache;

		protected Metrics metrics;

		protected ClassContainer(ClassContent classContent) throws ModelException
		{
			this.classContent	= classContent;

			superclasses	= new ArrayList<ClassContainer>();
			subclasses		= new ArrayList<ClassContainer>();
			attributes		= new HashMap<String, String>();
			operations		= new HashMap<String, ArrayList<OperationContainer>>();
			associations	= new HashMap<String, InnerAssociationContainer>();
			aggregations	= new ArrayList<InnerAggregationContainer>();

			/* Creates and checks attributes. A ModelException is thrown if a duplicate attribute name is found. */
			createAndCheckAttributes();

			/* Creates and checks methods (operations). A ModelException is thrown if a duplicate operation
			 * name/signature is found. */
			createAndCheckOperations();
		}

		@Override
		public String toString()
		{
			return classContent.getIdentifier();
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

		protected void addAttribute(String name, String type) throws DuplicateException
		{
			if(attributes.containsKey(name))
			{
				throw new DuplicateException();
			}

			attributes.put(name, type);
		}

		//protected void addOperation(String name, String type, String signature) throws DuplicateException
		protected void addOperation(Operation operation) throws DuplicateException
		{
			ArrayList<OperationContainer> ocArrayList	= null;
			OperationContainer operationContainer		= new OperationContainer(operation);
			String name									= operation.getIdentifier();

			if(operations.containsKey(name))
			{
				ocArrayList	= operations.get(name);

				for(OperationContainer otherOperation : ocArrayList)
				{
					if(otherOperation.isSignatureIdentical(operationContainer))
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

		void addAssociation(Association association, String name, String with, String multiplicity)
				throws DuplicateException
		{
			if(associations.containsKey(name))
			{
				throw new DuplicateException();
			}

			associations.put(name, new InnerAssociationContainer(association, with, multiplicity));
		}

		void addAggregation(Aggregation aggregation, boolean isContainer, String details)
		{
			aggregations.add(new InnerAggregationContainer(aggregation, isContainer, details));
		}

		/**
		 * Calculates different metrics. Creates an instance of {@link Metrics}.
		 */
		void calculateMetrics()
		{
			if(null != metrics)
			{
				/* Metrics already calculated. */

				return;
			}

			metrics	= new Metrics(this);

			metrics.calculate();
		}

		// CREATORS/CHECKERS

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
					throw resetAndReturnException(ERRORS.DUPLICATE_ATTRIBUTE)
						.set(ATTRIBUTES.ATTRIBUTE, attribute.getIdentifier())
						.set(ATTRIBUTES.CLASS, getName());
				}
			}
		}

		protected void createAndCheckOperations() throws ModelException
		{
			for(Operation operation : classContent.getOperations())
			{
				try
				{
					//addOperation(operation.getIdentifier(), operation.getType(), signatureSb.toString());
					addOperation(operation);
				}
				catch (DuplicateException e)
				{
					throw resetAndReturnException(ERRORS.DUPLICATE_OPERATION)
						.set(ATTRIBUTES.OPERATION_NAME, operation.getIdentifier())
						.set(ATTRIBUTES.OPERATION_TYPE, operation.getType())
						.set(ATTRIBUTES.OPERATION_SIGNATURE, OperationContainer.formatSignature(operation))
						.set(ATTRIBUTES.CLASS, getName());
				}
			}
		}

		// GETTERS

		protected String getName()
		{
			return classContent.getIdentifier();
		}

		protected Operation[] getOperations()
		{
			return classContent.getOperations();
		}

		ClassContainer[] getSuperclasses()
		{
			return superclasses.toArray(new ClassContainer[superclasses.size()]);
		}

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

		ClassContainer[] getSubclasses()
		{
			return subclasses.toArray(new ClassContainer[subclasses.size()]);
		}

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
		 * Returns the (calculated) metrics.
		 *
		 * @return	The instance of {@link Metrics}.
		 */
		Metrics getMetrics()
		{
			if(null == metrics)
			{
				/* If not already calculated, let's calculate metrics. */

				calculateMetrics();
			}

			return metrics;
		}

		// CONTAINS?

		boolean containsAttribute(String name)
		{
			return attributes.containsKey(name);
		}

		boolean containsMethod(String name, String signature)
		{
			// HashMap<String, ArrayList<OperationContainer>> operations;
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

		boolean containsMethods(OperationContainer operationContainer)
		{
			//return operations1.contains(operationContainer);

			//ListIterator<OperationContainer>

			return false;
		}

		// ...

		protected void buildCaches()
		{
			if((null == attributes) || (null == operations) || (null == subclasses) || (null == superclasses))
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

				// TODO clear associations + NULL
			}

			{
				ArrayList<mvc.models.AggregationContainer> temp	= new ArrayList<mvc.models.AggregationContainer>();

				for(InnerAggregationContainer ac : aggregations)
				{
					temp.add(new AggregationContainer(ac.aggregation,
							(ac.isContainer ? "Contient : " : "Partie de : ") + ac.details));
				}

				aggregationsCache	= temp.toArray(new mvc.models.AggregationContainer[temp.size()]);

				// TODO clear aggregations + NULL
			}

			// need it for name...
			//classContent	= null;
		}

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
				//temp.add(val.getValue() + " " + val.getKey());
			}

			return attributeNamesCache;
		}

		public String[] getAttributes()
		{
			if(null == attributesCache)
			{
				buildCaches();
			}

			return attributesCache;
		}

		ModelException resetAndReturnException(ModelException.ERRORS error) throws ModelException
		{
			//resetModel();

			return new ModelException(error);
		}
	}

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