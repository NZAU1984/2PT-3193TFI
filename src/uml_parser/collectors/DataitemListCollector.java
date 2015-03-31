package uml_parser.collectors;

import java.util.ArrayList;

import uml_parser.Dataitem;
import uml_parser.DataitemList;
import bnf_parser.collectors.Collector;

/**
 * This class collects a {@code DataitemList} which correspond to [<dataitem>{,<dataitem>}] (with/without spaces).
 *
 * @author Hubert Lemelin
 */
public class DataitemListCollector extends Collector implements DataitemList
{
	// PROTECTED PROPERTIES

	/**
	 * List of {@link Dataitem}'s.
	 */
	protected ArrayList<DataitemCollector>	dataitemCollectors;

	/**
	 * Constructor.
	 */
	public DataitemListCollector()
	{
		super();

		dataitemCollectors	= new ArrayList<DataitemCollector>();
	}

	/**
	 * Expects 0+ {@link DataitemCollector}'s and puts each, in order, in a list.
	 *
	 * @see Collector#addChild(Collector, int)
	 */
	@Override
	public void addChild(Collector collector, int index)
	{
		if((null != collector) && (collector instanceof DataitemCollector))
		{
			dataitemCollectors.add((DataitemCollector) collector);
		}
	}

	@Override
	public Dataitem[] getDataitems()
	{
		if(null == dataitemCollectors)
		{
			return new Dataitem[0];
		}

		return dataitemCollectors.toArray(new Dataitem[dataitemCollectors.size()]);
	}

	@Override
	public String toString()
	{
		StringBuilder sb	= new StringBuilder();

		sb.append("DataitemList{");

		if((null != dataitemCollectors) && (0 < dataitemCollectors.size()))
		{
			sb.append(dataitemCollectors.get(0));

			for(int i = 1, iMax = dataitemCollectors.size(); i < iMax; ++i)
			{
				sb.append(", ").append(dataitemCollectors.get(i));
			}
		}

		sb.append("}");

		return sb.toString();
	}

}
