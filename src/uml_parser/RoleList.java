package uml_parser;

import uml_parser.collectors.RoleListCollector;

/**
 * This is an interface used to link the MVC to a {@link RoleListCollector}. It allows to reduce the coupling
 * between the MVC and {@link RoleListCollector}.
 *
 * @author Hubert Lemelin
 */
public interface RoleList
{
	/**
	 * Returns an array of all the {@link Role}'s contained in a {@link RoleListCollector}.
	 *
	 * @return An array of {@link Role}'s.
	 */
	public Role[] getRoles();

	/**
	 * Used to simply display the contents of a {@code DataitemList}.
	 *
	 * @return	The object as a String.
	 */
	@Override
	public String toString();
}
