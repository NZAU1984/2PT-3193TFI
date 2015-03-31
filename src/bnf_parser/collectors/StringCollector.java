package bnf_parser.collectors;

import bnf_parser.BnfParser;

/**
 * This class is used to collect a string that matched either a string or a pattern in
 * {@link BnfParser#evaluateRule(bnf_parser.Rule)}. If another {@code StringCollector} is passed to the
 * {@link #addChild(Collector, int)} method, its string is appended to the current string.
 * @author Hubert Lemelin
 *
 */
public class StringCollector extends Collector
{
	// PROTECTED PROPERTIES

	/**
	 * The StringBuilder used to append a string to the current string.
	 */
	protected StringBuilder sb;

	// PUBLIC CONSTRUCTOR

	/**
	 * Initializes the StringBuilder.
	 */
	public StringCollector()
	{
		sb				= new StringBuilder();
	}

	// PUBLIC METHODS

	/**
	 * If {@code collector} is an instance of {@code StringCollector} (this class) it is appended to the StringBuilder.
	 * Otherwise, it is simply ignored.
	 */
	@Override
	public void addChild(Collector collector, int index)
	{
		if(collector instanceof StringCollector)
		{
			sb.append(((StringCollector) collector).getString());
		}
	}

	/**
	 * Appends {@code string} to the StringBuilder.
	 * @param string	The string to be appended.
	 */
	public void addString(String string)
	{
		sb.append(string);
	}

	/**
	 * Returns the string from the StringBuilder.
	 * @return	The string from the StringBuilder.
	 */
	public String getString()
	{
		return sb.toString();
	}
}
