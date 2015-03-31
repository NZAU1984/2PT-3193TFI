package bnf_parser;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import bnf_parser.callables.Callable;
import bnf_parser.callables.CallableContainsMoreThanOneCollectorException;
import bnf_parser.collectors.Collector;


/**
 * This class allows to transform a file to a collection of nested objects based on specific rules defined by the user.
 * Nested objects are in fact classes that extend {@link Collector}. Rules are defined using the class {@link Rule}.
 *
 * @author Hubert Lemelin
 *
 */
public class BnfParser
{
	// PUBLIC STATIC CONSTANTS

	/**
	 * UTF-8 encoding.
	 */
	public static final String	UTF8_ENCODING	= "UTF-8";

	/**
	 * Latin1 encoding.
	 */
	public static final String LATIN_1_ENCODING	= "ISO-8859-1";

	// PROTECTED PROPERTIES

	/**
	 * The instance of {@link Subparser}.
	 */
	protected Subparser subparser;

	// PUBLIC CONSTRUCTORS

	/**
	 * Default constructor.
	 */
	public BnfParser()
	{
		subparser	= new Subparser();
	}

	// PUBLIC METHODS

	/**
	 * Open the specified {@code filename} using the specified {@code charset}.
	 *
	 * @param filename		The path of the file to be opened.
	 * @param charset		The charset to use to open the file.
	 * @throws IOException	Thrown whenever an error occurs while opening the file.
	 */
	public void open(String filename, String charset) throws IOException
	{
		subparser.open(filename, charset);
	}

	/**
	 * Closes the current opened file.
	 */
	public void close()
	{
		subparser.close();
	}

	/**
	 * Returns a new empty {@link Rule}.
	 *
	 * @return	A new empty rule.
	 */
	public Rule newRule()
	{
		return new Rule();
	}

	/**
	 * Evaluates the specified {@link Rule}.
	 *
	 * @param rule	The {@link Rule} to be evaluated.
	 *
	 * @return	 A new instance of the {@link Collector} of the {@link Rule} if one was defined. Returns NULL if the
	 * 		{@link Rule} does not have a collector.
	 *
	 * @throws ParsingFailedException							Throw if the evaluation of the {@link Rule} fails.
	 * @throws CallableContainsMoreThanOneCollectorException	Thrown if a {@link Callable} contains more than one
	 * 															{@link Collector}.
	 * @throws NoFileSpecifiedException							Thrown if parsing occurs before the file was specified.
	 */
	public Collector evaluateRule(Rule rule)
			throws ParsingFailedException, CallableContainsMoreThanOneCollectorException, NoFileSpecifiedException
	{
		return subparser.evaluateRule(rule);
	}

	/**
	 * Inner class which is the brain of the parser. All the magic happens here.
	 *
	 * @author Hubert Lemelin
	 *
	 */
	protected class Subparser implements SubparserInterface
	{
		// PROTECTED PROPERTIES

		/**
		 * Stores the filesize of current opened file.
		 */
		protected long fileSize;

		/**
		 * Stored the charset currently used to read the file.
		 */
		protected String fileCharset;

		/**
		 * The instance of {@link FileInputStream} used to read the file.
		 */
		protected FileInputStream fileInputStream;

		/**
		 * The instance of {@link CharBuffer} used to read the file. This allows to get substrings in the file without
		 * having to move the cursor explicitly.
		 */
		protected CharBuffer charBuffer;

		/**
		 * Current character position in the file. A character may have more than one byte.
		 */
		protected int charBufferPosition;

		/**
		 * Current byte position in the file.
		 */
		protected int rawBufferPosition;

		// PUBLIC CONSTRUCTORS

		/**
		 * Constructor.
		 */
		public Subparser()
		{

		}

		// PUBLIC METHODS

		/**
		 * Opens the specified file.
		 *
		 * @param filename	The filename of the file to open.
		 * @param charset	The charset to be used to read the file.
		 *
		 * @throws IOException	Thrown whenever a file error occurs.
		 */
		public void open(String filename, String charset) throws IOException
		{
			if(!UTF8_ENCODING.equals(charset) && !LATIN_1_ENCODING.equals(charset))
			{
				throw new UnsupportedCharsetException(charset);
			}

			// http://www.java-tips.org/java-se-tips/java.util.regex/how-to-apply-regular-expressions-on-the-contents-of-a.html
			/* Below, we use a CharBuffer to read the file. It allows to use string functions with having to explicitly
			 * move the cursor. It also loads as much characters as it needs. Also, with regular expression, it reads
			 * as many characters as needed. If a pattern matches a short string, it doesn't have to read lots and lots
			 * of characters. The only thing is that at any time, it cannot contain more than 'fileChannel.size()'
			 * which is about 2gb, which, here, is way enough! */
			File file				= new File(filename);
			fileCharset				= charset;
			fileSize				= file.length();
			fileInputStream			= new FileInputStream(file);
	        FileChannel fileChannel	= fileInputStream.getChannel();
	        ByteBuffer byteBuffer	= fileChannel.map(FileChannel.MapMode.READ_ONLY, 0, (int) fileChannel.size());
	        charBuffer				= Charset.forName(charset).newDecoder().decode(byteBuffer);
	        charBufferPosition		= 0;
	        rawBufferPosition		= 0;
		}

