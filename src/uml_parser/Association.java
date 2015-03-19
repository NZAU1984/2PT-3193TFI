package uml_parser;


/**
 * This is an interface used to link the MVC to an {@link OperationCollector}. It allows to reduce the coupling
 * between the MVC and {@link OperationCollector}.
 *
 * @author Hubert Lemelin
 */
public interface Association
{
	/**
	 * Returns the {@code identifier} (name) of the association.
	 *
	 * @return	The identifier.
	 */
	public String getIdentifier();

	/**
	 * Returns the first {@link Role} in the association.
	 *
	 * @return	The first role.
	 */
	public Role getFirstRole();

	/**
	 * Returns the second {@link Role} in the association.
	 *
	 * @return	The second role.
	 */
	public Role getSecondRole();

	/**
	 * Used to simply display the contents of an {@code Association}.
	 *
	 * @return	The object as a String.
	 */
	@Override
	public String toString();
}
