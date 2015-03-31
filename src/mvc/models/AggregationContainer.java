package mvc.models;

import uml_parser.Aggregation;

/**
 * This class is a container for an aggregation, but only after everything was calculated. It is used when caches are
 * being built.
 *
 * @author Hubert Lemelin
 *
 */
class AggregationContainer
{
	/**
	 * The reference to the {@link Aggregation}.
	 */
	public final Aggregation aggregation;

	/**
	 * The part of the aggregatin to be shown to the user in the list.
	 */
	public final String string;

	/**
	 * Constructor.
	 *
	 * @param aggregation	The reference to the {@link Aggregation} instance.
	 * @param string		The string to be shown to the user in the list.
	 */
	public AggregationContainer(Aggregation aggregation, String string)
	{
		this.aggregation	= aggregation;
		this.string			= string;
	}

	/**
	 * Simply returns {@code string} when the list shows all its aggregations.
	 */
	@Override
	public String toString()
	{
		return string;
	}
}