		/**
		 * Closes the current file.
		 */
		public void close()
		{
			if(null == fileInputStream)
			{
				return;
			}

			try
			{
				fileInputStream.close();
			}
			catch (IOException e)
			{
				// TODO Auto-generated catch block
				//e.printStackTrace();
			}
			finally
			{
				fileInputStream	= null;
				charBuffer		= null;
			}
		}

		@Override
		public Collector evaluateRule(Rule rule)
				throws ParsingFailedException, CallableContainsMoreThanOneCollectorException, NoFileSpecifiedException
		{
			if((null == fileInputStream) || (null == charBuffer))
			{
				throw new NoFileSpecifiedException();
			}

			/* Creating the rule's collector. It might be 'null' (for example, a simple pattern matching rule may only have
			 * to return true if the pattern matched, but the matched string is not important. As an example, creating a
			 * rule to match spaces often doesn't have to 'collectString' the matched spaces. If a callable's collector will
			 * override the rule's collector, the collector, here, will be null as it doesn't make any sense to create a
			 * collector to later replace it by another. */
			Collector collector = rule.createCollector();

			int startOffset	= getBufferPosition();

			/* The rule only has one instance, so we must reset it before adding collectors to it. */
			rule.resetIterator();

			int bufferPositionBeforeParsing		= getBufferPosition();
			int rawBufferPositionBeforeParsing	= getRawBufferPosition();

			while(rule.hasNext())
			{
				/* Get next subrule. */
				Callable callable	= rule.next();

				if(!callable.parse(this))
				{
					/* Subrule failed. Let's throw an exception. */

					setBufferPosition(bufferPositionBeforeParsing);
					setRawBufferPosition(rawBufferPositionBeforeParsing);

					throw new ParsingFailedException();
				}

				if(rule.doOverrideCollector())
				{
					/* If the current subrule "offers" its collector to the rule, let's grab it. */

					collector = callable.getCollector();
				}
				else if(rule.noCollectorOverriding() && (null != collector))
				{
					/* If there is no collector overriding and the rule has a collector, let's add all the current
					 * subrule's collectors to it.  */

					for(Collector callableCollector	: callable.getCollectors())
					{
						collector.addChild(callableCollector, rule.getIndex());
					}
				}
			}

			if(rule.doMatchEndOfFile() && (rawBufferPosition != fileSize))
			{
				/* All subrules were applied but the rule has to match the end of file but it doesn't: exception. */
				throw new ParsingFailedException();
			}


			if(null != collector)
			{
				/* If there is a collector, let's send the start/end offsets in the file. */

				collector.setOffsets(startOffset, getBufferPosition());
			}

			return collector;
		}

		/**
		 * Checks if at the current parser's position the provided pattern matches.
		 * @param pattern the pattern to check which must not start with '^'
		 * @return the matched string if success, null otherwise
		 */
		@Override
		public String matchPattern(String pattern)
		{
			Pattern pattern1	= Pattern.compile("(^" + pattern + ")", Pattern.DOTALL);
			Matcher matcher		= pattern1.matcher(charBuffer);

			/* There is only one group that can be found, the one contained in the parentheses above. */
			if(matcher.find())
			{
				String group = matcher.group();

				/* Moving the buffer's position after the found string. */
				setBufferPosition(charBufferPosition + group.length());

				incrementRawBufferPosition(group);

				return group;
			}

			/* Pattern did not match. */
			return null;
		}

		// PROTECTED METHODS

		/**
		 * Returns the buffer's current position (character position).
		 * @return	The buffer's current position.
		 */
		protected int getBufferPosition()
		{
			return charBufferPosition;
		}

		/**
		 * Returns the buffer's current position (byte position).
		 * @return	The buffer's current position.
		 */
		protected int getRawBufferPosition()
		{
			return rawBufferPosition;
		}

		/**
		 * Sets the buffer position.
		 *
		 * @param position	The position.
		 */
		protected void setBufferPosition(int position)
		{
			charBufferPosition	= position;

			charBuffer.position(charBufferPosition);
		}

		/**
		 * Sets the raw position of the buffer.
		 *
		 * @param position	The position.
		 */
		protected void setRawBufferPosition(int position)
		{
			rawBufferPosition	= position;
		}

		/**
		 * Increments the raw buffer positoin by {@code increment}.
		 *
		 * @param increment	The increment step.
		 */
		protected void incrementRawBufferPosition(int increment)
		{
			rawBufferPosition	+= increment;
		}

		/**
		 * Increments the raw buffer position according to the string length computed using the used charset.
		 *
		 * @param string	The string form which calculate the length.
		 */
		protected void incrementRawBufferPosition(String string)
		{
			if(null == string)
			{
				return;
			}

			incrementRawBufferPosition(fileCharset.equals(UTF8_ENCODING) ? length(string) : string.length());
		}

		/**
		 * Returns the total number of bytes in a {@link SubSequence}. Many encodings such as UTF-8 sometimes use more
		 * than one byte to create a character. This method is used to calculate offset in the file.
		 *
		 * @param sequence	The {@link CharSequence} from which return the length.
		 *
		 * @return	The length of the sequence.
		 */
		protected int length(CharSequence sequence)
		{
			int count = 0;

			for (int i = 0, iMax = sequence.length(); i < iMax; ++i)
			{
				char character = sequence.charAt(i);

				if (character <= 0x7F)
				{
					++count;
				}
				else if (character <= 0x7FF)
				{
					count	+= 2;
				}
				else if (Character.isHighSurrogate(character))
				{
					count	+= 4;

					++i;
				}
				else
				{
					count	+= 3;
				}
			}

			return count;
		}
	}
}
