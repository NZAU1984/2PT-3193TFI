package uml_parser.collectors;

import uml_parser.Aggregation;
import uml_parser.Role;
import bnf_parser.collectors.Collector;

/**
 * This class collects an {@link Aggregation} which correspond to 'AGGREGATION' 'CONTAINER' <role> 'PARTS' <roleList>;
 * (with/without spaces).
 *
 * @author Hubert Lemelin
 */
public class AggregationCollector extends Collector implements Aggregation
{
	/**
	 * The {@link Role} (class) containing the parts.
	 */
	protected Role roleCollector;

	/**
	 * The {@link RoleListCollector} containing the parts of the aggregation.
	 */
	protected RoleListCollector roleListCollector;

	//protected RoleListC

	public AggregationCollector()
	{
		super();
	}

	/**
	 * Expects one {@link RoleCollector} being the "owner" of the parts, and one {@link RoleListCollector} being the
	 * parts of the aggregation.
	 *
	 * @see Collector#addChild(Collector, int)
	 */
	@Override
	public void addChild(Collector collector, int index)
	{
		if(null != collector)
		{
			if(collector instanceof RoleCollector)
			{
				roleCollector	= (RoleCollector) collector;
			}
			else if(collector instanceof RoleListCollector)
			{
				roleListCollector	= (RoleListCollector) collector;
			}
		}
	}

	@Override
	public Role getRole()
	{
		return roleCollector;
	}

	@Override
	public Role[] getPartRoles()
	{
		if(null == roleListCollector)
		{
			return new Role[0];
		}

		return roleListCollector.getRoles();
	}

	@Override
	public String toString()
	{
		StringBuilder sb	= new StringBuilder();

		sb.append("Aggregation{")
			.append(getRole())
			.append(" <>--- ")
			.append(roleListCollector)
			.append("}");

		return sb.toString();
	}
}
