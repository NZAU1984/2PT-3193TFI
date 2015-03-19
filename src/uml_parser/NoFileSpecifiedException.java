package uml_parser;

public class NoFileSpecifiedException extends Exception
{
	public NoFileSpecifiedException()
	{
		super();
	}

	public NoFileSpecifiedException(String message)
	{
		super(message);
	}
}
