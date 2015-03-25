package uml_parser;

import uml_parser.collectors.GeneralizationCollector;


/**
 * This is an interface used to link the MVC to an {@link GeneralizationCollector}. It allows to reduce the coupling
 * between the MVC and {@link GeneralizationCollector}.
 *
 * @author Hubert Lemelin
 */
public interface Generalization
{
	/**
	 * Returns the {@code identifier} (name) of the association.
	 *
	 * @return	The identifier.
	 */
	public String getSuperclassName();

	public String[] getSubclassNames();

	/**
	 * Used to simply display the contents of an {@code Association}.
	 *
	 * @return	The object as a String.
	 */
	@Override
	public String toString();
}
