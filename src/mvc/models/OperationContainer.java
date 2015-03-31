package mvc.models;

import uml_parser.Dataitem;
import uml_parser.Operation;

/**
 * This class stored information about one operation.
 *
 * @author Hubert Lemelin
 *
 */
class OperationContainer
{
	// PROTECTED PROPERTIES

	/**
	 * The reference to the {@link Operation}.
	 */
	protected final Operation operation;

	/**
	 * The signature of the operation.
	 */
	protected final String signature;

	// STATIC METHODS

	/**
	 * Returns the signature of the attributes of an {@link Operation}. We use a static method because sometimes an
	 * exception is thrown when trying to add an operation to a class. By calling this static method, we are able to
	 * show the signature of the erroneous operation to the user.
	 *
	 * @param operation	The operation from which format the signature.
	 *
	 * @return	The signature.
	 */
	public static String formatSignature(Operation operation)
	{
		StringBuilder signatureSb	= new StringBuilder();
		Dataitem[] attributes		= operation.getAttributes();

		for(int i = 0, iMax = attributes.length; i < iMax; ++i)
		{
			if(0 != i)
			{
				signatureSb.append(", ");
			}

			signatureSb.append(attributes[i].getType());
		}

		return signatureSb.toString();
	}

	// CONSTRUCTOR

	/**
	 * Constructor.
	 *
	 * @param operation	The reference to the instance of {@link Operation}.
	 */
	OperationContainer(Operation operation)
	{
		this.operation	= operation;

		signature	= formatSignature(operation);
	}

	// PACKAGE METHODS

	/**
	 * Returns the name of the operation.
	 *
	 * @return	The name of the operation.
	 */
	String getName()
	{
		return operation.getIdentifier();
	}

	/**
	 * Returns the type of the opration.
	 *
	 * @return	The type of the operation.
	 */
	String getType()
	{
		return operation.getType();
	}

	/**
	 * Returns the signature of the attribute of the operation.
	 *
	 * @return	The signature of the attribute of the operation.
	 */
	String getSignature()
	{
		return signature;
	}

	/**
	 * Returns all the attributes of the operation.
	 *
	 * @return	An array of {@link Dataitem}.
	 */
	Dataitem[] getAttributes()
	{
		return operation.getAttributes();
	}

	/**
	 * Returns the number of attributes.
	 *
	 * @return	The number of attributes.
	 */
	int getAttributeCount()
	{
		return operation.getAttributes().length;
	}

	/**
	 * Compares the attribute signature of two operations.
	 *
	 * @param oc	The other operation to compare with.
	 *
	 * @return	{@code true} if signatures are identical, {@code false} otherwise.
	 */
	boolean isSignatureIdentical(OperationContainer oc)
	{
		return signature.equals(oc.signature);
	}

	/**
	 * Returns wether or not two operations are identical. We do not check the type since a class cannot have two
	 * methods with the same name and the same signature regardless of the return type.
	 *
	 * @param oc	The other operation to compare with.
	 *
	 * @return	{@code true} if operations are identical, {@code false} otherwise.
	 */
	boolean equals(OperationContainer oc)
	{
		return operation.getIdentifier().equals(oc.operation.getIdentifier()) && isSignatureIdentical(oc);
	}
}
