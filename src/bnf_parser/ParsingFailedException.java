package bnf_parser;

public class ParsingFailedException extends Exception
{
	public ParsingFailedException()
	{
		super();
	}

	public ParsingFailedException(String message)
	{
		super(message);
	}
}
