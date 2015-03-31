package bnf_parser;

import java.util.ArrayList;
import java.util.NoSuchElementException;

import bnf_parser.callables.Callable;
import bnf_parser.callables.MatchAnyRule;
import bnf_parser.callables.MatchPattern;
import bnf_parser.callables.MatchRule;
import bnf_parser.callables.MatchString;
import bnf_parser.collectors.Collector;
import bnf_parser.collectors.StringCollector;


public class Rule
{
	// PUBLIC STATIC CONSTANTS

	/**
	 * Constant used when a subrule collector does not override the rule collector.
	 */
	public final static int NO_COLLECTOR_OVERRIDE	= -1;

	/**
	 * Constant (almost) equal to infinity.
	 */
	public final static int INFINITY = Integer.MAX_VALUE;

	// PROTECTED PROPERTIES

	/**
	 * The class of the {@link Collector} used by the rule.
	 */
	protected Class<?> collectorClass;

	/**
	 * The list of {@link Callable}'s which are equivalent to subrules.
	 */
	protected ArrayList<CallableContainer> callables;

	/**
	 * The value of the iterator index when iterating through the {@link Callable}'s.
	 */
	protected int iteratorIndex	= -1;

	/**
	 * When the rule used the collector of one of its {@link Callable}'s its index is stored in this property.
	 */
	protected int collectorOverrideIndex = NO_COLLECTOR_OVERRIDE;

	/**
	 * Equals true when the rule must match the end of the file.
	 */
	protected boolean mustMatchEndOfFile	= false;

	// PACKAGE CONSTRUCTOR

	/**
	 * Package constructor.
	 */
	Rule()
	{
		collectorClass	= null;
		callables		= new ArrayList<CallableContainer>();
	}

	/**
	 * Sets the {@link Collector} used by the rule.
	 *
	 * @param collectorClass	The class of the {@link Collector} to be used.
	 *
	 * @return	'this' to allow chaining.
	 *
	 * @throws IncorrectCollectorException	Thrown when the specified class does not extend {@link Collector}.
	 */
	public Rule setCollector(Class<?> collectorClass) throws IncorrectCollectorException
	{
		if(!Collector.class.isAssignableFrom(collectorClass))
		{
			/* Not extending Collector. */

			throw new IncorrectCollectorException();
		}

		this.collectorClass = collectorClass;

		return this;
	}

	/**
	 * Defines a new subrule that must match the specified string a number of times comprised between
	 * {@code minOccurencex} and {@code minOccurencex}. This subrule will have a {@link StringCollector}.
	 *
	 * @param string		The string to be matched.
	 * @param minOccurences	The minimum number of occurences.
	 * @param maxOccurences	The maximum number of occurences.
	 *
	 * @return	'this' to allow chaining.
	 */
	public Rule matchString(String string, int minOccurences, int maxOccurences)
	{
		return matchString(string, minOccurences, maxOccurences, true);
	}

	/**
	 * Defines a new subrule that must match the specified string a number of times comprised between
	 * {@code minOccurencex} and {@code minOccurencex}. This subrule won't have a {@link StringCollector}.
	 *
	 * @param string		The string to be matched.
	 * @param minOccurences	The minimum number of occurences.
	 * @param maxOccurences	The maximum number of occurences.
	 *
	 * @return	'this' to allow chaining.
	 */
	public Rule matchStringWithoutCollecting(String string, int minOccurences, int maxOccurences)
	{
		return matchString(string, minOccurences, maxOccurences, false);
	}

	/**
	 * Defines a new subrule that must match the specified pattern a number of times comprised between
	 * {@code minOccurencex} and {@code minOccurencex}. The pattern is a regular expression. This subrule will have a
	 * {@link StringCollector}.
	 *
	 * @param pattern		The pattern to be matched.
	 * @param minOccurences	The minimum number of occurences.
	 * @param maxOccurences	The maximum number of occurences.
	 *
	 * @return	'this' to allow chaining.
	 */
	public Rule matchPattern(String pattern, int minOccurences, int maxOccurences)
	{
		return matchPattern(pattern, minOccurences, maxOccurences, true);
	}

	/**
	 * Defines a new subrule that must match the specified pattern a number of times comprised between
	 * {@code minOccurencex} and {@code minOccurencex}. The pattern is a regular expression. This subrule won't have a
	 * {@link StringCollector}.
	 *
	 * @param pattern		The pattern to be matched.
	 * @param minOccurences	The minimum number of occurences.
	 * @param maxOccurences	The maximum number of occurences.
	 *
	 * @return	'this' to allow chaining.
	 */
	public Rule matchPatternWithoutCollecting(String pattern, int minOccurences, int maxOccurences)
	{
		return matchPattern(pattern, minOccurences, maxOccurences, false);
	}

