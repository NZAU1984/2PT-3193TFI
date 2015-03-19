package bnf_parser.callables;

import bnf_parser.Rule;
import bnf_parser.SubparserInterface;

/**
 * This class checks whether or not any rule among a given set of rules matches from the current position in the file
 * being parsed. If the maximum number of occurences is greater than 1, then this class will attempt to match as
 * many rules from the subset as possible.
 *
 * @author Hubert Lemelin
 */
public class MatchAnyRule extends Callable
{
	/**
	 * The set of rules form which at least one rule must match.
	 */
	protected Rule[] rules;

	/**
	 * Constructor.
	 *
	 * @param minOccurences	The minimum number of occurences the pattern must appear. Can be 0.
	 * @param maxOccurences	The maximum number of occurences the pattern must appear. Can be 'infinity'
	 * ({@code Integer.MAX_VALUE}).
	 * @param rules			The set of rules to be matched.
	 */
	public MatchAnyRule(int minOccurences, int maxOccurences, Rule... rules)
	{
		super(minOccurences, maxOccurences);

		this.rules	= rules;
	}

	/**
	 * Checks if any of the given rule matches in the parser at the current file position.
	 *
	 * @return	True if parsing succeeded, false otherwise.
	 */
	@Override
	public boolean parse(SubparserInterface parser)
	{
		resetCollectors();

		int occurences	= 0;

		/* Loop until the number of occurences reaches the maximum value. */
		while(occurences <= maxOccurences)
		{
			/* This is used to know if, in the inner loop below, at least one rule matched. */
			boolean matched	= false;

			/* Looping through the set of rules. */
			for(Rule rule : rules)
			{
				try
				{
					/* If parsing is successful, it will return a Collector (or null). */
					addCollector(parser.evaluateRule(rule));

					++occurences;

					matched	= true;
				}
				catch (Exception e)
				{
					/* The current rule did not match, let's try the next one (if this is not the last one in the
					 * set). */
					continue;
				}
			}

			if(!matched)
			{
				/* if no rule in the inner loop matched, let's break because none will match if we loop again. */
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
