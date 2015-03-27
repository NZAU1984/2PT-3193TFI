package mvc.models;

class DuplicateException extends Exception
{
	protected DuplicateException()
	{
		super();
	}

	protected DuplicateException(String message)
	{
		super(message);
	}
}
