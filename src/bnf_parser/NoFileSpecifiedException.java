package bnf_parser;

/**
 * Defines an exception thrown when parsing occurs without a file being specified.
 *
 * @author Hubert Lemelin
 *
 */
public class NoFileSpecifiedException extends Exception
{
	/**
	 * Cosntructor without message.
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