	/**
	 * Defines a new subrule that must match the specified rule a number of times comprised between
	 * {@code minOccurencex} and {@code minOccurencex}. This subrule will use the {@link Collector} used by the
	 * specified rule.
	 *
	 * @param rule			The rule to be matched.
	 * @param minOccurences	The minimum number of occurences.
	 * @param maxOccurences	The maximum number of occurences.
	 *
	 * @return	'this' to allow chaining.
	 */
	public Rule matchRule(Rule rule, int minOccurences, int maxOccurences)
	{
		callables.add(new CallableContainer(new MatchRule(rule, minOccurences, maxOccurences)));

		return this;
	}

	/**
	 * Matches any rule contained in {@code rules} a number of times comprised between
	 * {@code minOccurencex} and {@code minOccurencex}. Since many rules may be specified, this subrule does not
	 * automatically use a {@link Collector}. The user by explicitly specify one using {@link #setCollector(Class)}.
	 *
	 * @param minOccurences	The minimum number of occurences.
	 * @param maxOccurences	The maximum number of occurences.
	 * @param rules			An array of {@link Rule}'s in which at least one must match.
	 *
	 * @return	'this' to allow chaining.
	 */
	public Rule matchAnyRule(int minOccurences, int maxOccurences, Rule... rules)
	{
		callables.add(new CallableContainer(new MatchAnyRule(minOccurences, maxOccurences, rules)));

		return this;
	}

	/**
	 * Makes the rule use the {@link Collector} of the latest specified subrule.
	 *
	 * @return	'this' to allow chaining.
	 * @throws NoSubruleDefinedException	Thrown if no subrule was defined when this method was used.
	 */
	public Rule overrideCollector() throws NoSubruleDefinedException
	{
		collectorOverrideIndex	= getPreviousCallableIndex();

		return this;
	}

	/**
	 * Associates an index with the latest subrule's {@link Collector}. This is useful to distinguish different
	 * {@link Collector}'s of the same type.
	 *
	 * @param index	The index to be associated with the latest subrule's {@link Collector}.
	 *
	 * @return 'this' to allow chaining.
	 *
	 * @throws NoSubruleDefinedException	Thrown if no subrule was defined when this method was used.
	 */
	public Rule setIndex(int index) throws NoSubruleDefinedException
	{
		callables.get(getPreviousCallableIndex()).index = index;

		return this;
	}

	/**
	 * Sets wether or not the rule must match the end of the file being parsed.
	 *
	 * @param val	Wether the rule must match the end of file ({@code true}) or not ({@code false}).
	 *
	 * @return	'this' to allow chaining.
	 */
	public Rule mustMatchEndOfFile(boolean val)
	{
		mustMatchEndOfFile	= val;

		return this;
	}

	/**
	 * Forces the rule to match the end of the file being parsed.
	 *
	 * @return	'this' to allow chaining.
	 */
	public Rule mustMatchEndOfFile()
	{
		return mustMatchEndOfFile(true);
	}

	// PACKAGE METHODS

	/**
	 * Creates a new instance of the {@link Collector} associated with the rule.
	 *
	 * @return	The instance of the newly created {@link Collector}.
	 */
	Collector createCollector()
	{
		if(null == collectorClass)
		{
			/* A subrule can have no collector. */

			return null;
		}

		try
		{
			/* Using Java's reflexivity, we call the default constructor of the class. This might throw an exception,
			 * but correctly designed collectors should not! */
			return (Collector) collectorClass.getConstructor().newInstance();
		}
		catch(Exception e)
		{
			/* If an error occurs, which should never occur, we simply return null. */

			return null;
		}
	}

	/**
	 * Resets the iterator to the beginning of the list of {@link Callable}'s. Since a {@link Rule} can be used
	 * many times, the parser must reset the iterator to be able to iterate again.
	 */
	void resetIterator()
	{
		/* Not '0' since next() uses '++iteratorIndex' to simplify code. */
		iteratorIndex	= -1;
	}

	/**
	 * Returns wether or not the iterator has reached the end of the list of {@link Callable}'s.
	 *
	 * @return	{@code true} if the iterator still hasn't reached the end of the list, {@code false} if it is at the
 * 				end.
	 */
	boolean hasNext()
	{
		return (!callables.isEmpty() && (iteratorIndex < (callables.size() - 1)));
	}

