package bnf_parser.callables;

import bnf_parser.Rule;
import bnf_parser.SubparserInterface;

/**
 * This class checks whether or not a rule matches from the current position in the file being parsed.
 *
 * @author Hubert Lemelin
 */
public class MatchRule extends Callable
{
	/**
	 * The rule that must be matched.
	 */
	protected Rule rule;

	/**
	 * Constructor.
	 * @param rule			The rule to be matched.
	 * @param minOccurences	The minimum number of occurences the pattern must appear. Can be 0.
	 * @param maxOccurences	The maximum number of occurences the pattern must appear. Can be 'infinity'
	 * ({@code Integer.MAX_VALUE}).
	 */
	public MatchRule(Rule rule, int minOccurences, int maxOccurences)
	{
		super(minOccurences, maxOccurences);

		this.rule = rule;
	}

	/**
	 * Checks if the rule matches in the parser at the current file position. The number of occurences of the pattern
	 * was defined in the constructor.
	 *
	 * @return	True if parsing succeeded, false otherwise.
	 */
	@Override
	public boolean parse(SubparserInterface parser)
	{
		resetCollectors();

		int occurences		= 0;

		/* Loop until the maximum number of occurences was reached or the rule failed. */
		while(occurences <= maxOccurences)
		{
			try
			{
				/* If the parsing of the rule succeeds, the parser will return a Collector (it can be NULL). */
				addCollector(parser.evaluateRule(rule));

// TODO remove
//System.out.println(rule.getName() + " success");
			}
			catch (Exception e)
			{
				/* If the parsing fails, let's simply break. We don't necessarily want to return false as we might want
				 * to check if the rule matches a certain number of times, between a minimum and a maximum. Returning
				 * false would break that logic. */
// TODO remove
//System.out.println(rule.getName() + " failed");
				break;
			}

			++occurences;

			if(occurences == maxOccurences)
			{
				break;
			}
		}

		if(occurences >= minOccurences)
		{
			/* If the number of occurences is at least equal to the minimum, return true. */
			return true;
		}

		/* If the number of occurences is wrong, let's reset the collectors so they can be garbage collected and let's
		 * return false. */

		resetCollectors();

		return false;
	}
}
