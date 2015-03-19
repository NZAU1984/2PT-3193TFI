package uml_parser;


/**
 * This is an interface used to link the MVC to an {@link OperationCollector}. It allows to reduce the coupling
 * between the MVC and {@link OperationCollector}.
 *
 * @author Hubert Lemelin
 */
public interface Operation
{
	/**
	 * Returns the {@code identifier} part of the {@code Dataitem}.
	 *
	 * @return	The identifier.
	 */
	public String getIdentifier();

	/**
	 * Returns the {@code multiplicity} part of the {@code Dataitem}.
	 *
	 * @return	The multiplicity.
	 */
	public String getType();

	/**
	 * Returns an array of all the operation attributes (as {@link Dataitem}.
	 * @return	An array of the operation attributes.
	 */
	public Dataitem[] getAttributes();

	/**
	 * Used to simply display the contents of a {@code Dataitem}.
	 * @return	The object as a String.
	 */
	@Override
	public String toString();
}
