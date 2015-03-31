package bnf_parser.callables;

/**
 * Exception class used when a {@link Callable} contains more than one {@link Collector}. It is thrown when a rule
 * wants to override one of its subrules' collector which contains more than one collector.
 *
 * @author Hubert Lemelin
 *
 */
public class CallableContainsMoreThanOneCollectorException extends Exception
{
	/**
	 * Constructor.
	 */
	public CallableContainsMoreThanOneCollectorException()
	{
		super();
	}

	/**
	 * Constructor with message.
	 * @param message	The message.
	 */
	public CallableContainsMoreThanOneCollectorException(String message)
	{
		super(message);
	}
}