	/**
	 * Returns the next {@link Callable} in the list.
	 *
	 * @return	The next {@link Callable} in the list.
	 *
	 * @throws NoSuchElementException	Just like built-in Java iterators, this exception is thrown if this method is
	 * 									called on an empty list or when the iterator has reached the end of the list.
	 */
	Callable next() throws NoSuchElementException
	{
		if(!hasNext())
		{
			throw new NoSuchElementException();
		}

		/* Updates the index at the same time as it returns the next element. */
		return callables.get(++iteratorIndex).callable;
	}

	/**
	 * Returns wether or not the {@link Rule} uses the {@link Collector} of one of its subrules.
	 *
	 * @return	{@code true} if the {@link Rule} does not use the {@link Collector} of one of its subrules,
	 * 	{@code false} if is does.
	 */
	boolean noCollectorOverriding()
	{
		return collectorOverrideIndex == NO_COLLECTOR_OVERRIDE;
	}

	/**
	 * Returns wether or not the {@link Collector} of the current{@link Callable} returned by the iterator is the
	 * {@link Collector} used by the rule.
	 *
	 * @return
	 */
	boolean doOverrideCollector()
	{
		return (iteratorIndex == collectorOverrideIndex);
	}

	/**
	 * Returns the index associated with the subrule's {@link Collector} to identify that collector. If no index was
	 * specified be the used, the value '-1' will be returned.
	 *
	 * @return	The index associated with the subrule's {@link Collector}.
	 */
	int getIndex()
	{
		return callables.get(iteratorIndex).index;
	}

	/**
	 * Returns wether or not the {@link Rule} must match the end of file.
	 *
	 * @return	{@code true} if the {@link Rule} must match the end of file, {@code false} otherwise.
	 */
	boolean doMatchEndOfFile()
	{
		return mustMatchEndOfFile;
	}


	// PROTECTED

	/**
	 * Please refer to {@link Rule#matchString(String, int, int)} and
	 * {@link Rule#matchStringWithoutCollecting(String, int, int)}. This method simply simplifies code and the two
	 * referenced methods simply use a different value for the parameter {@code collectString}.
	 *
	 * @param string		The string to be matched.
	 * @param minOccurences	The minimum number of occurences.
	 * @param maxOccurences	The maximum number of occurences.
	 * @param collectString	Wether or not the matched string must be collected in a {@link StringCollector}.
	 *
	 * @return	'this' to allow chaining.
	 */
	protected Rule matchString(String string, int minOccurences, int maxOccurences, boolean collectString)
	{
		callables.add(new CallableContainer(new MatchString(string, minOccurences, maxOccurences, collectString)));

		return this;
	}

	/**
	 * Please refer to {@link Rule#matchPattern(String, int, int)} and
	 * {@link Rule#matchPatternWithoutCollecting(String, int, int)}. This method simply simplifies code and the two
	 * referenced methods simply use a different value for the parameter {@code collectString}.
	 *
	 * @param pattern		The pattern to be matched.
	 * @param minOccurences	The minimum number of occurences.
	 * @param maxOccurences	The maximum number of occurences.
	 * @param collectString	Wether or not the matched string must be collected in a {@link StringCollector}.
	 *
	 * @return	'this' to allow chaining.
	 */
	protected Rule matchPattern(String pattern, int minOccurences, int maxOccurences, boolean collectString)
	{
		callables.add(new CallableContainer(new MatchPattern(pattern, minOccurences, maxOccurences, collectString)));

		return this;
	}

	/**
	 * Returns the index of the last subrule ({@link Callable}) defined in the {@link Rule}. It is used by
	 * {@link Rule#overrideCollector()} and {@link Rule#setIndex(int)}.
	 *
	 * @return	The index of the last subrule ({@link Callable}) defied.
	 *
	 * @throws NoSubruleDefinedException	Thrown if no subrule was defined when this method is called.
	 */
	protected int getPreviousCallableIndex() throws NoSubruleDefinedException
	{
		if(0 == callables.size())
		{
			/* Thrown to avoid having an invalid index below (0 - 1 = -1). */
			throw new NoSubruleDefinedException();
		}

		return callables.size() - 1;
	}

	/**
	 * Inner class which acts as a container for every {@link Callable}. It allows to specify a custom index for a
	 * specific {@link Callable} which will be used by a {@link Collector} to distinguish different {@link Collector}'s.
	 * @author Hubert Lemelin
	 *
	 */
	protected class CallableContainer
	{
		/**
		 * The instance of the {@link Callable}.
		 */
		protected Callable callable;

		/**
		 * The index associated with the {@link Callable}.
		 */
		protected int index = -1;

		/**
		 * Constructor.
		 *
		 * @param callable	The instance of the {@link Callable}.
		 */
		protected CallableContainer(Callable callable)
		{
			this.callable	= callable;
		}
	}
}
