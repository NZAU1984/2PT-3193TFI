package uml_parser;

import uml_parser.collectors.RoleCollector;

/**
 * This is an interface used to link the MVC to a {@link RoleCollector}. It allows to reduce the coupling between
 * the MVC and {@link RoleCollector}.
 *
 * @author Hubert Lemelin
 */
public interface Role
{
	/**
	 * Returns the {@code identifier} part of the {@code Role}.
	 *
	 * @return	The identifier.
	 */
	public String getIdentifier();

	/**
	 * Returns the {@code multiplicity} part of the {@code Role}.
	 *
	 * @return	The multiplicity.
	 */
	public String getMultiplicity();

	/**
	 * Used to simply display the contents of a {@code Role}.
	 *
	 * @return	The object as a String.
	 */
	@Override
	public String toString();
}
