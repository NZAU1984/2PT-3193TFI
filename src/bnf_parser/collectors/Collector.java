package bnf_parser.collectors;

/**
 * This abstract class is the skeleton of a collector which is called by the parsed to store either strings (via
 * {@link StringCollector}) or other collectors (subclassed of Collector) by using the {@link #addChild(Collector, int)}
 * method. It also allows to define the start and end offsets in the file between which the parser was able to match
 * the current rule.
 *
 * @author Hubert Lemelin
 *
 */
public abstract class Collector
{
	// PRIVATE PROPERTIES

	/**
	 * Start offset of current Collector corresponding to the position in the file when parsing of the current rule was
	 * started.
	 */
	private int startOffset;

	/**
	 * End offset of current Collector corresponding to the position in the file when parsing of the current rule was
	 * finished.
	 */
	private int endOffset;

	// PUBLIC ABSTRACT METHODS

	/**
	 * Called in subclass when a rule/subrule was successfully parsed and returns a certain subclass of Collector. The
	 * current (subclass) Collector can decide whether to use it or not.
	 *
	 * @param collector	The collector returned by the parser. It might be null.
	 * @param index		A custom index defined when creating the current rule to identify more easily a collector, which
	 * is particularly useful when many Collector instances are used, and even more useful when some instances are
	 * allowed to be missing (minOccurences = 0).
	 */
	public abstract void addChild(Collector collector, int index);

	// PUBLIC METHODS

	/**
	 * Sets the start offset which is the position in the file where the parsing started.
	 *
	 * @param startOffset The start offset.
	 */
	public void setStartOffset(int startOffset)
	{
		this.startOffset	= startOffset;
	}

	/**
	 * Sets the end offset which is the position in the file where the parsing (successfully) ended.
	 * @param endOffset	The end offset.
	 */
	public void setEndOffset(int endOffset)
	{
		this.endOffset	= endOffset;
	}

	/**
	 * Sets both the start offset and the end offset, see {@link #setStartOffset(int)} and {@link #setEndOffset(int)}.
	 * @param startOffset	The start offset.
	 * @param endOffset		The end offset.
	 */
	public void setOffsets(int startOffset, int endOffset)
	{
		setStartOffset(startOffset);
		setEndOffset(endOffset);
	}

	/**
	 * Returns the start offset. See {@link #setStartOffset(int)} and {@link #setEndOffset(int)}.
	 * @return	The start offset.
	 */
	public int getStartOffset()
	{
		return startOffset;
	}

	/**
	 * Returns the end offset. See {@link #setStartOffset(int)} and {@link #setEndOffset(int)}.
	 * @return	The end offset.
	 */
	public int getEndOffset()
	{
		return endOffset;
	}
}
