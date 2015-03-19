package bnf_parser;

public class NoSubruleDefinedException extends Exception
{
	public NoSubruleDefinedException()
	{
		super();
	}

	public NoSubruleDefinedException(String message)
	{
		super(message);
	}
}
