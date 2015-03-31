package uml_parser;

/**
 * Exception class used when parsing does not have a file to parse.
 *
 * @author Hubert Lemelin
 *
 */
public class NoFileSpecifiedException extends Exception
{
	/**
	 * Constructor.
	 */
	public NoFileSpecifiedException()
	{
		super();
	}

	/**
	 * Constructor with message.
	 *
	 * @param message	The message.
	 */
	public NoFileSpecifiedException(String message)
	{
		super(message);
	}
}
