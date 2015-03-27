package mvc.models;

import uml_parser.Aggregation;

class AggregationContainer
{
	public final Aggregation aggregation;

	public final String string;

	public AggregationContainer(Aggregation aggregation, String string)
	{
		this.aggregation	= aggregation;
		this.string			= string;
	}

	@Override
	public String toString()
	{
		return string;
	}
}
