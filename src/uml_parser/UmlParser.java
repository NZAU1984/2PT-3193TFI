package uml_parser;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;

import uml_parser.collectors.AggregationCollector;
import uml_parser.collectors.AssociationCollector;
import uml_parser.collectors.ClassContentCollector;
import uml_parser.collectors.DataitemCollector;
import uml_parser.collectors.DataitemListCollector;
import uml_parser.collectors.GeneralizationCollector;
import uml_parser.collectors.IdentifierListCollector;
import uml_parser.collectors.ModelCollector;
import uml_parser.collectors.OperationCollector;
import uml_parser.collectors.OperationListCollector;
import uml_parser.collectors.RoleCollector;
import uml_parser.collectors.RoleListCollector;
import bnf_parser.BnfParser;
import bnf_parser.IncorrectCollectorException;
import bnf_parser.NoFileSpecifiedException;
import bnf_parser.NoSubruleDefinedException;
import bnf_parser.ParsingFailedException;
import bnf_parser.Rule;
import bnf_parser.callables.CallableContainsMoreThanOneCollectorException;
import bnf_parser.collectors.Collector;

/**
 * This class transforms a UML definition file into a collection of nested objects.
 *
 * @author Hubert Lemelin
 *
 */
public class UmlParser
{
	// PUBLIC STATIC CONSTANTS

	/**
	 * UTF8 charset constant.
	 */
	public static final String	UTF8_ENCODING	= BnfParser.UTF8_ENCODING;

	/**
	 * Latin1 charset constant.
	 */
	public static final String LATIN_1_ENCODING	= BnfParser.LATIN_1_ENCODING;

	// PROTECTED PROPERTIES

	/**
	 * The instance of {@link BnfParser} used to transform a UML definition file into a collection of differents
	 * objects.
	 */
	protected BnfParser bnfParser;

	/* Below are a series of rules. They are almost the same as the ones defined in the BNF grammar in "TP1". */

	protected Rule space;

	protected Rule identifier;

	protected Rule type;

	protected Rule dataitem;

	protected Rule dataitemOptionalRepeat;

	protected Rule dataitemList;

	protected Rule operation;

	protected Rule operationOptionalRepeat;

	protected Rule operationList;

	protected Rule operations;

	protected Rule classContent;

	protected Rule multiplicity;

	protected Rule role;

	protected Rule association;

	protected Rule identifierOptionalRepeat;

	protected Rule identifierList;

	protected Rule generalization;

	protected Rule roleOptionalRepeat;

	protected Rule roleList;

	protected Rule aggregation;

	protected Rule model;

	// PRIVATE PROPERTIES

	/**
	 * Stores the unique isntance of {@link UmlParser} created by the singleton design pattern.
	 */
	private static UmlParser instance;

	// PUBLIC STATIC METHODS

	/**
	 * Returns the unique instance of {@link UmlParser} created by the singleton design pattern. This allows to reduce
	 * the memory used since rules used don't change, so it would be useless to define them over and over if more than
	 * once instance of {@link UmlParser} has to be used.
	 *
	 * @return	The unique instance of {@link UmlParser}.
	 */
	public static UmlParser getInstance()
	{
		if(null == instance)
		{
			instance	= new UmlParser();
		}

		return instance;
	}

	// PRIVATE CONSTRUCTOR

	/**
	 * Private constructor.
	 */
	private UmlParser()
	{
		bnfParser	= new BnfParser();

		/* Let's create the rules. Since this constructor will only be called once, rules will be defined only once in
		 * the whole program. */
		createRules();
	}

	// PUBLIC METHODS

