package mvc;
/*
 * Section:
 * 	- Class
 *  - Attribute
 *  - Method
 *  - Parent class
 *  - Child class
 *  - Inheritance
 *  - Association
 *  - Aggregation
 *
 * Type:
 *	- DUPLICATE
 *  - UNKNOWN (class)
 *  - CYCLE
 *  - (UNKNOWN_AGGREGATION_CONTAINER_CLASS)
 *  - (UNKNOWN_AGGREGATION_PART_CLASS)
 *
 * Details:
 * 	- Class name
 *  - Attribute name
 *  - Method name/return type/signature
 */
public class ModelException extends Exception
{
	public static enum SECTION
	{
		CLASS,
		ATTRIBUTE,
		METHOD,
		SUPERCLASS,
		SUBCLASS,
		INHERITANCE,
		ASSOCIATION,
		AGGREGATION;
	}

	public static enum TYPE
	{
		DUPLICATE,
		UNKNOWN,
		UNKNOWN_CONTAINER,
		UNKWNON_PART,
		CYCLE
	}

	protected final SECTION section;
	protected final TYPE type;
	protected final String details;

	public ModelException(SECTION section, TYPE type, String details)
	{
		this(section, type, details, null);
	}

	public ModelException(SECTION section, TYPE type, String details, String message)
	{
		super(message);

		this.section	= section;
		this.type		= type;
		this.details	= details;
	}

	public SECTION getSection()
	{
		return section;
	}

	public TYPE getType()
	{
		return type;
	}

	public String getDetails()
	{
		return details;
	}
}
