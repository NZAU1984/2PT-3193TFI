package mvc.models;

import java.util.HashMap;

/**
 * Exception class used when a specific error occurs in the model. It uses an ENUM to define many types of errors which
 * allows to use one class instead of many individual exception classes. It also allows to set/get specific
 * attributes.
 *
 * @author Hubert Lemelin
 *
 */
public class ModelException extends Exception
{
	/**
	 * The error types.
	 *
	 * @author Hubert Lemelin
	 *
	 */
	public static enum ERRORS
	{
		DUPLICATE_ASSOCIATION,
		DUPLICATE_ATTRIBUTE,
		DUPLICATE_CLASS,
		DUPLICATE_OPERATION,
		INHERITANCE_CYCLE,
		INVALID_FILE,
		MULTIPLE_INHERITANCE_NOT_ALLOWED,
		PARSING_FAILED,
		UNKNOWN_AGGREGATION_CONTAINER_CLASS,
		UNKNOWN_AGGREGATION_PART_CLASS,
		UNKNOWN_ASSOCIATION_CLASS,
		UNKNOWN_GENERALIZATION_SUBCLASS,
		UNKNOWN_GENERALIZATION_SUPERCLASS,
		;
	}

	/**
	 * The attributes.
	 *
	 * @author Hubert Lemelin
	 *
	 */
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

	/**
	 * Current error type.
	 */
	protected final ERRORS error;

	/**
	 * List of attributes/values.
	 */
	protected HashMap<ATTRIBUTES, String> attributeMap;

	/**
	 * Constructor.
	 *
	 * @param error	The type of the new exception.
	 */
	public ModelException(ERRORS error)
	{
		this(error, null);
	}

	/**
	 * Constructor with message.
	 *
	 * @param error		The type of the new exception.
	 * @param message	The message.
	 */
	public ModelException(ERRORS error, String message)
	{
		super(message);

		this.error	= error;
	}

	/**
	 * Defines an attribute with a value.
	 *
	 * @param attribute	The attribute.
	 * @param value		The value.
	 *
	 * @return	Returns 'this' to allow chaining.
	 */
	public ModelException set(ATTRIBUTES attribute, String value)
	{
		createMap();

		attributeMap.put(attribute, value);

		return this;
	}

	/**
	 * Returns the type of the error.
	 *
	 * @return	The type of the erorr.
	 */
	public ERRORS getError()
	{
		return error;
	}

	/**
	 * Returns the value of specified attribute.
	 *
	 * @param attribute	The attribute.
	 *
	 * @return	The value if attribut is found, {@code null} otherwise.
	 */
	public String get(ATTRIBUTES attribute)
	{
		if(null == attributeMap)
		{
			return null;
		}

		return attributeMap.get(attribute);
	}

	/**
	 * Creates the map if not already created.
	 */
	protected void createMap()
	{
		if(null == attributeMap)
		{
			attributeMap	= new HashMap<ATTRIBUTES, String>();
		}
	}
}
