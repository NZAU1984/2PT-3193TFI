package uml_parser.collectors;

import uml_parser.Generalization;
import bnf_parser.collectors.Collector;
import bnf_parser.collectors.StringCollector;

/**
 * This class collects a {@link Generalization} which correspond to
 * 'GENERALIZATION'<identifier>'SUBCLASSES'<identifierList>; (with/without spaces).
 *
 * @author Hubert Lemelin
 */
public class GeneralizationCollector extends Collector implements Generalization
{
	/**
	 * The identifier (name) of the superclass of the generalization.
	 */
	protected String identifier;

	/**
	 * The list of the subclasses of the generalization.
	 */
	protected IdentifierListCollector identifierListCollector;

	/**
	 * Constructor.
	 */
	public GeneralizationCollector()
	{
		super();
	}

	/**
	 * Expects one {@link StringCollector} being the {@code identifier}, and one {@link IdentifierListCollector} which
	 * contains the names (identifiers) of the subclasses.
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
			else if(collector instanceof IdentifierListCollector)
			{
				identifierListCollector	= (IdentifierListCollector) collector;
			}
		}
	}

	@Override
	public String getSuperclassName()
	{
		return identifier;
	}

	@Override
	public String[] getSubclassNames()
	{
		if(null == identifierListCollector)
		{
			return new String[0];
		}

		return identifierListCollector.getIdentifiers();
	}

	@Override
	public String toString()
	{
		StringBuilder sb	= new StringBuilder();

		sb.append("Generalization{")
			.append(getSuperclassName())
			.append(" : {")
			.append(identifierListCollector)
			.append("}");

		return sb.toString();
	}
}