	/**
	 * Parses the specified UML definition file using the specified charset.
	 *
	 * @param filename	The filename of the UML definition file to be parsed.
	 * @param charset	The charset of the file.
	 *
	 * @return	An instance of {@link ModelCollector} which implements {@link Model}.
	 *
	 * @throws IOException							Thrown when a file error occurs (ex. file not found).
	 *
	 * @throws uml_parser.ParsingFailedException	Thrown when parsing fails.
	 */
	public Model parse(String filename, String charset) throws IOException, uml_parser.ParsingFailedException
	{
		bnfParser.open(filename, charset);

		Collector parsedModel	= null;

		try
		{
			/* Let's try to parse the model. */

			parsedModel	= bnfParser.evaluateRule(model);
		}
		catch(CallableContainsMoreThanOneCollectorException e)
		{
			/* This error is thrown when the parser asks a Callable to return its Collector. It cannot contain more than
			 * one collector. Here, it should never happen since rules don't add more than one Collector. */

			e.printStackTrace();
		}
		catch(ParsingFailedException e)
		{
			/* If one rule mandatory rule fails, this exception is thrown. */

			throw new uml_parser.ParsingFailedException();
		}
		catch(NoFileSpecifiedException e)
		{
			/* Should not happen. */

			e.printStackTrace();
		}
		finally
		{
			/* Let's close the file no matter what happens to prevent memory leaks. */

			bnfParser.close();
		}

		if(!(parsedModel instanceof Model))
		{
			/* This should never happen since we try to evaluation the Model rule which returns an instance of
			 * ModelCollector implementing Model. */

			throw new uml_parser.ParsingFailedException();
		}

		return (Model) parsedModel;
	}

	// PROTECTED METHODS

