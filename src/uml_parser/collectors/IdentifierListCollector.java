package uml_parser.collectors;

import java.util.ArrayList;

import uml_parser.IdentifierList;
import bnf_parser.collectors.Collector;
import bnf_parser.collectors.StringCollector;

/**
 * This class collects a list of {@code identifiers} which correspond to [<identifier>{,<identifier>}]
 * (with/without spaces).
 *
 * @author Hubert Lemelin
 */
public class IdentifierListCollector extends Collector implements IdentifierList
{
	/**
	 * List of the identifiers.
	 */
	protected ArrayList<String>	identifiers;

	/**
	 * Constructor.
	 */
	public IdentifierListCollector()
	{
		super();

		identifiers	= new ArrayList<String>();
	}

	/**
	 * Expects 0+ {@link StringCollector}'s, each being an {@code identifier}.
	 *
	 * @see Collector#addChild(Collector, int)
	 */
	@Override
	public void addChild(Collector collector, int index)
	{
		if((null != collector) && (collector instanceof StringCollector))
		{
			/* If collector is an instance of StringCollector, let's grab its string. */
			identifiers.add(((StringCollector) collector).getString());
		}
	}

	@Override
	public String[] getIdentifiers()
	{
		if(null == identifiers)
		{
			return new String[0];
		}

		return identifiers.toArray(new String[identifiers.size()]);
	}

	@Override
	public String toString()
	{
		StringBuilder sb	= new StringBuilder();

		sb.append("DataitemList{");

		if((null != identifiers) && (0 < identifiers.size()))
		{
			sb.append(identifiers.get(0));

			for(int i = 1, iMax = identifiers.size(); i < iMax; ++i)
			{
				sb.append(", ").append(identifiers.get(i));
			}
		}

		sb.append("}");

		return sb.toString();
	}
}
