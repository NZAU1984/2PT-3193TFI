package mvc.models;

import uml_parser.Association;


class AssociationContainer
{
	public final Association association;

	public final String string;

	public AssociationContainer(Association association, String string)
	{
		this.association	= association;
		this.string			= string;
	}

	@Override
	public String toString()
	{
		return string;
	}
}
