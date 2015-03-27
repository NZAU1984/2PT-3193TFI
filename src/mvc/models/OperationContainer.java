package mvc.models;

import uml_parser.Dataitem;
import uml_parser.Operation;

class OperationContainer
{
	protected final Operation operation;

	protected final String signature;

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

	OperationContainer(Operation operation)
	{
		this.operation	= operation;

		signature	= formatSignature(operation);
	}

	String getType()
	{
		return operation.getType();
	}

	Dataitem[] getAttributes()
	{
		return operation.getAttributes();
	}

	int getAttributeCount()
	{
		return operation.getAttributes().length;
	}

	boolean isSignatureIdentical(OperationContainer oc)
	{
		return signature.equals(oc.signature);
	}
}
