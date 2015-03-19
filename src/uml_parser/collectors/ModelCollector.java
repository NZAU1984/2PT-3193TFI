package uml_parser.collectors;

import java.util.ArrayList;

import uml_parser.Aggregation;
import uml_parser.Association;
import uml_parser.ClassContent;
import uml_parser.Generalization;
import uml_parser.Model;
import bnf_parser.collectors.Collector;
import bnf_parser.collectors.StringCollector;

/**
 * This class collects a mix of {@link ClassContentCollector}, {@link AssociationCollector},
 * {@link GeneralizationCollector} and {@link AggregationCollector} declarations and use them to represent a UML schema.
 *
 * @author Hubert Lemelin
 */
public class ModelCollector extends Collector implements Model
{
	/**
	 * The {@code identifier} part of the {@code model}.
	 */
	protected String identifier;

	/**
	 * Contains all the class declarations.
	 */
	protected ArrayList<ClassContentCollector> classContentCollectors;

	/**
	 * Contains all the association declarations.
	 */
	protected ArrayList<AssociationCollector> associationCollectors;

	/**
	 * Contains all the generalization declarations.
	 */
	protected ArrayList<GeneralizationCollector> generalizationCollectors;

	/**
	 * Contains all the aggregation declarations.
	 */
	protected ArrayList<AggregationCollector> aggregationCollectors;

	public ModelCollector()
	{
		super();

		classContentCollectors		= new ArrayList<ClassContentCollector>();
		associationCollectors		= new ArrayList<AssociationCollector>();
		generalizationCollectors	= new ArrayList<GeneralizationCollector>();
		aggregationCollectors		= new ArrayList<AggregationCollector>();
	}

	/**
	 * Expects one {@link StringCollector} containing the {@code identifier} (name) of the model and any of
	 * {@link ClassContentCollector}, {@link AssociationCollector}, {@link GeneralizationCollector} and
	 * {@link AggregationCollector}.
	 *
	 * @see Collector#addChild(Collector, int)
	 */
	@Override
	public void addChild(Collector collector, int index)
	{
		if(null != collector)
		{
			if(collector instanceof StringCollector)
			{
				identifier	= ((StringCollector) collector).getString();
			}
			else if(collector instanceof ClassContentCollector)
			{
				classContentCollectors.add((ClassContentCollector) collector);
			}
			else if(collector instanceof AssociationCollector)
			{
				associationCollectors.add((AssociationCollector) collector);
			}
			else if(collector instanceof GeneralizationCollector)
			{
				generalizationCollectors.add((GeneralizationCollector) collector);
			}
			else if(collector instanceof AggregationCollector)
			{
				aggregationCollectors.add((AggregationCollector) collector);
			}
		}
	}

	@Override
	public String toString()
	{
		StringBuilder sb	= new StringBuilder();

		sb.append("Model{")
			.append(getIdentifier())
			.append(", classes={");

		if((null != classContentCollectors) && (0 < classContentCollectors.size()))
		{
			sb.append(classContentCollectors.get(0));

			for(int i = 1, iMax = classContentCollectors.size(); i < iMax; ++i)
			{
				sb.append(", ").append(classContentCollectors.get(i));
			}
		}

		sb.append("}").append(", associations={");

		if((null != associationCollectors) && (0 < associationCollectors.size()))
		{
			sb.append(associationCollectors.get(0));

			for(int i = 1, iMax = associationCollectors.size(); i < iMax; ++i)
			{
				sb.append(", ").append(associationCollectors.get(i));
			}
		}

		sb.append("}").append(", generalizations = {");

		if((null != generalizationCollectors) && (0 < generalizationCollectors.size()))
		{
			sb.append(generalizationCollectors.get(0));

			for(int i = 1, iMax = generalizationCollectors.size(); i < iMax; ++i)
			{
				sb.append(", ").append(generalizationCollectors.get(i));
			}
		}

		sb.append("}").append(", aggregations = {");

		if((null != aggregationCollectors) && (0 < aggregationCollectors.size()))
		{
			sb.append(aggregationCollectors.get(0));

			for(int i = 1, iMax = aggregationCollectors.size(); i < iMax; ++i)
			{
				sb.append(", ").append(aggregationCollectors.get(i));
			}
		}

		sb.append("}}");

		return sb.toString();
	}

	@Override
	public String getIdentifier()
	{
		return identifier;
	}

	@Override
	public ClassContent[] getClasses()
	{
		if(null == classContentCollectors)
		{
			return new ClassContent[0];
		}

		return classContentCollectors.toArray(new ClassContent[classContentCollectors.size()]);
	}

	@Override
	public Association[] getAssociations()
	{
		if(null == associationCollectors)
		{
			return new Association[0];
		}

		return associationCollectors.toArray(new Association[associationCollectors.size()]);
	}

	@Override
	public Generalization[] getGeneralizations()
	{
		if(null == generalizationCollectors)
		{
			return new Generalization[0];
		}

		return generalizationCollectors.toArray(new Generalization[generalizationCollectors.size()]);
	}

	@Override
	public Aggregation[] getAggregations()
	{
		if(null == aggregationCollectors)
		{
			return new Aggregation[0];
		}

		return aggregationCollectors.toArray(new Aggregation[aggregationCollectors.size()]);
	}
}
