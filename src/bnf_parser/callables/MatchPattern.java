package bnf_parser.callables;

import java.util.regex.Pattern;

import bnf_parser.SubparserInterface;
import bnf_parser.collectors.StringCollector;

/**
 * This class checks whether or not a regular expression pattern matches from the current position in the file being
 * parsed.
 *
 * @author Hubert Lemelin
 *
 */
public class MatchPattern extends Callable
{
	/**
	 * The pattern to be matched.
	 */
	protected String pattern;

	/**
	 * If true, the pattern will be quoted, which is equivalent to match a string.
	 */
	protected boolean quotePattern	= false;

	/**
	 * If true, the matched string will be collected (by creating a {@link StringCollector}. This is useful to safe
	 * memory when only the presence of the pattern is sufficient to determine that a rule has succeeded. For example,
	 * if we want to match spaces (pattern = \s+), we usually don't care about the string itself, we just want to know
	 * if there are spaces or not.
	 */
	protected boolean collectString	= true;

	/**
	 * Constructor.
	 *
	 * @param pattern		The pattern to be matched. It must not start with {@code ^} since it is always added to make
	 * sure the pattern matches from the current parser position.
	 * @param minOccurences	The minimum number of occurences the pattern must appear. Can be 0.
	 * @param maxOccurences	The maximum number of occurences the pattern must appear. Can be 'infinity'
	 * ({@code Integer.MAX_VALUE}).
	 */
	public MatchPattern(String pattern, int minOccurences, int maxOccurences)
	{
		this(pattern, minOccurences, maxOccurences, true);
	}

	/**
	 * Constructor which allows/disallows the matched string to be collected.
	 *
	 * @param pattern		The pattern to be matched. It must not start with {@code ^} since it is always added to make
	 * sure the pattern matches from the current parser position.
	 * @param minOccurences	The minimum number of occurences the pattern must appear. Can be 0.
	 * @param maxOccurences	The maximum number of occurences the pattern must appear. Can be 'infinity'
	 * ({@code Integer.MAX_VALUE}).
	 * @param collectString	If true, the string will be collected via a new {@link StringCollector}.
	 */
	public MatchPattern(String pattern, int minOccurences, int maxOccurences, boolean collectString)
	{
		super(minOccurences, maxOccurences);

		this.pattern	= pattern;

		collectString(collectString);
	}

	/**
	 * Checks if the pattern matches in the parser at the current file position. The number of occurences of the pattern
	 * was defined in the constructor.
	 *
	 * @return	True if parsing succeeded, false otherwise.
	 */
	@Override
	public boolean parse(SubparserInterface parser)
	{
		resetCollectors();

		/* By default, the collector is null because we don't necessarily want to create a StringCollector
		 * unusefully. */
		StringCollector collector	= null;
		int occurences				= 0;
		StringBuilder sb			= null;

		if(collectString)
		{
			/* Only create a StringBuilder if the string must be collected. */
			sb	= new StringBuilder();
		}

		String patternWorkCopy	= pattern;

		if(quotePattern)
		{
			/* Quote the pattern (it becomes a simple string to be matched) if it must. */
			patternWorkCopy	= Pattern.quote(pattern);
		}

		String val;

		/* Loop as long as the pattern matches and the number of occurences is <= maxOccurences. */
		while((val = parser.matchPattern(patternWorkCopy)) != null)
		{
			if(null != sb)
			{
				sb.append(val);
			}

			++occurences;

			if(occurences == maxOccurences)
			{
				break;
			}
		}

		if(occurences >= minOccurences)
		{
			/*If the number of occurences is at least equal to minOccurences, create the StringCollector if required,
			 * and then return true. */
			if(null != sb)
			{
				collector	= new StringCollector();
				collector.addString(sb.toString());

				addCollector(collector);
			}

			return true;
		}

		/* If number of occurences is inferior to minOccurences, return false. Let's point out that after maxOccurences
		 * has be reached, we do not return false if the pattern still exists after. */
		return false;
	}

	// PROTECTED METHODS

	/**
	 * Sets whether or not the pattern must be quoted.
	 * @param val	If true, the pattern will be quoted.
	 */
	protected void quotePattern(boolean val)
	{
		quotePattern	= val;
	}

	/**
	 * Sets wheter of not the matched string must be collected.
	 * @param val	If true, the matched string will be collector.
	 */
	protected void collectString(boolean val)
	{
		collectString	= val;
	}

}
