package uml_parser.collectors;

import uml_parser.Dataitem;
import bnf_parser.collectors.Collector;
import bnf_parser.collectors.StringCollector;

/**
 * This class collects a {@link Dataitem} which correspond to <identifier>:<type> (with/without spaces).
 *
 * @author Hubert Lemelin
 */
public class DataitemCollector extends Collector implements Dataitem
{
	/**
	 * The {@code identifier} part of the {@link Dataitem}.
	 */
	protected String identifier;

	/**
	 * The {@code type} part of the {@link Dataitem}.
	 */
	protected String type;

	public DataitemCollector()
	{
		super();
	}

	/**
	 * Expects two {@link StringCollector}'s, the first one (index = 0) being the {@code identifier}, the second one
	 * (index = 1) being the {@code type}.
	 *
	 * @see Collector#addChild(Collector, int)
	 */
	@Override
	public void addChild(Collector collector, int index)
	{
		if((null != collector) && (collector instanceof StringCollector))
		{
			/* If collector is an instance of StringCollector, let's grab its string. */
			String str	= ((StringCollector) collector).getString();

			/* Switch on the index to set the right property. */
			switch(index)
			{
				case 0:
					identifier	= str;

					break;

				case 1:
					type	= str;

					break;

				default:
					break;
			}
		}
	}

	@Override
	public String getIdentifier()
	{
		return identifier;
	}

	@Override
	public String getType()
	{
		return type;
	}

	@Override
	public String toString()
	{
		return (new StringBuilder()
			.append("Dataitem{")
			.append(getIdentifier())
			.append(" : ")
			.append(getType())
			.append("}"))
			.toString();
	}
}
