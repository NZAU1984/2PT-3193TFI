package bnf_parser;

/**
 * Defines an exception thrown when the evaluation of a rule failed.
 *
 * @author Hubert Lemelin
 *
 */
public class ParsingFailedException extends Exception
{
	/**
	 * Constructor without message.
	 */
	public ParsingFailedException()
	{
		super();
	}

	/**
	 * Constructor with message.
	 *
	 * @param message	The message.
	 */
	public ParsingFailedException(String message)
	{
		super(message);
	}
}
