package bnf_parser;

import bnf_parser.callables.CallableContainsMoreThanOneCollectorException;
import bnf_parser.collectors.Collector;

public interface SubparserInterface
{
	public Collector evaluateRule(Rule rule)
			throws ParsingFailedException, CallableContainsMoreThanOneCollectorException,
			NoFileSpecifiedException;

	public String matchPattern(String pattern);
}
