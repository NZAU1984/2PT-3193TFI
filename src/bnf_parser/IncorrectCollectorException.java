package bnf_parser;

import bnf_parser.collectors.Collector;

/**
 * Defines an exception thrown when the user tries to define the {@link Collector} of a {@rule Rule} that is invalid.
 * Most likely, this will happen if the specified class does not extend {@link Collector}.
 *
 * @author Hubert Lemelin
 *
 */
public class IncorrectCollectorException extends Exception
{
	/**
	 * Default constructor.
	 */
	public IncorrectCollectorException()
	{
		super();
	}

	/**
	 * Constructor with message.
	 *
	 * @param message	The message.
	 */
	public IncorrectCollectorException(String message)
	{
		super(message);
	}
}
