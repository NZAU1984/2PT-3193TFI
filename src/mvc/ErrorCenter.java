package mvc;

import java.io.IOException;
import java.util.HashMap;

import uml_parser.ParsingFailedException;

public class ErrorCenter
{
	// PUBLIC STATIC CONSTANTS

	/**
	 * When an {@link IOException} occurs.
	 */
	public static final long INVALID_FILE	= 1L << 0;

	/**
	 * When a {@link ParsingFailedException} occurs.
	 */
	public static final long PARSING_FAILED	= 1L << 1;

	/**
	 * When a class name exists more than once.
	 */
	public static final long DUPLICATE_CLASS_NAME	= 1L << 2;

	/**
	 * When an attribute name exists more than once in one class.
	 */
	public static final long DUPLICATE_ATTRIBUTE_NAME	= 1L << 3;

	/**
	 * When an operation (name, type, signature) exists more than once in one class.
	 */
	public static final long DUPLICATE_OPERATION	= 1L << 4;

	/**
	 * When a generalization specifies an unknown parent class.
	 */
	public static final long UNKNOWN_GENERALIZATION_PARENT_CLASS	= 1L << 5;

	/**
	 * When a generalization specifies an unknown child class.
	 */
	public static final long UNKNOWN_GENERALIZATION_CHILD_CLASS	= 1L << 6;

	/**
	 * When a cycle exists in the inheritance of a class, for example class A is a child of class B, which is a child
	 * of class A (A -> B -> A).
	 */
	public static final long INHERITANCE_CYCLE_DETECTED	= 1L << 7;

	/**
	 * When a class name in an association is unknown.
	 */
	public static final long UNKNOWN_ASSOCIATION_CLASS	= 1L << 8;

	/**
	 * When an association already exists between two classes.
	 */
	public static final long DUPLICATE_ASSOCIATION	= 1L << 9;

	/**
	 * When the name of the container class in an aggregation is unknown.
	 */
	public static final long UNKNOWN_AGGREGATION_CONTAINER_CLASS	= 1L << 10;

	/**
	 * When the name of one of the part classes in an aggregation is unknown.
	 */
	public static final long UNKNOWN_AGGREGATION_PART_CLASS	= 1L << 11;

	// PROTECTED PROPERTIES

	/**
	 * Contains the error messages explaining what each error is.
	 */
	protected HashMap<Long, String> errorMessages;

	protected long currentError	= -1;

	// PUBLIC CONSTRUCTOR

	public ErrorCenter()
	{
		errorMessages	= new HashMap<Long, String>();

		setupErrorMessages();
	}

	// PUBLIC METHODS

	public Error newError(long errorNumber, String details)
	{
		if(errorMessages.containsKey(errorNumber))
		{
			StringBuilder sb	= new StringBuilder();

			sb.append(errorMessages.get(errorNumber));

			if(null != details)
			{
				sb.append(" : ").append(details);
			}

			sb.append(".");

			return new Error(errorNumber, sb.toString());
		}

		return null;
	}

	/**
	 * Creates a new {@link Error} according to the error number provided. If the error number is invalid, it simply
	 * returns {@code null}.
	 *
	 * @param errorNumber	The error number.
	 */
	public Error newError(long errorNumber)
	{
		if(errorMessages.containsKey(errorNumber))
		{
			return newError(errorNumber, null);
		}

		return null;
	}

	// PROTECTED METHODS

	/**
	 * Sets up the errorNumber/message HashMap.
	 */
	protected void setupErrorMessages()
	{
		errorMessages.put(INVALID_FILE, "Fichier invalide");
		errorMessages.put(PARSING_FAILED, "L'analyse du fichier a échoué");
		errorMessages.put(DUPLICATE_CLASS_NAME, "L'identifiant d'une classe existe plus d'une fois");
		errorMessages.put(DUPLICATE_ATTRIBUTE_NAME, "L'identifiant d'un attribut existe plus d'une fois");
		errorMessages.put(DUPLICATE_OPERATION, "Le nom/type/signature d'une opération existe plus d'une fois");
		errorMessages.put(UNKNOWN_GENERALIZATION_PARENT_CLASS,
				"Une généralisation fait référence à une classe parent qui n'exist pas");
		errorMessages.put(UNKNOWN_GENERALIZATION_CHILD_CLASS,
				"Une généralisation fait référence à une classe enfant qui n'exist pas");
		errorMessages.put(INHERITANCE_CYCLE_DETECTED,
				"Il existe un cycle dans l'héritage (entre les classes) défini dans les généralisations");
		errorMessages.put(UNKNOWN_ASSOCIATION_CLASS, "Une des deux classes définies dans une association est inconnue");
		errorMessages.put(DUPLICATE_ASSOCIATION, "Une association entre deux classes existe déjà");
		errorMessages.put(UNKNOWN_AGGREGATION_CONTAINER_CLASS,
				"Le nom de la classe contenant (CONTAINER) d'une aggrégation est inconnu");
		errorMessages.put(UNKNOWN_AGGREGATION_PART_CLASS,
				"Le nom d'une des classes contenues (PARTS) dans une aggrégation est inconnu");
	}

	// PUBLIC INNER CLASSES

	/**
	 * A simple container for error number/message.
	 *
	 * @author Hubert Lemelin
	 */
	public class Error
	{
		// PUBLIC PROPERTIES

		/**
		 * The error number.
		 */
		public final long number;

		/**
		 * The error message.
		 */
		public final String message;

		// PROTECTED CONSTRUCTOR

		/**
		 * Creates a new {@code Error}.
		 *
		 * @param number	The error number.
		 * @param message	The error message.
		 */
		protected Error(long number, String message)
		{
			this.number		= number;
			this.message	= message;
		}
	}

}
