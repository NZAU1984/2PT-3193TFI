package uml_parser;

import uml_parser.collectors.DataitemListCollector;

/**
 * This is an interface used to link the MVC to a {@link DataitemListCollector}. It allows to reduce the coupling
 * between the MVC and {@link DataitemListCollector}.
 *
 * @author Hubert Lemelin
 */
public interface DataitemList
{
	/**
	 * Returns an array of all the {@link Dataitem}'s contained in a {@link DataitemListCollector}.
	 *
	 * @return An array of {@link Dataitem}'s.
	 */
	public Dataitem[] getDataitems();

	/**
	 * Used to simply display the contents of a {@code DataitemList}.
	 *
	 * @return	The object as a String.
	 */
	@Override
	public String toString();
}