	/**
	 * Creates all the rules necessary to parse the BNF grammar defined in "TP1".
	 */
	protected void createRules()
	{
		try
		{
			/* Below is a set of rules which we will not explain. Please refer to "TP1". */
			space	= bnfParser.newRule()
					.matchPatternWithoutCollecting("\\s+", 1, 1);

			identifier = bnfParser.newRule()
						.matchPattern("[A-Za-z_\\-0-9]+", 1, 1).overrideCollector();

			type	= bnfParser.newRule()
					.matchRule(identifier, 1, 1).overrideCollector();

			/* A dataitem corresponds to <identifier>:<multiplicity> (with/without spaces). Since 'identifier' and 'multiplicity' both
			 * return a StringCollector, we help differentiate them by setting different indices. */
			dataitem	= bnfParser.newRule().setCollector(DataitemCollector.class)
					.matchRule(space, 0, 1)
					.matchRule(identifier, 1, 1).setIndex(0)
					.matchRule(space, 0, 1)
					.matchStringWithoutCollecting(":", 1, 1)
					.matchRule(space, 0, 1)
					.matchRule(identifier, 1, 1).setIndex(1)
					.matchRule(space, 0, 1);

			dataitemOptionalRepeat	= bnfParser.newRule()
					.matchStringWithoutCollecting(",", 1, 1)
					.matchRule(dataitem, 1, 1).overrideCollector();

			/* This is an alias of 'attribute_list' and 'arg_list' which are the same: [<dataitem>{,<dataitem>}].
			 * By using a BNF grammar, optional list with optional repeats have to be created a certain way:
			 *     - Create a rule for one single item (rule 'dataitem')
			 *     - Create a rule for one single item preceeded by a coma (that's the part after the first element, the
			 *       part in {} in BNF; here it's the rule 'dataitemOptionalRepeat')
			 *     - Create a rule that must match one single element (rule 'dataitem') and that can match 0 or more
			 *       times an element preceeded by a come (rule "dataitemOptionalRepeat')
			 *     - Finally, where the list is optional, like in 'operation', simply ask to match the rule
			 *       'dataitemList' 0 or 1 time. */
			dataitemList	= bnfParser.newRule().setCollector(DataitemListCollector.class)
					.matchRule(dataitem, 1, 1)
					.matchRule(dataitemOptionalRepeat, 0, Rule.INFINITY);

			operation	= bnfParser.newRule().setCollector(OperationCollector.class)
					.matchRule(space, 0, 1)
					.matchRule(identifier, 1, 1).setIndex(0)
					.matchRule(space, 0, 1)
					.matchStringWithoutCollecting("(", 1, 1)
					.matchRule(dataitemList, 0, 1)
					.matchStringWithoutCollecting(")", 1, 1)
					.matchRule(space, 0, 1)
					.matchStringWithoutCollecting(":", 1, 1)
					.matchRule(space, 0, 1)
					.matchRule(type, 1, 1).setIndex(1)
					.matchRule(space, 0, 1);

			operationOptionalRepeat	= bnfParser.newRule()
					.matchStringWithoutCollecting(",", 1, 1)
					.matchRule(operation, 1, 1).overrideCollector();

			operationList	= bnfParser.newRule().setCollector(OperationListCollector.class)
					.matchRule(space, 1, 1)
					.matchRule(operation, 1, 1)
					.matchRule(operationOptionalRepeat, 0, Rule.INFINITY);

			operations	= bnfParser.newRule()
					.matchStringWithoutCollecting("OPERATIONS", 1, 1)
					.matchRule(operationList, 0, 1).overrideCollector();

			classContent	= bnfParser.newRule().setCollector(ClassContentCollector.class)
					.matchRule(space, 0, 1)
					.matchStringWithoutCollecting("CLASS", 1, 1)

					/* Forces a space between 'CLASS' and <identifier>, otherwise both could be "stuck" together which
					 * would not be very logical. */
					.matchRule(space, 1, 1)
					.matchRule(identifier, 1, 1)

					/* Same as comment above. */
					.matchRule(space, 1, 1)
					.matchStringWithoutCollecting("ATTRIBUTES", 1, 1)

					/* Let's force a space between 'ATTRIBUTES' and [<identifier> of a possible <dataitem> for the list
					 * of attributes if it exists OR 'OPERATIONS' if no attributes exist]. Otherwise it would allow
					 * something like 'ATTRIBUTESnom_equipe:String' or 'ATTRIBUTESOPERATIONS'. */
					.matchRule(space, 1, 1)

					.matchRule(dataitemList, 0, 1)
					.matchStringWithoutCollecting("OPERATIONS", 1, 1)

					.matchRule(operationList, 0, 1)

					/* If there was no operationList, there might be a space before ";", that's why we check for it. */
					.matchRule(space, 0, 1)
					.matchStringWithoutCollecting(";", 1, 1)
					.matchRule(space, 0, 1);

			/* ASSOCIATION */

			multiplicity	= bnfParser.newRule()
					.matchPattern("ONE_OR_MANY|ONE|MANY|OPTIONALLY_ONE|UNDEFINED", 1, 1).overrideCollector();

			role	= bnfParser.newRule().setCollector(RoleCollector.class)
					.matchStringWithoutCollecting("CLASS", 1, 1)
					.matchRule(space, 1, 1)
					.matchRule(identifier, 1, 1).setIndex(0)
					.matchRule(space, 1, 1)
					.matchRule(multiplicity, 1, 1).setIndex(1);

			association	= bnfParser.newRule().setCollector(AssociationCollector.class)
					.matchRule(space, 0, 1)
					.matchStringWithoutCollecting("RELATION", 1, 1)
					.matchRule(space, 1, 1)
					.matchRule(identifier, 1,  1)
					.matchRule(space, 1, 1)
					.matchStringWithoutCollecting("ROLES", 1, 1)
					.matchRule(space, 1, 1)
					.matchRule(role, 1, 1).setIndex(0)
					.matchRule(space, 0, 1)
					.matchStringWithoutCollecting(",", 1, 1)
					.matchRule(space,  0,  1)
					.matchRule(role, 1, 1).setIndex(1)
					.matchRule(space,  0, 1)
					.matchStringWithoutCollecting(";", 1, 1)
					.matchRule(space, 0, 1);

			/* GENERALIZATION */

			identifierOptionalRepeat	= bnfParser.newRule()
					.matchStringWithoutCollecting(",", 1, 1)
					.matchRule(space, 0, 1)
					.matchRule(identifier, 1, 1).overrideCollector()
					.matchRule(space, 0, 1);

			identifierList	= bnfParser.newRule().setCollector(IdentifierListCollector.class)
					.matchRule(space, 0, 1)
					.matchRule(identifier, 1, 1)
					.matchRule(identifierOptionalRepeat, 0, Rule.INFINITY);

			generalization	= bnfParser.newRule().setCollector(GeneralizationCollector.class)
					.matchRule(space, 0, 1)
					.matchStringWithoutCollecting("GENERALIZATION", 1, 1)
					.matchRule(space,  1, 1)
					.matchRule(identifier, 1, 1)
					.matchRule(space, 1, 1)
					.matchStringWithoutCollecting("SUBCLASSES", 1, 1)
					.matchRule(space, 1, 1)
					.matchRule(identifierList, 1, 1)
					.matchRule(space, 0, 1)
					.matchStringWithoutCollecting(";", 1, 1)
					.matchRule(space, 0, 1);

			/* AGGREGATION */

			roleOptionalRepeat	= bnfParser.newRule()
					.matchRule(space, 0, 1)
					.matchStringWithoutCollecting(",", 1, 1)
					.matchRule(space, 0, 1)
					.matchRule(role, 1, 1).overrideCollector();

			roleList	= bnfParser.newRule().setCollector(RoleListCollector.class)
					.matchRule(role, 1, 1)
					.matchRule(roleOptionalRepeat, 0, Rule.INFINITY);

			aggregation	= bnfParser.newRule().setCollector(AggregationCollector.class)
					.matchRule(space, 0, 1)
					.matchStringWithoutCollecting("AGGREGATION", 1, 1)
					.matchRule(space, 0, 1)
					.matchStringWithoutCollecting("CONTAINER", 1, 1)
					.matchRule(space, 1, 1)
					.matchRule(role, 1,  1)
					.matchRule(space, 1, 1)
					.matchStringWithoutCollecting("PARTS", 1, 1)
					.matchRule(space, 1, 1)
					.matchRule(roleList, 1, 1)
					.matchRule(space, 0, 1)
					.matchStringWithoutCollecting(";", 1, 1)
					.matchRule(space, 0, 1);

			/* MODEL */

			model	= bnfParser.newRule().setCollector(ModelCollector.class).mustMatchEndOfFile()

					/* Allows spaces at the beginning of file. Let's not be too strict... */
					.matchRule(space, 0, 1)

					.matchStringWithoutCollecting("MODEL", 1, 1)
					.matchRule(space, 1, 1)
					.matchRule(identifier, 1, 1)
					.matchRule(space, 0, 1)
					.matchAnyRule(0, Rule.INFINITY, classContent, association, generalization, aggregation)

					/* Allows spaces at the end of file. Let's not be too strict... This optional rule will always
					 * fail because classContent, association, generalization and aggregation already checks for
					 * trailing spaces. */
					.matchRule(space, 0, 1);
		}
		catch (NoSubruleDefinedException e)
		{
			/* Should never happen since rules are properly defined. */

			e.printStackTrace();
		}
		catch (IncorrectCollectorException e)
		{
			/* Should never happen since rules are properly defined. */

			e.printStackTrace();
		}
	}

