package uml_parser.collectors;

import uml_parser.Role;
import bnf_parser.collectors.Collector;
import bnf_parser.collectors.StringCollector;

/**
 * This class collects a {@code Dataitem} which correspond to <identifier>:<multiplicity> (with/without spaces).
 *
 * @author Hubert Lemelin
 */
public class RoleCollector extends Collector implements Role
{
	/**
	 * The name of the role.
	 */
	protected String identifier;

	/**
	 * The multiplicity of the role.
	 */
	protected String multiplicity;

	/**
	 * Constructor.
	 */
	public RoleCollector()
	{
		super();
	}

	/**
	 * Expects two {@link StringCollector}'s, the first one (index = 0) being the {@code identifier}, the second one
	 * (index = 1) being the {@code multiplicity}.
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
					multiplicity	= str;

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
	public String getMultiplicity()
	{
		return multiplicity;
	}

	@Override
	public String toString()
	{
		return (new StringBuilder()
			.append("Role{")
			.append(getIdentifier())
			.append(" : ")
			.append(getMultiplicity())
			.append("}"))
			.toString();
	}
}
