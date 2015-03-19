package bnf_parser.callables;

import bnf_parser.collectors.StringCollector;


/**
 * This class checks whether or not a string matches from the current position in the file being parsed. It simply
 * extends {@link MatchPattern} by 'telling' it to quote the pattern (the string given to this class).
 *
 * @author Hubert Lemelin
 *
 */
public class MatchString extends MatchPattern
{
	/**
	 * Constructor.
	 *
	 * @param string		The string to be matched.
	 * @param minOccurences	The minimum number of occurences the pattern must appear. Can be 0.
	 * @param maxOccurences	The maximum number of occurences the pattern must appear. Can be 'infinity'
	 * ({@code Integer.MAX_VALUE}).
	 */
	public MatchString(String string, int minOccurences, int maxOccurences)
	{
		this(string, minOccurences, maxOccurences, true);
	}

	/**
	 * Constructor which allows/disallows the matched string to be collected.
	 *
	 * @param string		The string to be matched.
	 * @param minOccurences	The minimum number of occurences the pattern must appear. Can be 0.
	 * @param maxOccurences	The maximum number of occurences the pattern must appear. Can be 'infinity'
	 * ({@code Integer.MAX_VALUE}).
	 * @param collectString	If true, the string will be collected via a new {@link StringCollector}.
	 */
	public MatchString(String string, int minOccurences, int maxOccurences, boolean collectString)
	{
		super(string, minOccurences, maxOccurences, collectString);

		quotePattern(true);
	}
}
