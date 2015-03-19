package uml_parser;


/**
 * This is an interface used to link the MVC to an {@link OperationCollector}. It allows to reduce the coupling
 * between the MVC and {@link OperationCollector}.
 *
 * @author Hubert Lemelin
 */
public interface ClassContent
{
	/**
	 * Returns the {@code identifier} (name) of the class.
	 *
	 * @return	The identifier.
	 */
	public String getIdentifier();

	/**
	 * Returns an array of all attributes (as {@link Dataitem} of the class.
	 *
	 * @return	An array of the attributes of the class.
	 */
	public Dataitem[] getAttributes();

	/**
	 * Returns an array of all the operations (as {@link Operation}) of the class.
	 *
	 * @return	An array of the operations of the class.
	 */
	public Operation[] getOperations();

	/**
	 * Used to simply display the contents of a {@code ClassContent}.
	 *
	 * @return	The object as a String.
	 */
	@Override
	public String toString();
}
