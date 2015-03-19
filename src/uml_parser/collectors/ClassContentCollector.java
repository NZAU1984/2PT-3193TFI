package uml_parser.collectors;

import uml_parser.ClassContent;
import uml_parser.Dataitem;
import uml_parser.Operation;
import bnf_parser.collectors.Collector;
import bnf_parser.collectors.StringCollector;

/**
 * This class collects an {@link Operation} which correspond to <identifier>(<dataitemList>):<multiplicity>
 * (with/without spaces).
 *
 * @author Hubert Lemelin
 */
public class ClassContentCollector extends Collector implements ClassContent
{
	/**
	 * The identifier (name) of the operation.
	 */
	protected String identifier;

	/**
	 * A collector of {@link Dataitem}'s which represent the attribute of the class. It can be empty.
	 */
	protected DataitemListCollector dataitemListCollector;

	/**
	 * A collector of {@link Operation}'s which are the operations (methods) of the class. It can be empty.
	 */
	protected OperationListCollector operationListCollector;

	public ClassContentCollector()
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
			else if(collector instanceof DataitemListCollector)
			{
				dataitemListCollector	= (DataitemListCollector) collector;
			}
			else if(collector instanceof OperationListCollector)
			{
				operationListCollector	= (OperationListCollector) collector;
			}
		}
	}

	@Override
	public String getIdentifier()
	{
		return identifier;
	}

	@Override
	public Dataitem[] getAttributes()
	{
		if(null == dataitemListCollector)
		{
			return new Dataitem[0];
		}

		return dataitemListCollector.getDataitems();
	}

	@Override
	public Operation[] getOperations()
	{
		if(null == operationListCollector)
		{
			return new Operation[0];
		}

		return operationListCollector.getOperations();
	}

	@Override
	public String toString()
	{
		StringBuilder sb	= new StringBuilder();

		sb.append("Class{")
			.append(getIdentifier())
			.append(", {")
			.append((null == dataitemListCollector) ? "" : dataitemListCollector)
			.append("}, {")
			.append((null == operationListCollector) ? "" : operationListCollector)
			.append("}}");

		return sb.toString();
	}

}
