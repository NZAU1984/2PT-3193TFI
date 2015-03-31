package mvc;

import java.io.File;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import javax.swing.JFileChooser;

import mvc.models.Metrics.METRICS;
import mvc.models.Model;
import mvc.models.ModelException;
import mvc.models.ModelException.ATTRIBUTES;
import mvc.views.MainWindow;

/**
 * This class is the class contaning the logic of the program. It responds to events send by the view
 * ({@link MainWindow}) and ask the {@link Model} to update the view with data.
 *
 * @author Hubert Lemelin
 *
 */
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

	/**
	 * A {@link JFileChooser} used to ask the file to open.
	 */
	protected JFileChooser fileChooser;

	/**
	 * The current file selected by the user.
	 */
	protected File file;

	/**
	 * Associates each elements of {@link METRICS} to a description.
	 */
	protected HashMap<String, String> metricDetails;

	/**
	 * Current filename of the file being parser. We do not use {@code file} since the use may select another file
	 * without parsing the file right away. We do this to avoid clicking on "Create metrics file" of the file
	 * currently being parsed but save the metrics file in the wrong directory if the selected file is not in the
	 * same directory.
	 */
	protected String filename;

	/**
	 * Construcrtor.
	 *
	 * @param model	Reference to the model.
	 * @param view	Reference to the view.
	 */
	public Controller(Model model, MainWindow view)
	{
		this.model	= model;
		this.view	= view;

		fileChooser	= new JFileChooser();

		/* Will select the directory in which the bin/src folder reside. */
		file	= new File(System.getProperty("user.dir"));

		metricDetails	= new HashMap<String, String>();

		/* Descriptions of metrics. */
		metricDetails.put(METRICS.ANA.toString(), "ANA(ci) : nombre moyen d'arguments des méthodes locales pour la classe ci.");
		metricDetails.put(METRICS.NOM.toString(), "NOM(ci) : nombre de méthodes locales/héritées de la classe.");
		metricDetails.put(METRICS.NOA.toString(), "NOA(ci) : nombre d'attributs locaux/hérités de la classe ci.");
		metricDetails.put(METRICS.ITC.toString(), "ITC(ci) : nombre de fois où d'autres classes du diagramme apparaissent comme types des arguments des méthodes de ci.");
		metricDetails.put(METRICS.ETC.toString(), "ETC(ci) : nombre de fois où ci apparaît comme type des arguments dans les méthodes des autres classes du diagramme.");
		metricDetails.put(METRICS.CAC.toString(), "CAC(ci) : nombre d'associations (incluant les agrégations) locales/héritées auxquelles participe une classe ci.");
		metricDetails.put(METRICS.DIT.toString(), "DIT(ci) : taille du chemin le plus long reliant une classe ci à une classe racine dans le graphe d'héritage.");
		metricDetails.put(METRICS.CLD.toString(), "CLD(ci) : taille du chemin le plus long reliant une classe ci à une classe feuille dans le graphe d'héritage.");
		metricDetails.put(METRICS.NOC.toString(), "NOC(ci) : nombre de sous-classes directes de ci.");
		metricDetails.put(METRICS.NOD.toString(), "NOD(ci) : nombre de sous-classes directes et indirectes de ci.");

		/* At first, disable action buttons since no file is selected. */
		view.disableParseButton();
		view.disablecreateMetricsFileButton();
	}

	/**
	 * Called by the view when the select file button is clicked. It then shows a file selecting box.
	 */
	public void selectFileButtonClicked()
	{
		/* Use previous file's directory (at first, it is the project directory). */
		fileChooser.setCurrentDirectory(file);

		int val	= fileChooser.showOpenDialog(view);

		if(val == JFileChooser.APPROVE_OPTION)
		{
			file = fileChooser.getSelectedFile();

			if((null != file) && file.exists())
			{
				/* Update the view only if existing file. */
				view.setFilename(file.getAbsolutePath());

				/* Since the file is valid we now can parse it. */
				view.enableParseButton();
			}
		}
	}

	/**
	 * Called by the view when the parse button is clicked. The model will now try to parse the file.
	 */
	public void parseButtonClicked()
	{
		if(null != file)
		{
			/* Empty the view. We don't want to show an error with previous information shown behind it. */
			view.resetElements();

			/* Disable the create metrics button. Parsing first has to succeed. */
			view.disablecreateMetricsFileButton();

			try
			{
				filename	= file.getAbsolutePath();

				/* We can allow/disallow multiple inheritance. */
				model.setMultipleInheritance(view.isMultipleInheritanceCheckBoxChecked());

				/* This is THE call, the one transforming the file into a bunch of nested objects. */
				model.analyseFile(filename);

				/* Parsing succeeded, let's show the create metrics button. */
				view.enablecreateMetricsFileButton();
			}
			catch (ModelException e)
			{
				/* An exception occurs. Let's see what happened and send info to the user. */

				filename	= null;

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

					case INVALID_FILE:
						sb.append("Une erreur est survenue lors de l'ouverture du fichier.");

						break;

					case MULTIPLE_INHERITANCE_NOT_ALLOWED:
						sb.append("De l'héritage multiple a été détecté alors qu'il n'est présentement pas permis dans les options.");
						break;

					case PARSING_FAILED:
						sb.append("L'interprétation du fichier a échoué. Veuillez vérifier la syntaxe du fichier.");

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

	/**
	 * Called by the view when the create metrics button is clicked. It will create a CSV file containing all the
	 * metrics for all the classes.
	 */
	public void createMetricsFileButtonClicked()
	{
		if((null != model) && (null != filename))
		{
			/* First line = header, all other lines = class name + all metric values */
			String[][] metrics	= model.getMetrics();

			StringBuilder sb	= new StringBuilder();

			int nMetrics	= METRICS.getNumberOfMetrics();

			/* Header line */

			sb.append(quoteString("Class"));

			for(int i = 0; i < nMetrics; ++i)
			{
				sb.append(",");
				sb.append(quoteString(METRICS.getMetricFromIndex(i).toString()));
			}

			sb.append("\r\n");

			/* Class name + metric values, one line per class. */

			for (String[] metric : metrics)
			{
				sb.append(quoteString(metric[0]));

				for(int j = 1; j <= nMetrics; ++j)
				{
					sb.append(",");
					sb.append(quoteString(metric[j]));
				}

				sb.append("\r\n");
			}

			/* To avoid overwritting a file, let's append the current date/time to it. */
			String date			= (new SimpleDateFormat("yyyyMMdd-HHmmss")).format(new Date());
			String outFilename	= filename + "-metrics" + "-" + date + ".csv";

			PrintWriter fileWriter	= null;

			try
			{
				fileWriter	= new PrintWriter(outFilename, "ISO-8859-1");

				fileWriter.print(sb.toString());
			}
			catch (Exception e)
			{
				view.showError("Une erreur est survenue lors de la création du fichier des métriques.");
			}
			finally
			{
				if(null != fileWriter)
				{
					fileWriter.close();
				}
			}

			view.showSuccess("Les métriques ont été enregistrées dans le fichier suivant :\n" + outFilename);
		}
	}

	/**
	 * Called by the view when the user clicks on a class in the list. It will ask the model to send the information of
	 * that class (for example, the model will send the list of attributes, operations, ...)
	 */
	public void classListClicked()
	{
		String className	= view.getSelectedClass();

		model.sendClassInfo(className);
	}

	/**
	 * Called by the view when the user clicks on an association. It asks the model to send the substring in the file
	 * corresponding to the selected association.
	 */
	public void associationListClicked()
	{
		int index	= view.getSelectedAssociationIndex();

		if(0 <= index)
		{
			model.sendAssociationDetails(view.getSelectedClass(), index);
		}
	}

	/**
	 * Called by the view when the user clicks on an aggregation. It asks the model to send the substring in the file
	 * corresponding to the selected aggregation.
	 */
	public void aggregationListClicked()
	{
		int index	= view.getSelectedAggregationIndex();

		if(0 <= index)
		{
			model.sendAggregationDetails(view.getSelectedClass(), index);
		}
	}

	/**
	 * Called by the view when the user clicks on a metric. It sends to the view the description of the selected metric.
	 */
	public void metricListClicked()
	{
		int index = view.getSelectedMetricIndex();

		if((0 <= index) && (index < metricDetails.size()))
		{
			view.setDetails(metricDetails.get(METRICS.getMetricFromIndex(index).toString()));
		}
	}

	/**
	 * Called by the view when the user checks/unchecks the allow multiple inheritance checkbox.
	 */
	public void multipleInheritanceCheckBoxClicked()
	{
		/* Do nothing. We'll check if it is checked when parsing the file. */
	}

	/**
	 * Puts a string between quotes for the generation of the CSV file.
	 *
	 * @param string	The string to be quoted. It is assumed that it does not contain quotes.
	 *
	 * @return	The quoted string.
	 */
	protected String quoteString(String string)
	{
		return "\"" + string + "\"";
	}
}
