package mvc;

import java.io.File;

import javax.swing.JFileChooser;

import mvc.ModelException.ATTRIBUTES;
import mvc.views.MainWindow;

public class Controller
{
	// PROTECTED PROPERTIES

	/**
	 * The model (hardcoded to save time).
	 */
	protected Model model;

	/**
	 * The view (hardcoded to save time).
	 */
	protected MainWindow view;

	protected JFileChooser fileChooser;

	protected File file;

	public Controller(Model model, MainWindow view)
	{
		this.model	= model;
		this.view	= view;

		fileChooser	= new JFileChooser();

		/* Will select the directory in which the bin/src folder reside. */
		file	= new File(System.getProperty("user.dir"));
	}

	public void selectFileButtonClicked()
	{
		/* Use previous file's directory (at first, it is the project directory). */
		fileChooser.setCurrentDirectory(file);

		int val	= fileChooser.showOpenDialog(view);

		if(val == JFileChooser.APPROVE_OPTION)
		{
			file = fileChooser.getSelectedFile();

			if(null != file)
			{
				view.setFilename(file.getAbsolutePath());
			}
		}
	}

	public void parseButtonClicked()
	{
		if(null != file)
		{
			try
			{
				model.analyseFile(file.getAbsolutePath());
			}
			catch (ModelException e)
			{
				StringBuilder sb	= new StringBuilder();

				switch(e.getError())
				{
					case DUPLICATE_ASSOCIATION:
						sb.append("L'association '")
							.append(e.get(ATTRIBUTES.ASSOCIATION))
							.append("' a été définie plus d'une dans dans la classe '")
							.append(e.get(ATTRIBUTES.CLASS))
							.append("'.");

						break;

					case DUPLICATE_ATTRIBUTE:
						sb.append("L'attribut ")
							.append(e.get(ATTRIBUTES.ATTRIBUTE))
							.append(" a été défini plus d'une fois dans la classe ")
							.append(e.get(ATTRIBUTES.CLASS))
							.append(".");

						break;

					case DUPLICATE_CLASS:
						sb.append("La classe ")
							.append(e.get(ATTRIBUTES.CLASS))
							.append(" a été définie plus d'une fois.");

						break;

					case DUPLICATE_OPERATION:
						sb.append("L'opération ")
							.append(e.get(ATTRIBUTES.OPERATION_NAME))
							.append(" avec la signature '")
							.append(e.get(ATTRIBUTES.OPERATION_SIGNATURE))
							.append(" '")
							.append(" et retournant le type ")
							.append(e.get(ATTRIBUTES.OPERATION_TYPE))
							.append(" a été définie plus d'une fois dans la classe ")
							.append(e.get(ATTRIBUTES.CLASS))
							.append(".");

						break;

					case INHERITANCE_CYCLE:
						sb.append("Un cycle a été détecté dans l'héritage d'une ou plusieurs classes.");

						break;

					case UNKNOWN_AGGREGATION_CONTAINER_CLASS:
						sb.append("La classe contenant (\"container class\") '")
							.append(e.get(ATTRIBUTES.CONTAINER_CLASS))
							.append("' à laquelle fait référence une aggrégation est inconnue.");

						break;

					case UNKNOWN_AGGREGATION_PART_CLASS:
						sb.append("La classe partie (\"part class\") '")
						.append(e.get(ATTRIBUTES.PART_CLASS))
						.append("' est inconnue dans une aggrégation ayant comme contenant (\"container class\") la ")
						.append("classe '")
						.append(e.get(ATTRIBUTES.CONTAINER_CLASS))
						.append("'.");

						break;

					case UNKNOWN_ASSOCIATION_CLASS:
						sb.append("Une association fait référence ");

						if(null == e.get(ATTRIBUTES.SECOND_CLASS))
						{
							sb.append(" à la classe '")
								.append(e.get(ATTRIBUTES.FIRST_CLASS))
								.append("' qui n'existe pas.");
						}
						else
						{
							sb.append(" aux classes '")
							.append(e.get(ATTRIBUTES.FIRST_CLASS))
							.append("' et '")
							.append(e.get(ATTRIBUTES.SECOND_CLASS))
							.append("' qui n'existent pas.");
						}

						break;

					case UNKNOWN_GENERALIZATION_SUBCLASS:
						sb.append("La sous-classe '")
							.append(e.get(ATTRIBUTES.SUBCLASS))
							.append("' n'existe pas dans la généralisation ayant '")
							.append(e.get(ATTRIBUTES.SUPERCLASS))
							.append("' comme super-classe.");

						break;

					case UNKNOWN_GENERALIZATION_SUPERCLASS:
						sb.append("Une généralisation fait référence à la super-classe '")
							.append(e.get(ATTRIBUTES.SUPERCLASS))
							.append("' qui n'existe pas.");

						break;

					default:
						break;
				}

				view.showError(sb.toString());
			}
		}
	}

	public void classListClicked()
	{
		String className	= view.getSelectedClass();

		model.sendClassInfo(className);
	}

	public void associationListClicked()
	{
		int index	= view.getSelectedAssociationIndex();

		if(0 <= index)
		{
			model.sendAssociationDetails(view.getSelectedClass(), index);
		}
	}

	public void aggregationListClicked()
	{
		int index	= view.getSelectedAggregationIndex();

		if(0 <= index)
		{
			model.sendAggregationDetails(view.getSelectedClass(), index);
		}
	}
}
