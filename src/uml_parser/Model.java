package uml_parser;

import uml_parser.collectors.ModelCollector;


/**
 * This is an interface used to link the MVC to an {@link ModelCollector}. It allows to reduce the coupling
 * between the MVC and {@link ModelCollector}.
 *
 * @author Hubert Lemelin
 */
public interface Model
{
	/**
	 * Returns the {@code identifier} (name) of the model.
	 *
	 * @return	The identifier.
	 */
	public String getIdentifier();

	/**
	 * Returns an array containing the class declarations of the model.
	 *
	 * @return	The class declarations.
	 */
	public ClassContent[] getClasses();

	/**
	 * Returns an array containing the association declarations of the model.
	 *
	 * @return	The association declarations.
	 */
	public Association[] getAssociations();

	/**
	 * Returns an array containing the generalization declarations of the model.
	 *
	 * @return	The generalization declarations of the model.
	 */
	public Generalization[] getGeneralizations();

	/**
	 * Returns an array containing the aggregations declarations of the model.
	 *
	 * @return	The aggregations declarations of the model.
	 */
	public Aggregation[] getAggregations();

	/**
	 * Used to simply display the contents of an {@code Model}.
	 *
	 * @return	The object as a String.
	 */
	@Override
	public String toString();
}
