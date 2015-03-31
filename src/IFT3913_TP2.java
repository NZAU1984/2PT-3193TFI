import java.io.IOException;

import mvc.Controller;
import mvc.models.Model;
import mvc.views.MainWindow;

/**
 * Main class which simply links the MVC components together.
 *
 * @author Hubert Lemelin
 *
 */
public class IFT3913_TP2
{
	public static void main(String[] args) throws IOException
	{
		MainWindow view = new MainWindow();

		Model model	= new Model();

		model.addObserver(view);

		Controller controller	= new Controller(model, view);

		view.setController(controller);
	}
}
