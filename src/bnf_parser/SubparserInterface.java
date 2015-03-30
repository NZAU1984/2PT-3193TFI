package bnf_parser;

import bnf_parser.callables.Callable;
import bnf_parser.callables.CallableContainsMoreThanOneCollectorException;
import bnf_parser.collectors.Collector;

/**
 * Simple interface used between {@link BnfParser} and classes extending {@link Callable}. It is used to diminish
 * coupling between packets/classes.
 *
 * @author Hubert Lemelin
 *
 */
public interface SubparserInterface
{
	/**
	 * Tries to match the specified rule.
	 *
	 * @param rule	The rule to be evaluated.
	 *
	 * @return	Return an instance of the collector specified in the rule. It can be null so one must always check
	 * if it is null before calling any methods on it.
	 *
	 * @throws ParsingFailedException							Thrown when the rule did not match.
	 * @throws CallableContainsMoreThanOneCollectorException	Thrown when a callable contains more than one collector.
	 * @throws NoFileSpecifiedException							Thrown when the parsing happens before any file was
	 * 															specified.
	 */
	public Collector evaluateRule(Rule rule)
			throws ParsingFailedException, CallableContainsMoreThanOneCollectorException,
			NoFileSpecifiedException;

	/**
	 * The class implements this interface must provide this method. A pattern is the basic element used by the parser
	 * when reading a file to be parser.
	 *
	 * @param pattern	The pattern to be matched
	 * @return			The string that was matched by the pattern.
	 */
	public String matchPattern(String pattern);
}