	/**
	 * Returns a substring from the original file by asking a {@link Collector} to returns the start and end offsets
	 * in the file where the {@link Rule} associated with it matched in the file.
	 *
	 * @param filename	The filename of the file that was parsed.
	 * @param charset	The charset used to open the file.
	 *
	 * @param obj	An object that must extend {@link Collector} from which we want the start/end offsets.
	 *
	 * @return	A {@link String} corresponding to the substring of the file that was matched by the {@link Rule}
	 * 			associated with the {@link Collector}.
	 */
	public String getSubstringFromFile(String filename, String charset, Object obj)
	{
		if(!(obj instanceof Collector))
		{
			return null;
		}

		String substring	= null;

		Collector collector	= (Collector) obj;

		FileInputStream fileInputStream = null;

		try
		{
			/* Below we open the file and use a CharBuffer so we don't need to move directly in the file. We simply
			 * have to call the method 'subSequence' and that method will go by itself to the right position in the
			 * file. It helps save lot's of memory. */
			fileInputStream = new FileInputStream(filename);
			FileChannel fileChannel	= fileInputStream.getChannel();
	        ByteBuffer byteBuffer	= fileChannel.map(FileChannel.MapMode.READ_ONLY, 0, (int) fileChannel.size());
	        CharBuffer charBuffer	= Charset.forName(charset).newDecoder().decode(byteBuffer);

	        substring	= charBuffer.subSequence(collector.getStartOffset(), collector.getEndOffset()).toString();
		}
		catch (Exception e)
		{
			/* If any error occurs, 'substring' will be null, let's simply return that value. */
		}
		finally
		{
			if(null != fileInputStream)
			{
				try
				{
					fileInputStream.close();
				}
				catch (IOException e)
				{

				}
			}
		}

		return substring;
	}
}
