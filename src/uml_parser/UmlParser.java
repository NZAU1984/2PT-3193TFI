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

public class UmlParser
{
	// PUBLIC STATIC CONSTANTS

	public static final String	UTF8_ENCODING	= BnfParser.UTF8_ENCODING;

	public static final String LATIN_1_ENCODING	= BnfParser.LATIN_1_ENCODING;

	// PROTECTED PROPERTIES

	protected BnfParser bnfParser;

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

	private static UmlParser instance;

	// PUBLIC STATIC METHODS

	public static UmlParser getInstance()
	{
		if(null == instance)
		{
			instance	= new UmlParser();
		}

		return instance;
	}

	// PRIVATE CONSTRUCTOR

	private UmlParser()
	{
		bnfParser	= new BnfParser();

		createRules();
	}

	// PUBLIC METHODS
	public Model parse(String filename, String charset) throws IOException, uml_parser.ParsingFailedException
	{
		bnfParser.open(filename, charset);

		Collector parsedModel	= null;

		try
		{
			parsedModel	= bnfParser.evaluateRule(model);
		}
		catch(CallableContainsMoreThanOneCollectorException e)
		{
			e.printStackTrace();
		}
		catch(ParsingFailedException e)
		{
			throw new uml_parser.ParsingFailedException();
		}
		catch(NoFileSpecifiedException e)
		{
			// Should not happen.
			e.printStackTrace();
		}
		finally
		{
			bnfParser.close();
		}

		if(!(parsedModel instanceof Model))
		{
			// Should not happen.
			throw new uml_parser.ParsingFailedException();
		}

		return (Model) parsedModel;
	}

	// PROTECTED METHODS

	protected void createRules()
	{
		try
		{
			space	= bnfParser.newRule().setName("space")
					.matchPatternWithoutCollecting("\\s+", 1, 1);

			identifier = bnfParser.newRule().setName("identifier")
						.matchPattern("[A-Za-z_\\-0-9]+", 1, 1).overrideCollector();

			type	= bnfParser.newRule().setName("multiplicity")
					.matchRule(identifier, 1, 1).overrideCollector();

			/* A dataitem corresponds to <identifier>:<multiplicity> (with/without spaces). Since 'identifier' and 'multiplicity' both
			 * return a StringCollector, we help differentiate them by setting different indices. */
			dataitem	= bnfParser.newRule().setCollector(DataitemCollector.class).setName("dataitem")
					.matchRule(space, 0, 1)
					.matchRule(identifier, 1, 1).setIndex(0)
					.matchRule(space, 0, 1)
					.matchStringWithoutCollecting(":", 1, 1)
					.matchRule(space, 0, 1)
					.matchRule(identifier, 1, 1).setIndex(1)
					.matchRule(space, 0, 1);

			dataitemOptionalRepeat	= bnfParser.newRule().setName("dataitemOptionalRepeat")
					.matchStringWithoutCollecting(",", 1, 1)
					.matchRule(dataitem, 1, 1).overrideCollector();

			/* This is an alias of 'attribute_list' and 'arg_list' which are the same: [<dataitem>{,<dataitem>}] */
			dataitemList	= bnfParser.newRule().setCollector(DataitemListCollector.class).setName("dataitemlist")
					.matchRule(dataitem, 1, 1)
					.matchRule(dataitemOptionalRepeat, 0, Rule.INFINITY);

			operation	= bnfParser.newRule().setCollector(OperationCollector.class).setName("operation")
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

			operationOptionalRepeat	= bnfParser.newRule().setName("operationOptionalRepeat")
					.matchStringWithoutCollecting(",", 1, 1)
					.matchRule(operation, 1, 1).overrideCollector();

			operationList	= bnfParser.newRule().setCollector(OperationListCollector.class).setName("operationList")
					.matchRule(space, 1, 1)
					.matchRule(operation, 1, 1)
					.matchRule(operationOptionalRepeat, 0, Rule.INFINITY);

			operations	= bnfParser.newRule().setName("operations")
					.matchStringWithoutCollecting("OPERATIONS", 1, 1)
					.matchRule(operationList, 0, 1).overrideCollector();

			classContent	= bnfParser.newRule().setCollector(ClassContentCollector.class).setName("classContent")
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

			multiplicity	= bnfParser.newRule().setName("multiplicity")
					.matchPattern("ONE_OR_MANY|ONE|MANY|OPTIONALLY_ONE|UNDEFINED", 1, 1).overrideCollector();

			role	= bnfParser.newRule().setCollector(RoleCollector.class).setName("role")
					.matchStringWithoutCollecting("CLASS", 1, 1)
					.matchRule(space, 1, 1)
					.matchRule(identifier, 1, 1).setIndex(0)
					.matchRule(space, 1, 1)
					.matchRule(multiplicity, 1, 1).setIndex(1);

			association	= bnfParser.newRule().setCollector(AssociationCollector.class).setName("association")
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

			identifierOptionalRepeat	= bnfParser.newRule().setName("identifierOptionalRepeat")
					.matchStringWithoutCollecting(",", 1, 1)
					.matchRule(space, 0, 1)
					.matchRule(identifier, 1, 1).overrideCollector()
					.matchRule(space, 0, 1);

			identifierList	= bnfParser.newRule().setCollector(IdentifierListCollector.class).setName("identifierList")
					.matchRule(space, 0, 1)
					.matchRule(identifier, 1, 1)
					.matchRule(identifierOptionalRepeat, 0, Rule.INFINITY);

			generalization	= bnfParser.newRule().setCollector(GeneralizationCollector.class).setName("generalization")
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

			roleOptionalRepeat	= bnfParser.newRule().setName("roleOptionalRepeat")
					.matchRule(space, 0, 1)
					.matchStringWithoutCollecting(",", 1, 1)
					.matchRule(space, 0, 1)
					.matchRule(role, 1, 1).overrideCollector();

			roleList	= bnfParser.newRule().setCollector(RoleListCollector.class).setName("roleList")
					.matchRule(role, 1, 1)
					.matchRule(roleOptionalRepeat, 0, Rule.INFINITY);

			aggregation	= bnfParser.newRule().setCollector(AggregationCollector.class).setName("aggregation")
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
					.matchRule(space, 1, 1)
					.matchAnyRule(1, Rule.INFINITY, classContent, association, generalization, aggregation)

					/* Allows spaces at the end of file. Let's not be too strict... This optional rule will always
					 * fail because classContent, association, generalization and aggregation already checks for
					 * trailing spaces. */
					.matchRule(space, 0, 1);
		}
		catch (NoSubruleDefinedException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (IncorrectCollectorException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public String getSubstringFromFile(String filename, String charset, Object obj)
	{
		if(!(obj instanceof Collector))
		{
			return null;
		}

		Collector collector	= (Collector) obj;

		FileInputStream fileInputStream = null;

		try
		{
			fileInputStream = new FileInputStream(filename);
			FileChannel fileChannel	= fileInputStream.getChannel();
	        ByteBuffer byteBuffer	= fileChannel.map(FileChannel.MapMode.READ_ONLY, 0, (int) fileChannel.size());
	        CharBuffer charBuffer				= Charset.forName(charset).newDecoder().decode(byteBuffer);

	        return charBuffer.subSequence(collector.getStartOffset(), collector.getEndOffset()).toString();
		}
		catch (Exception e)
		{
			//
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


		return null;
	}
}
