package mvc.models;

import uml_parser.Association;


/**
 * This class is a container for an association, but only after everything was calculated. It is used when caches are
 * being built.
 *
 * @author Hubert Lemelin
 *
 */
class AssociationContainer
{
	/**
	 * The reference to the {@link Association} instance.
	 */
	public final Association association;

	/**
	 * The part of the association to be shown to the user in the list.
	 */
	public final String string;

	/**
	 * Constructor.
	 *
	 * @param association	The reference to the {@link Association} instance.
	 * @param string		The part of the association to be shown to the user in the list.
	 */
	public AssociationContainer(Association association, String string)
	{
		this.association	= association;
		this.string			= string;
	}

	/**
	 * Simply returns {@code string} when the list shows all its aggregations.
	 */
	@Override
	public String toString()
	{
		return string;
	}
}
