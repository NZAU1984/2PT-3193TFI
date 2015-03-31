package bnf_parser.callables;

import java.util.ArrayList;

import bnf_parser.BnfParser;
import bnf_parser.SubparserInterface;
import bnf_parser.collectors.Collector;

/**
 * This abstract class represents the heart of the parsing process. Every rule contains one or more (subclasses of)
 * {@code Callable} in an ordered list. The {@link BnfParser} calls the {@link #parse(SubparserInterface)} method of
 * every {@code Callable}. If all calls return true, then the rule matched successfully. Otherwise, if any call returns
 * false, then the parsing failed (the rule did not match). Every callable has a minimum number of occurences (can be
 * 0) and a maximum number of occurences (can be up to {@code Integer.MAX_VALUE} which is considered 'infinity').
 *
 * @author Hubert Lemelin
 *
 */
public abstract class Callable
{
	// PROTECTED PROPERTIES

	/**
	 * The minimum number of occurences this {@code Callable} must match. Can be 0.
	 */
	protected int minOccurences;

	/**
	 * The maximum number of occurences this {@code Callable} must match. Can be 'infinity' which corresponds to
	 * {@code Integer.MAX_VALUE}.
	 */
	protected int maxOccurences;

	// PRIVATE PROPERTIES

	/**
	 * Contains all the {@code Collector}'s returned by the parser. Usually, only one {@link Collector} is returned,
	 * but sometimes, especially in {@link MatchRule}, there can be more than one {@code Callable}'s returned.
	 */
	private ArrayList<Collector> collectors;

	// PROTECTED CONSTRUCTOR

	/**
	 * Initializes properties.
	 *
	 * @param minOccurences	The minimum number of occurences that must match.
	 * @param maxOccurences	The maximum number of occurences that must match.
	 */
	protected Callable(int minOccurences, int maxOccurences)
	{
		/* For the sake of simplification, let's simply take the min/max to make sure min is not greater than max. */
		this.minOccurences	= Math.min(minOccurences, maxOccurences);
		this.maxOccurences	= Math.max(minOccurences, maxOccurences);

		resetCollectors();
	}

	// PUBLIC METHODS

	/**
	 * Returns the only {@link Collector} contained in {@code collectors}.
	 * @return The only {@code Collector} contained in {@code collectors} if there is exactly one. If none exists, it
	 * returns NULL.
	 * @throws CallableContainsMoreThanOneCollectorException Thrown if more than one {@code Collector} exists.
	 */
	public Collector getCollector() throws CallableContainsMoreThanOneCollectorException
	{
		/* More than one collectors, let's throw an exception. */
		if(1 < collectors.size())
		{
			throw new CallableContainsMoreThanOneCollectorException();
		}

		/* If none exists, simply return null because sometimes, a Callable can have a 'NULL' collector (for example,
		 * MatchString with no collecting (to same some memory). */
		if(0 == collectors.size())
		{
			return null;
		}

		/* Returns the only collector. */
		return collectors.get(0);
	}

	/**
	 * Returns all the {@code Collector}'s in an array.
	 * @return All the {@code Collector}'s as an array.
	 */
	public Collector[] getCollectors()
	{
		return collectors.toArray(new Collector[collectors.size()]);
	}

	// PUBLIC ABSTRACT METHODS

	/**
	 * Abstract method that must be defined by subclasses which is the heart of the parsing process. This is the method
	 * that decided whether or not the current rule/subrule succeeds or fails.
	 *
	 * @param parser The current instance of a parser implementign {@link SubparserInterface}.
	 * @return	True if parsing is successful, false otherwise.
	 */
	public abstract boolean parse(SubparserInterface parser);

	// PROTECTED METHODS

	/**
	 * (re) Initializes {@code collectors}.
	 */
	protected void resetCollectors()
	{
		collectors	= new ArrayList<Collector>();
	}

	/**
	 * Used by subclasses to add a {@code Collector} when parsing succeeds.
	 * @param collector
	 */
	protected void addCollector(Collector collector)
	{
		collectors.add(collector);
	}
}