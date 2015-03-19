package uml_parser;

import uml_parser.collectors.DataitemCollector;

/**
 * This is an interface used to link the MVC to a {@link DataitemCollector}. It allows to reduce the coupling between
 * the MVC and {@link DataitemCollector}.
 *
 * @author Hubert Lemelin
 */
public interface Dataitem
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
	 * Used to simply display the contents of a {@code Dataitem}.
	 * @return	The object as a String.
	 */
	@Override
	public String toString();
}
