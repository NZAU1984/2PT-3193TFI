package uml_parser.collectors;

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
public class OperationCollector extends Collector implements Operation
{
	/**
	 * The identifier (name) of the operation.
	 */
	protected String identifier;

	/**
	 * The multiplicity of the operation.
	 */
	protected String type;

	/**
	 * A collector of {@link Dataitem} which represent the attribute of the operation. It can be empty.
	 */
	protected DataitemListCollector dataitemListCollector;

	public OperationCollector()
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
				String val	= ((StringCollector) collector).getString();
				switch(index)
				{
					case 0:
						identifier	= val;

						break;

					case 1:
						type	= val;

						break;

					default:
						break;
				}
			}
			else if(collector instanceof DataitemListCollector)
			{
				dataitemListCollector	= (DataitemListCollector) collector;
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
	public Dataitem[] getAttributes()
	{
		if(null == dataitemListCollector)
		{
			return new Dataitem[0];
		}

		return dataitemListCollector.getDataitems();
	}

	@Override
	public String toString()
	{
		StringBuilder sb	= new StringBuilder();

		sb.append("Operation{")
			.append(getIdentifier())
			.append(" : ")
			.append(getType())
			.append(", {")
			.append((null == dataitemListCollector) ? "" : dataitemListCollector)
			.append("}}");

		return sb.toString();
	}

}
