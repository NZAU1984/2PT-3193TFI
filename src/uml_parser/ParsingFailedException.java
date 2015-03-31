package uml_parser;

/**
 * Exception class used when parsing fails.
 * @author Hubert Lemelin
 *
 */
public class ParsingFailedException extends Exception
{
	/**
	 * Constructor.
	 */
	public ParsingFailedException()
	{
		super();
	}

	/**
	 * Constructor with message.
	 * @param message	The message.
	 */
	public ParsingFailedException(String message)
	{
		super(message);
	}
}
