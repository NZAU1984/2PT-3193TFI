package uml_parser;

import uml_parser.collectors.IdentifierListCollector;

/**
 * This is an interface used to link the MVC to a {@link IdentifierListCollector}. It allows to reduce the coupling
 * between the MVC and {@link IdentifierListCollector}.
 *
 * @author Hubert Lemelin
 */
public interface IdentifierList
{
	/**
	 * Returns an array of all the {@code identifiers} contained in a {@link IdentifierListCollector}.
	 *
	 * @return An array of {@link Dataitem}'s.
	 */
	public String[] getIdentifiers();

	/**
	 * Used to simply display the contents of a {@code IdentifierList}.
	 *
	 * @return	The object as a String.
	 */
	@Override
	public String toString();
}
