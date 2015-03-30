package mvc.views;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Observable;
import java.util.Observer;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import mvc.Controller;
import mvc.models.ListContainer;

public class MainWindow	extends JFrame implements Observer
{
	// PROTECTED PROPERTIES

	/**
	 * The controller (hardcoded to save time).
	 */
	protected Controller controller;

	protected JLabel filenameLabel;

	protected JButton selectFileButton;

	protected JButton parseButton;

	protected JList<String> classList;

	protected JList<String> attributeList;

	protected JList <String> subclassList;

	protected JList <String> superclassList;

	protected JList <String> methodList;

	protected JList<String> associationList;

	protected JList<String> aggregationList;

	protected JLabel detailLabel;

	protected JTextArea detailTextArea;

	protected JComboBox<String> encodingComboBox;

	protected JCheckBox multipleInheritanceCheckBox;

	public MainWindow()
	{
		super("IFT3913 :: TP2 par Hubert Lemelin");

		super.setSize(800, 800);

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		setLayout(new GridBagLayout());

		filenameLabel	= new JLabel();

		filenameLabel.setPreferredSize(new Dimension(450, 20));
		filenameLabel.setText("");
		filenameLabel.setBorder(BorderFactory.createStrokeBorder(new BasicStroke(1)));

		selectFileButton	= new JButton("Sélectionner...");

		JPanel filePanel = new JPanel();

		filePanel.add(filenameLabel, BorderLayout.WEST);
		filePanel.add(selectFileButton, BorderLayout.EAST);
		filePanel.setBorder(BorderFactory.createTitledBorder("Sélection de fichier"));

		GridBagConstraints c = new GridBagConstraints();
		c.fill	= GridBagConstraints.BOTH;
		c.weighty = 0.0;
		c.weightx = 1.0;
		c.gridx = 0;
		c.gridy = 0;

		getContentPane().add(filePanel, c);

		parseButton	= new JButton("Analyser");

		encodingComboBox	= new JComboBox<String>(new String[] {"ISO-8859-1", "UTF-8"});

		multipleInheritanceCheckBox	= new JCheckBox();
		multipleInheritanceCheckBox.setSelected(true);

		JPanel optionPanel = new JPanel();
		optionPanel.add(new JLabel("Encodage :"));
		optionPanel.add(encodingComboBox);
		optionPanel.add(multipleInheritanceCheckBox);
		optionPanel.add(new JLabel("Permettre l'héritage multiple"));
		optionPanel.setBorder(BorderFactory.createTitledBorder("Options"));

		++c.gridy;

		getContentPane().add(optionPanel, c);

		JPanel actionPanel	= new JPanel();
		actionPanel.add(parseButton);
		actionPanel.setBorder(BorderFactory.createTitledBorder("Actions"));

		++c.gridy;

		getContentPane().add(actionPanel, c);

		JPanel mainPanel	= new JPanel(new GridBagLayout());

		GridBagConstraints c1	= new GridBagConstraints();

		c1.fill	= GridBagConstraints.BOTH;

		c1.gridx = 0;
		c1.gridy = 0;
		c1.weightx = (float) 1 / 3;
		c1.weighty = 1.0;
		c1.gridheight = 4;

		mainPanel.setBorder(BorderFactory.createTitledBorder("Contenu"));

		JPanel classPanel	= new JPanel(new BorderLayout());
		classPanel.setBorder(BorderFactory.createTitledBorder("Classes"));

		/*DefaultListModel<String> classes = new DefaultListModel<String>();
		classes.addElement("test");
		classes.addElement("test1");
		classes.addElement("test2");*/

		classList	= new JList<String>();

		classList.setBorder(BorderFactory.createLoweredBevelBorder());

		classList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		classList.setVisibleRowCount(-1);

		//classList.setModel(classes);

		classPanel.add(new JScrollPane(classList));

		mainPanel.add(classPanel, c1);

		JPanel attributePanel	= new JPanel(new BorderLayout());
		attributePanel.setBorder(BorderFactory.createTitledBorder("Attributs"));

		attributeList	= new JList<String>();

		attributeList.setBorder(BorderFactory.createLoweredBevelBorder());

		attributeList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		attributeList.setVisibleRowCount(-1);

		attributePanel.add(new JScrollPane(attributeList));

		c1.gridx = 1;
		c1.weighty = (float) 1 / 4;
		c1.gridheight = 1;

		mainPanel.add(attributePanel, c1);

		JPanel subclassPanel	= new JPanel(new BorderLayout());

		subclassPanel.setBorder(BorderFactory.createTitledBorder("Sous-classes"));

		subclassList	= new JList<String>();

		subclassList.setBorder(BorderFactory.createLoweredBevelBorder());

		subclassList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		subclassList.setVisibleRowCount(-1);

		subclassPanel.add(new JScrollPane(subclassList));

		c1.gridx = 1;
		c1.gridy = 1;

		mainPanel.add(subclassPanel, c1);

		JPanel superclassPanel	= new JPanel(new BorderLayout());

		superclassPanel.setBorder(BorderFactory.createTitledBorder("Super-classes"));

		superclassList	= new JList<String>();

		superclassList.setBorder(BorderFactory.createLoweredBevelBorder());

		superclassList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		superclassList.setVisibleRowCount(-1);

		superclassPanel.add(new JScrollPane(superclassList));

		c1.gridx = 2;
		c1.gridy = 1;

		mainPanel.add(superclassPanel, c1);

		JPanel methodPanel	= new JPanel(new BorderLayout());
		methodPanel.setBorder(BorderFactory.createTitledBorder("Méthodes"));

		methodList	= new JList<String>();

		methodList.setBorder(BorderFactory.createLoweredBevelBorder());

		methodList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		methodList.setVisibleRowCount(-1);

		methodPanel.add(new JScrollPane(methodList));

		c1.gridx = 2;
		c1.gridy = 0;

		mainPanel.add(methodPanel, c1);

		JPanel associationPanel	= new JPanel(new BorderLayout());
		associationPanel.setBorder(BorderFactory.createTitledBorder("Associations"));

		associationList	= new JList<String>();

		associationList.setBorder(BorderFactory.createLoweredBevelBorder());

		associationList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		associationList.setVisibleRowCount(-1);

		associationPanel.add(new JScrollPane(associationList));

		c1.gridx = 1;
		c1.gridy = 2;

		mainPanel.add(associationPanel, c1);

		JPanel aggregationPanel	= new JPanel(new BorderLayout());
		aggregationPanel.setBorder(BorderFactory.createTitledBorder("Agrégations"));

		aggregationList	= new JList<String>();

		aggregationList.setBorder(BorderFactory.createLoweredBevelBorder());

		aggregationList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		aggregationList.setVisibleRowCount(-1);

		aggregationPanel.add(new JScrollPane(aggregationList));

		c1.gridx = 2;
		c1.gridy = 2;

		mainPanel.add(aggregationPanel, c1);

		JPanel detailPanel	= new JPanel(new BorderLayout());
		detailPanel.setBorder(BorderFactory.createTitledBorder("Détails"));

		detailTextArea	= new JTextArea();

		detailTextArea.setEditable(false);

		detailTextArea.setBorder(BorderFactory.createLoweredBevelBorder());

		c1.gridx = 1;
		c1.gridy = 3;
		c1.gridwidth = 2;

		detailPanel.add(new JScrollPane(detailTextArea));

		mainPanel.add(detailPanel, c1);

		++c.gridy;
		c.weighty = 1.0;

		getContentPane().add(mainPanel, c);

		selectFileButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				selectFileButtonClicked();
			}
		});

		parseButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				parseButtonClicked();
			}
		});

		classList.addListSelectionListener(new ListSelectionListener()
		{
			@Override
			public void valueChanged(ListSelectionEvent e)
			{
				if(!e.getValueIsAdjusting() && !classList.isSelectionEmpty())
				{
					classListClicked();
				}
			}
		});

		associationList.addListSelectionListener(new ListSelectionListener()
		{
			@Override
			public void valueChanged(ListSelectionEvent e)
			{
				if(!e.getValueIsAdjusting() && !classList.isSelectionEmpty())
				{
					associationListClicked();
				}
			}
		});

		aggregationList.addListSelectionListener(new ListSelectionListener()
		{
			@Override
			public void valueChanged(ListSelectionEvent e)
			{
				if(!e.getValueIsAdjusting() && !classList.isSelectionEmpty())
				{
					aggregationListClicked();
				}
			}
		});

		setVisible(true);
	}

	// PUBLIC METHODS

	public String getSelectedClass()
	{
		if(null != classList.getSelectedValue())
		{
			return classList.getSelectedValue();
		}

		return null;
	}

	public int getSelectedAssociationIndex()
	{
		return associationList.getSelectedIndex();
	}

	public int getSelectedAggregationIndex()
	{
		return aggregationList.getSelectedIndex();
	}

	public void setController(Controller controller)
	{
		this.controller	= controller;
	}

	public void setFilename(String filename)
	{
		filenameLabel.setText(filename);
	}

	// PROTECTED METHODS

	protected void selectFileButtonClicked()
	{
		if(null != controller)
		{
			controller.selectFileButtonClicked();
		}
	}

	protected void parseButtonClicked()
	{
		if(null != controller)
		{
			controller.parseButtonClicked();
		}
	}

	protected void classListClicked()
	{
		detailTextArea.setText("");

		if(null != controller)
		{
			controller.classListClicked();
		}
	}

	protected void associationListClicked()
	{
		if(null != controller)
		{
			controller.associationListClicked();
		}
	}

	protected void aggregationListClicked()
	{
		if(null != controller)
		{
			controller.aggregationListClicked();
		}
	}

	@Override
	public void update(Observable o, Object arg)
	{
		if(arg instanceof String)
		{
			detailTextArea.setText((String) arg);

			return;
		}

		/*if(arg instanceof mvc.ErrorCenter.Error)
		{
			Error error			= (Error) arg;
			String errorMessage	= "";

			/* Pseudoswitch because Java 6 can't switch on longs and even though it's always the same at the moment,
			 * someone may want to handle each error type differently. * /
			if(error.number == ErrorCenter.INVALID_FILE)
			{
				errorMessage	= error.message;
			}
			else if(error.number == ErrorCenter.PARSING_FAILED)
			{
				errorMessage	= error.message;
			}
			// ...
			else
			{
				errorMessage	= error.message;
			}

			JOptionPane.showMessageDialog(this, errorMessage, "Erreur", JOptionPane.ERROR_MESSAGE);
		}
		else */
		if(arg instanceof ListContainer)
		{
			ListContainer listContainer	= (ListContainer) arg;

			DefaultListModel<String> listModel	= new DefaultListModel<String>();

			for(Object obj : ((ListContainer) arg).getList())
			{
				listModel.addElement(obj.toString());
			}

			if(listContainer.getId() == ListContainer.CLASS_LIST)
			{
				classList.removeAll();
				attributeList.removeAll();
				methodList.removeAll();
				subclassList.removeAll();
				associationList.removeAll();
				detailTextArea.setText("");

				classList.setModel(listModel);
			}
			else if(listContainer.getId() == ListContainer.ATTRIBUTE_LIST)
			{
				attributeList.removeAll();

				attributeList.setModel(listModel);
			}
			else if(listContainer.getId() == ListContainer.OPERATION_LIST)
			{
				methodList.removeAll();

				methodList.setModel(listModel);
			}
			else if(listContainer.getId() == ListContainer.SUBCLASS_LIST)
			{
				subclassList.removeAll();

				subclassList.setModel(listModel);
			}
			else if(listContainer.getId() == ListContainer.SUPERCLASS_LIST)
			{
				superclassList.removeAll();

				superclassList.setModel(listModel);
			}
			else if(listContainer.getId() == ListContainer.ASSOCIATION_LIST)
			{
				associationList.removeAll();

				associationList.setModel(listModel);
			}
			else if(listContainer.getId() == ListContainer.AGGREGATION_LIST)
			{
				aggregationList.removeAll();

				aggregationList.setModel(listModel);
			}
		}
	}

	public void showError(String errorMessage)
	{
		// TODO reset lists
		JOptionPane.showMessageDialog(this, errorMessage, "Erreur", JOptionPane.ERROR_MESSAGE);
	}
}
