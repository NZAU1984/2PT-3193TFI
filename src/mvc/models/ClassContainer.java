package mvc.models;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
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

		protected ArrayList<ClassContainer> superClasses;

		protected ArrayList<ClassContainer> subClasses;

		protected HashMap<String, String> attributes;

		protected HashMap<String, ArrayList<OperationContainer>> operations;

		protected HashMap<String, AssociationContainer111> associations;

		protected ArrayList<AggregationContainer111> aggregations;

		protected String[] attributesCache;
		protected String[] operationsCache;
		protected String[] subclassesCache;
		protected String[] superclassesCache;
		protected AssociationContainer[] associationsCache;
		protected AggregationContainer[] aggregationsCache;

		protected ClassContainer(ClassContent classContent) throws ModelException
		{
			this.classContent	= classContent;

			superClasses	= new ArrayList<ClassContainer>();
			subClasses		= new ArrayList<ClassContainer>();
			attributes		= new HashMap<String, String>();
			operations		= new HashMap<String, ArrayList<OperationContainer>>();
			associations	= new HashMap<String, AssociationContainer111>();
			aggregations	= new ArrayList<AggregationContainer111>();

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
		protected void addSuperclass(ClassContainer superClass)
		{
			if(superClasses.contains(superClass))
			{
				/* If superclass already defined, let's simply exit. Let's not throw an exception. */

				return;
			}

			superClasses.add(superClass);
		}

		/**
		 * Adds a subclass to the list of subclasses.
		 *
		 * @param subClass	The instance of the subclass.
		 */
		protected void addSubclass(ClassContainer subClass)
		{
			if(subClasses.contains(subClass))
			{
				/* If subclass already defined, let's simply exit. Let's not throw an exception. */

				return;
			}

			subClasses.add(subClass);
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

		protected void addAssociation(Association association, String name, String with, String multiplicity)
				throws DuplicateException
		{
			if(associations.containsKey(name))
			{
				throw new DuplicateException();
			}

			associations.put(name, new AssociationContainer111(association, with, multiplicity));
		}

		protected void addAggregation(Aggregation aggregation, boolean isContainer, String details)
		{
			aggregations.add(new AggregationContainer111(aggregation, isContainer, details));
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

		protected ClassContainer[] getParents()
		{
			return superClasses.toArray(new ClassContainer[superClasses.size()]);
		}

		protected void buildCaches()
		{
			if((null == attributes) || (null == operations) || (null == subClasses) || (null == superClasses))
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

				for(ClassContainer currentClass : subClasses)
				{
					temp.add(currentClass.getName());
				}

				subclassesCache	= temp.toArray(new String[temp.size()]);

				subClasses.clear();

				subClasses	= null;
			}

			{
				ArrayList<String>	temp	= new ArrayList<String>();

				for(ClassContainer currentClass : superClasses)
				{
					temp.add(currentClass.getName());
				}

				superclassesCache	= temp.toArray(new String[temp.size()]);

				superClasses.clear();

				superClasses	= null;
			}

			{
				ArrayList<mvc.models.AssociationContainer> temp	= new ArrayList<mvc.models.AssociationContainer>();

				Iterator<Entry<String, AssociationContainer111>>	iterator = associations.entrySet().iterator();

				while(iterator.hasNext())
				{
					Entry<String, AssociationContainer111> val	= iterator.next();

					temp.add(new AssociationContainer(val.getValue().association, val.getKey()));
				}

				associationsCache	= temp.toArray(new mvc.models.AssociationContainer[temp.size()]);

				// TODO clear associations + NULL
			}

			{
				ArrayList<mvc.models.AggregationContainer> temp	= new ArrayList<mvc.models.AggregationContainer>();

				for(AggregationContainer111 ac : aggregations)
				{
					temp.add(new AggregationContainer(ac.aggregation,
							(ac.isContainer ? "Contient : " : "Partie de : ") + ac.details));
				}

				aggregationsCache	= temp.toArray(new mvc.models.AggregationContainer[temp.size()]);

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

		 ModelException resetAndReturnException(ModelException.ERRORS error) throws ModelException
		{
			//resetModel();

			return new ModelException(error);
		}
	}

	class AssociationContainer111
	{
		protected final Association association;

		protected final String with;

		protected final String multiplicity;

		AssociationContainer111(Association association, String with, String multiplicity)
		{
			this.association	= association;
			this.with			= with;
			this.multiplicity	= multiplicity;
		}
	}

	class AggregationContainer111
	{
		protected final Aggregation	aggregation;

		protected final boolean isContainer;

		protected final String details;

		AggregationContainer111(Aggregation aggregation, boolean isContainer, String details)
		{
			this.aggregation	= aggregation;
			this.isContainer	= isContainer;
			this.details		= details;
		}
	}