package uml_parser.collectors;

import uml_parser.Association;
import uml_parser.Dataitem;
import uml_parser.Role;
import bnf_parser.collectors.Collector;
import bnf_parser.collectors.StringCollector;

/**
 * This class collects an {@link Association} which correspond to 'RELATION'<identifier>'ROLES'<role>,<role>;
 * (with/without spaces).
 *
 * @author Hubert Lemelin
 */
public class AssociationCollector extends Collector implements Association
{
	/**
	 * The identifier (name) of the association.
	 */
	protected String identifier;

	/**
	 * The first role.
	 */
	protected RoleCollector firstRoleCollector;

	/**
	 * The second role.
	 */
	protected RoleCollector secondRoleCollector;

	/**
	 * The second role.
	 */

	public AssociationCollector()
	{
		super();
	}

	/**
	 * Expects two {@link StringCollector}'s, the first one (index = 0) being the {@code identifier}, the second one
	 * (index = 1) being the {@code multiplicity}, and also expects one {@link DataitemListCollector} containing 0+
	 * {@link Dataitem}.
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
			else if(collector instanceof RoleCollector)
			{
				RoleCollector roleCollector	= (RoleCollector) collector;

				switch(index)
				{
					case 0:
						firstRoleCollector	= roleCollector;

						break;

					case 1:
						secondRoleCollector	= roleCollector;

						break;

					default:

						break;
				}
			}
		}
	}

	@Override
	public String getIdentifier()
	{
		return identifier;
	}

	@Override
	public Role getFirstRole()
	{
		return firstRoleCollector;
	}

	@Override
	public Role getSecondRole()
	{
		return secondRoleCollector;
	}


	@Override
	public String toString()
	{
		StringBuilder sb	= new StringBuilder();

		sb.append("Association{")
			.append(getIdentifier())
			.append(" : ")
			.append(firstRoleCollector)
			.append(" <-> ")
			.append(secondRoleCollector)
			.append("}");

		return sb.toString();
	}
}
