package uml_parser;

import uml_parser.collectors.AggregationCollector;


/**
 * This is an interface used to link the MVC to an {@link AggregationCollector}. It allows to reduce the coupling
 * between the MVC and {@link AggregationCollector}.
 *
 * @author Hubert Lemelin
 */
public interface Aggregation
{
	/**
	 * Returns the {@link Role} containing the parts.
	 *
	 * @return	The {@link Role} containing the parts.
	 */
	public Role getRole();

	/**
	 * Returns an array of {@link Role} which are the parts of the aggregation.
	 *
	 * @return	The roles being parts of the aggregation.
	 */
	public Role[] getPartRoles();

	/**
	 * Used to simply display the contents of an {@code Aggregation}.
	 *
	 * @return	The object as a String.
	 */
	@Override
	public String toString();
}
