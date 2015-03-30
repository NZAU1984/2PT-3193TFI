package bnf_parser;

/**
 * Defines an exception thrown when properties of a subrule are set when there are no subrules defined yet.
 * @author Hubert Lemelin
 *
 */
public class NoSubruleDefinedException extends Exception
{
	/**
	 * Cosntructor without message.
	 */
	public NoSubruleDefinedException()
	{
		super();
	}

	/**
	 * Constructor with message.
	 *
	 * @param message	The message.
	 */
	public NoSubruleDefinedException(String message)
	{
		super(message);
	}
}
