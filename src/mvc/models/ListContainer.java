package mvc.models;

public class ListContainer
{
	// PUBLIC CONSTANTS

	/**
	 * List of class names.
	 */
	public static final long CLASS_LIST	= 1L << 0;

	/**
	 * List of all the attribute names of one class.
	 */
	public static final long ATTRIBUTE_LIST	= 1L << 1;

	/**
	 * List of all the operation (method) names of one class.
	 */
	public static final long OPERATION_LIST	= 1L << 2;

	/**
	 * List of all the subclass names of one class.
	 */
	public static final long SUBCLASS_LIST	= 1L << 3;

	/**
	 * List of all the superclasses of on class.
	 */
	public static final long SUPERCLASS_LIST	= 1L << 4;

	/**
	 * List of all the associations/aggregations of one class.
	 */
	public static final long ASSOCIATION_LIST	= 1L << 5;

	/**
	 * List of all the associations/aggregations of one class.
	 */
	public static final long AGGREGATION_LIST	= 1L << 6;





	// PROPERTEC PROPERTIES

	/**
	 * The id of the list.
	 */
	protected long id;

	/**
	 * Array containing all the strings.
	 */
	protected Object[] list;

	// PUBLIC STATIC METHODS

	public static ListContainer newList(long id, Object[] list)
	{
		return new ListContainer(id, list);
	}

	// PROTECTED CONSTRUCTOR

	protected ListContainer(long id, Object[] list)
	{
		this.id		= id;
		this.list	= list;
	}

	// PUBLIC METHODS

	/**
	 * Returns the id of the list.
	 *
	 * @return	The id of the list.
	 */
	public long getId()
	{
		return id;
	}

	/**
	 * Return the array (list) of all strings contained in the container.
	 *
	 * @return	All the strings.
	 */
	public Object[] getList()
	{
		return list;
	}
}
