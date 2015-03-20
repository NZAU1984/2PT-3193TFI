package mvc;

import java.util.HashMap;

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
	public static enum ERRORS
	{
		DUPLICATE_ASSOCIATION,
		DUPLICATE_ATTRIBUTE,
		DUPLICATE_CLASS,
		DUPLICATE_OPERATION,
		INHERITANCE_CYCLE,
		UNKNOWN_AGGREGATION_CONTAINER_CLASS,
		UNKNOWN_AGGREGATION_PART_CLASS,
		UNKNOWN_ASSOCIATION_CLASS,
		UNKNOWN_GENERALIZATION_SUBCLASS,
		UNKNOWN_GENERALIZATION_SUPERCLASS,
		;
	}

	public static enum ATTRIBUTES
	{
		ASSOCIATION,
		ATTRIBUTE,
		CLASS,
		CONTAINER_CLASS,
		FIRST_CLASS,
		SECOND_CLASS,
		OPERATION_NAME,
		OPERATION_SIGNATURE,
		OPERATION_TYPE,
		PART_CLASS,
		SUBCLASS,
		SUPERCLASS,
		;
	}

	protected final ERRORS error;

	protected HashMap<ATTRIBUTES, String> attributeMap;

	public ModelException(ERRORS error)
	{
		this(error, null);
	}

	public ModelException(ERRORS error, String message)
	{
		super(message);

		this.error	= error;
	}

	public ModelException set(ATTRIBUTES attribute, String value)
	{
		createMap();

		attributeMap.put(attribute, value);

		return this;
	}

	public ERRORS getError()
	{
		return error;
	}

	public String get(ATTRIBUTES attribute)
	{
		if(null == attributeMap)
		{
			return null;
		}

		return attributeMap.get(attribute);
	}

	protected void createMap()
	{
		if(null == attributeMap)
		{
			attributeMap	= new HashMap<ATTRIBUTES, String>();
		}
	}
}
