package peakaboo.ui.swing.plotting;



import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.stream.Collectors;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.ScrollPaneConstants;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import commonenvironment.Apps;
import commonenvironment.Env;
import eventful.EventfulListener;
import eventful.EventfulTypeListener;
import net.sciencestudio.bolt.plugin.core.BoltPluginController;
import net.sciencestudio.bolt.plugin.core.BoltPluginSet;
import peakaboo.common.Configuration;
import peakaboo.common.PeakabooLog;
import peakaboo.common.Version;
import peakaboo.controller.mapper.data.MapSetController;
import peakaboo.controller.mapper.settings.MapViewSettings;
import peakaboo.controller.plotter.PlotController;
import peakaboo.controller.plotter.fitting.AutoEnergyCalibration;
import peakaboo.controller.plotter.fitting.TSOrdering;
import peakaboo.controller.plotter.settings.ChannelCompositeMode;
import peakaboo.curvefit.fitting.EnergyCalibration;
import peakaboo.curvefit.transition.EscapePeakType;
import peakaboo.curvefit.transition.TransitionSeries;
import peakaboo.dataset.DatasetReadResult;
import peakaboo.dataset.DatasetReadResult.ReadStatus;
import peakaboo.datasink.model.DataSink;
import peakaboo.datasink.plugin.DataSinkLoader;
import peakaboo.datasink.plugin.DataSinkPlugin;
import peakaboo.datasource.model.DataSource;
import peakaboo.datasource.model.components.fileformat.FileFormat;
import peakaboo.datasource.model.components.metadata.Metadata;
import peakaboo.datasource.model.components.physicalsize.PhysicalSize;
import peakaboo.datasource.model.components.scandata.ScanData;
import peakaboo.datasource.plugin.DataSourceLoader;
import peakaboo.datasource.plugin.DataSourceLookup;
import peakaboo.datasource.plugin.DataSourcePlugin;
import peakaboo.filter.model.FilterLoader;
import peakaboo.filter.model.FilterSet;
import peakaboo.mapping.FittingTransform;
import peakaboo.mapping.results.MapResultSet;
import peakaboo.ui.swing.Peakaboo;
import peakaboo.ui.swing.mapping.MapperFrame;
import peakaboo.ui.swing.misc.PluginData;
import peakaboo.ui.swing.plotting.datasource.DataSourceSelection;
import peakaboo.ui.swing.plotting.filters.FiltersetViewer;
import peakaboo.ui.swing.plotting.fitting.CurveFittingView;
import peakaboo.ui.swing.plotting.tabbed.TabbedPlotterManager;
import plural.executor.DummyExecutor;
import plural.executor.ExecutorSet;
import plural.streams.StreamExecutor;
import plural.streams.StreamExecutorSet;
import plural.streams.swing.StreamExecutorPanel;
import plural.streams.swing.StreamExecutorView;
import plural.swing.ExecutorSetView;
import plural.swing.ExecutorSetViewDialog;
import plural.swing.ExecutorView;
import scidraw.swing.SavePicture;
import scitypes.Bounds;
import scitypes.Coord;
import scitypes.Pair;
import scitypes.ReadOnlySpectrum;
import scitypes.SISize;
import scitypes.SigDigits;
import scitypes.util.Mutable;
import scitypes.util.StringInput;
import swidget.dialogues.AboutDialogue;
import swidget.dialogues.PropertyDialogue;
import swidget.dialogues.fileio.SimpleFileExtension;
import swidget.dialogues.fileio.SwidgetFilePanels;
import swidget.icons.IconFactory;
import swidget.icons.IconSize;
import swidget.icons.StockIcon;
import swidget.widgets.ButtonBox;
import swidget.widgets.ClearPanel;
import swidget.widgets.ComponentListPanel;
import swidget.widgets.DraggingScrollPaneListener;
import swidget.widgets.DraggingScrollPaneListener.Buttons;
import swidget.widgets.DropdownImageButton;
import swidget.widgets.ImageButton;
import swidget.widgets.Spacing;
import swidget.widgets.ToolbarImageButton;
import swidget.widgets.ZoomSlider;
import swidget.widgets.properties.PropertyViewPanel;
import swidget.widgets.tabbedinterface.TabbedInterfacePanel;
import swidget.widgets.toggle.ImageToggleButton;
import swidget.widgets.toggle.ItemToggleButton;



public class PlotPanel extends TabbedInterfacePanel
{

	private TabbedPlotterManager 	container;


	//Non-UI
	PlotController				controller;
	PlotCanvas					canvas;
	File						saveFilesFolder;
	File						savedSessionFileName;
	File						exportedDataFileName;
	File						datasetFolder;
	String                      programTitle;

	
	
	//===MAIN MENU WIDGETS===
	
	//FILE
	JMenuItem					snapshotMenuItem;
	JMenuItem					exportFittingsMenuItem;
	JMenuItem					exportFilteredDataMenuItem;
	JMenuItem					saveSession;
	JMenu 						exportSinks;

	//EDIT
	JMenuItem					undo, redo;


	JSpinner					scanNo;
	JLabel						scanLabel;
	JToggleButton				scanBlock;
	JLabel						channelLabel;

	//===TOOLBAR WIDGETS===
	JToolBar                    toolBar;
	
	//===PLOTTING UI WIDGETS===
	JSpinner					minEnergy, maxEnergy;
	ImageButton					toolbarSnapshot;
	DropdownImageButton			toolbarMap;
	ImageButton					toolbarInfo;
	ImageButton					energyGuess;
	ZoomSlider					zoomSlider;
	JPanel						bottomPanel;
	JPanel						scanSelector;
	JScrollPane					scrolledCanvas;


	public PlotPanel(TabbedPlotterManager container)
	{
		this.container = container;
		this.programTitle = " - " + Version.title;
		
		savedSessionFileName = null;
		exportedDataFileName = null;
		
		datasetFolder = Env.homeDirectory();

		controller = new PlotController();
				

		initGUI();

		controller.addListener(new EventfulTypeListener<String>() {

			public void change(String message)
			{
				setWidgetsState();
			}
		});

		setWidgetsState();



	}
	
	public String getProgramTitle()
	{
		return programTitle;
	}
	
	public void setProgramTitle(String title)
	{
		programTitle = title;
	}

	public PlotController getController()
	{
		return controller;
	}


	private void setWidgetsState()
	{

		boolean hasData = controller.data().hasDataSet();
		
		bottomPanel.setEnabled(hasData);
		snapshotMenuItem.setEnabled(hasData);
		exportFittingsMenuItem.setEnabled(hasData);
		exportFilteredDataMenuItem.setEnabled(hasData);
		toolbarSnapshot.setEnabled(hasData);
		exportSinks.setEnabled(hasData);
		energyGuess.setEnabled(hasData);
		toolbarInfo.setEnabled(hasData);
		
		if (hasData) {
			toolbarMap.setEnabled(controller.fitting().canMap() && controller.data().getDataSet().getDataSource().isContiguous());
			setEnergySpinners();

			if (controller.settings().getChannelCompositeType() == ChannelCompositeMode.NONE) {
				scanNo.setValue(controller.settings().getScanNumber() + 1);
				scanBlock.setSelected(controller.data().getDiscards().isDiscarded(controller.settings().getScanNumber()));
				scanSelector.setEnabled(true);
			} else {
				scanSelector.setEnabled(false);
			}

		}


		undo.setEnabled(controller.history().canUndo());
		redo.setEnabled(controller.history().canRedo());
		undo.setText("Undo " + controller.history().getNextUndo());
		redo.setText("Redo " + controller.history().getNextRedo());

		zoomSlider.setValueEventless((int)(controller.settings().getZoom()*100));
		setTitleBar();

		container.getWindow().validate();
		container.getWindow().repaint();

	}


	private void setEnergySpinners()
	{
		//dont let the listeners get wind of this change		
		minEnergy.setValue((double) controller.settings().getMinEnergy());
		maxEnergy.setValue((double) controller.settings().getMaxEnergy());
	}


	private void initGUI()
	{

		canvas = new PlotCanvas(controller, this);
		canvas.setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
		

		channelLabel = new JLabel("");
		channelLabel.setHorizontalAlignment(SwingConstants.CENTER);
		channelLabel.setFont(channelLabel.getFont().deriveFont(Font.PLAIN));

		canvas.addMouseMotionListener(new MouseMotionListener() {

			public void mouseDragged(MouseEvent e){}


			public void mouseMoved(MouseEvent e)
			{
				mouseMoveCanvasEvent(e.getX());
			}

		});



		
		Container pane = this.getContentLayer();

		GridBagLayout layout = new GridBagLayout();
		GridBagConstraints c = new GridBagConstraints();

		pane.setLayout(layout);

		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 3;
		c.weightx = 1.0;
		c.weighty = 0.0;
		c.fill = GridBagConstraints.HORIZONTAL;
		toolBar = new JToolBar();
		toolBar.setFloatable(false);
		populateToolbar(toolBar);
		pane.add(toolBar, c);

		JPanel p = new JPanel();
		p.setPreferredSize(new Dimension(1000, 100));
		
		scrolledCanvas = new JScrollPane(canvas);
		scrolledCanvas.setAutoscrolls(true);
		scrolledCanvas.setBorder(Spacing.bNone());
		
		
		scrolledCanvas.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scrolledCanvas.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		new DraggingScrollPaneListener(scrolledCanvas.getViewport(), canvas, Buttons.LEFT, Buttons.MIDDLE);

		JPanel canvasPanel = new JPanel(new BorderLayout());
		canvasPanel.add(scrolledCanvas, BorderLayout.CENTER);
		canvasPanel.add(createBottomBar(), BorderLayout.SOUTH);
		canvasPanel.setPreferredSize(new Dimension(600, 300));

		canvasPanel.addComponentListener(new ComponentListener() {
			
			public void componentShown(ComponentEvent e){}
					
			public void componentResized(ComponentEvent e)
			{
				canvas.updateCanvasSize();
			}
			
			public void componentMoved(ComponentEvent e){}
			
			public void componentHidden(ComponentEvent e){}
		});
		
		JTabbedPane tabs = new JTabbedPane();
		tabs.add(new CurveFittingView(controller.fitting(), controller, canvas), 0);
		tabs.add(new FiltersetViewer(controller.filtering(), container.getWindow()), 1);
		
		
		c.gridx = 0;
		c.gridy = 1;
		c.gridwidth = 1;
		c.weightx = 1.0;
		c.weighty = 1.0;
		c.fill = GridBagConstraints.BOTH;
		JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, tabs, canvasPanel);
		split.setResizeWeight(0);
		split.setOneTouchExpandable(true);
		split.setBorder(Spacing.bNone());
		pane.add(split, c);

		
		//createMenu();


	}


	private void setTitleBar()
	{
		container.setTitle(this, getTitleBarString());
	}


	private String getTitleBarString()
	{
		StringBuffer titleString;
		titleString = new StringBuffer();
		
		if (controller.data().hasDataSet())
		{
			titleString.append(controller.data().getDataSet().getScanData().datasetName());
			titleString.append(programTitle);
		} else {
			titleString.append("No Data");
			titleString.append(programTitle);
		}

		
		return titleString.toString();
	}


	private void populateToolbar(JToolBar toolbar)
	{

		toolbar.setLayout(new GridBagLayout());

		GridBagConstraints c = new GridBagConstraints();

		c.gridx = 0;
		c.gridy = 0;
		c.weightx = 0;
		c.weighty = 0;
		c.insets = new Insets(2, 2, 2, 2);
		c.fill = GridBagConstraints.NONE;

		ImageButton ibutton = new ToolbarImageButton("document-open", "Open", "Open a new data set");
		ibutton.addActionListener(e -> actionOpenData());
		toolbar.add(ibutton, c);

		// controls.add(button);

		toolbarSnapshot = new ToolbarImageButton(
			StockIcon.DEVICE_CAMERA,
			"Save Image",
			"Save a picture of the current plot");
		toolbarSnapshot.addActionListener(e -> actionSavePicture());
		c.gridx += 1;
		toolbar.add(toolbarSnapshot, c);

		toolbarInfo = new ToolbarImageButton(
			StockIcon.BADGE_INFO,
			"Scan Info",
			"Displays extended information about this data set");
		toolbarInfo.addActionListener(e -> actionShowInfo());
		c.gridx += 1;
		toolbarInfo.setEnabled(false);
		toolbar.add(toolbarInfo, c);

		JPopupMenu mapMenu = new JPopupMenu();
		populateFittingMenu(mapMenu);
		
		
		toolbarMap = new DropdownImageButton("map", "Map Fittings", "Display a 2D map of the relative intensities of the fitted elements", IconSize.TOOLBAR_SMALL, ToolbarImageButton.significantLayout, mapMenu);
		toolbarMap.addListener(message -> {
			switch (message)
			{
				case MAIN: 
					actionMap(FittingTransform.AREA);
					break;
				case MENU:
					break;
			}
		});
		
		c.gridx += 1;
		toolbarMap.setEnabled(false);
		toolbar.add(toolbarMap, c);

		c.gridx += 1;
		c.weightx = 1.0;
		toolbar.add(Box.createHorizontalGlue(), c);
		c.weightx = 0.0;


		JPanel energyControls = new ClearPanel();
		energyControls.setBorder(new EmptyBorder(0, 0, 0, Spacing.huge*2));
		energyControls.setLayout(new GridBagLayout());
		GridBagConstraints c2 = new GridBagConstraints();
		c2.gridx = 0;
		c2.gridy = 0;
		c2.weightx = 0;
		c2.weighty = 0;
		c2.fill = GridBagConstraints.NONE;
		c2.anchor = GridBagConstraints.EAST;

		JLabel energyLabel = new JLabel("Energy (keV) ");
		energyControls.add(energyLabel, c2);
		c2.gridx += 1;
		
		minEnergy = new JSpinner();
		minEnergy.setModel(new SpinnerNumberModel(0.0, -20.48, 20.48, 0.01));
		
		maxEnergy = new JSpinner();
		maxEnergy.setModel(new SpinnerNumberModel(20.48, 0.0, 204.8, 0.01));

		
		energyControls.add(minEnergy, c2);
		c2.gridx += 1;
		energyControls.add(new JLabel(" to "), c2);
		c2.gridx += 1;
		energyControls.add(maxEnergy, c2);
		c2.gridx += 1;
		
		minEnergy.addChangeListener(e -> {
			float min = ((Number) minEnergy.getValue()).floatValue();
			if (min > controller.settings().getMaxEnergy()) {
				min = controller.settings().getMaxEnergy() - 0.01f;
				minEnergy.setValue(min);
			} 
			controller.settings().setMinEnergy(min);	
		});
		maxEnergy.addChangeListener(e -> {
			float max = ((Number) maxEnergy.getValue()).floatValue();
			if (max < controller.settings().getMinEnergy()) {
				max = controller.settings().getMinEnergy() + 0.01f;
				maxEnergy.setValue(max);
			} 
			controller.settings().setMaxEnergy(max);
		});
		
		
		energyGuess = new ToolbarImageButton("auto", "", "Try to detect the correct max energy value by matching fittings to strong signal. Use with care.");
		energyControls.add(energyGuess, c2);
		energyGuess.addActionListener(e -> {
			actionGuessMaxEnergy();
		});
		
		c.gridx += 1;		
		toolbar.add(energyControls, c);

		
		c.gridx++;
		toolbar.add(createMenuButton(), c);
		

	}

	
	private void populateFittingMenu(JComponent menu)
	{
		final JMenuItem mapArea = createMenuItem(
				"Map Fitting Area", null, null, 
				e -> actionMap(FittingTransform.AREA),
				null, null
		);
		mapArea.setEnabled(false);
		menu.add(mapArea);
		
		
		final JMenuItem mapHeights = createMenuItem(
				"Map Fitting Heights", null, null, 
				e -> actionMap(FittingTransform.HEIGHT),
				null, null
		);
		mapHeights.setEnabled(false);
		menu.add(mapHeights);
		
		
		controller.addListener(message -> {
			mapHeights.setEnabled(controller.fitting().canMap());
			mapArea.setEnabled(controller.fitting().canMap());
		});
		
	}

	
	private JCheckBoxMenuItem createMenuCheckItem(String title, ImageIcon icon, String description, Consumer<Boolean> listener, KeyStroke key, Integer mnemonic)
	{
		
		JCheckBoxMenuItem menuItem;
		if (icon != null) {
			menuItem = new JCheckBoxMenuItem(title, icon);
		} else {
			menuItem = new JCheckBoxMenuItem(title);
		}
		
		Consumer<ActionEvent> checkListener = e -> {
			boolean orig = menuItem.isSelected();
			if (e.getSource() == menuItem) {
				orig = !orig;
			}
			menuItem.setSelected(!orig);
			listener.accept(!orig);
		};
		
		configureMenuItem(menuItem, description, checkListener, key, mnemonic);
		
		return menuItem;
		
	}
	
	private JRadioButtonMenuItem createMenuRadioItem(String title, ImageIcon icon, String description, Consumer<ActionEvent> listener, KeyStroke key, Integer mnemonic)
	{
		
		JRadioButtonMenuItem menuItem;
		if (icon != null) {
			menuItem = new JRadioButtonMenuItem(title, icon);
		} else {
			menuItem = new JRadioButtonMenuItem(title);
		}
		
		Consumer<ActionEvent> checkListener = e -> {
			menuItem.setSelected(true);
			listener.accept(e);
		};
		
		configureMenuItem(menuItem, description, checkListener, key, mnemonic);
		
		return menuItem;
		
	}
	
	private JMenuItem createMenuItem(String title, ImageIcon icon, String description, Consumer<ActionEvent> listener, KeyStroke key, Integer mnemonic)
	{
		JMenuItem menuItem;
		if (icon != null) {
			menuItem = new JMenuItem(title, icon);
		} else {
			menuItem = new JMenuItem(title);
		}
		
		configureMenuItem(menuItem, description, listener, key, mnemonic);
		
		return menuItem;
		
	}
	
	private void configureMenuItem(JMenuItem menuItem, String description, Consumer<ActionEvent> listener, KeyStroke key, Integer mnemonic)
	{
		
		
			
		Action action = new AbstractAction() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				listener.accept(e);
			}
		};

			
		//You'd think this would fail with tabs because they'd both try to handle the
		//key event, but it actually works perfectly. Maybe the tab component itself 
		//redirects input to only the focused tab?
		if (key != null) {
			this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(key, key.toString());
			this.getActionMap().put(key.toString(), action);
		}
		
		
		
		//Even though this isn't how actions are performed anymore, we still want it to show up
		if (key != null) menuItem.setAccelerator(key);
		
		if (mnemonic != null) menuItem.setMnemonic(mnemonic);
		if (description != null) menuItem.getAccessibleContext().setAccessibleDescription(description);
		if (description != null) menuItem.setToolTipText(description);
		if (listener != null) menuItem.addActionListener(e -> listener.accept(e));
	}
	
	private List<JMenu> createMenus()
	{

		JMenu menu;

		List<JMenu> menuBar = new ArrayList<>();

		

		// FILE Menu
		menu = new JMenu("File");
		menu.setMnemonic(KeyEvent.VK_F);
		menu.getAccessibleContext().setAccessibleDescription("Read and Write Data Sets");

		
		menu.add(createMenuItem(
				"Open Data\u2026", StockIcon.DOCUMENT_OPEN.toMenuIcon(), "Opens new data sets.",
				e -> actionOpenData(),
				KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.CTRL_MASK), KeyEvent.VK_O
		));
		
		
		menu.add(createMenuItem(
				"Save Session", StockIcon.DOCUMENT_SAVE.toMenuIcon(), null, 
				e -> actionSaveSession(),
				null, null
		));
		
		menu.add(createMenuItem(
				"Load Session", null, null,
				e -> actionLoadSession(),
				null, null
		));
		

		
		JMenu export = new JMenu("Export");
		
		exportSinks = new JMenu("Raw Data");
		
		for (BoltPluginController<? extends DataSinkPlugin> plugin : DataSinkLoader.getPluginSet().getAll()) {
			exportSinks.add(createMenuItem(
					plugin.getName(), null, null,
					e -> actionExportData(plugin.create()),
					null, null
			));
		}
		
		export.add(exportSinks);
		

		
		snapshotMenuItem = createMenuItem(
				"Plot as Image\u2026", StockIcon.DEVICE_CAMERA.toMenuIcon(), "Saves the current plot as an image",
				e -> actionSavePicture(),
				KeyStroke.getKeyStroke(KeyEvent.VK_P, ActionEvent.CTRL_MASK), KeyEvent.VK_P
		);
		export.add(snapshotMenuItem);
		
		
		exportFilteredDataMenuItem = createMenuItem(
				"Filtered Data as Text", StockIcon.DOCUMENT_EXPORT.toMenuIcon(), "Saves the filtered data to a text file",
				e -> actionSaveFilteredData(),
				null, null
		);
		export.add(exportFilteredDataMenuItem);
		
		exportFittingsMenuItem = createMenuItem(
				"Fittings as Text", null, "Saves the current fitting data to a text file",
				e -> actionSaveFittingInformation(),
				null, null
		);
		export.add(exportFittingsMenuItem);


		menu.add(export);
		
		
		JMenu plugins = new JMenu("Plugins");

		plugins.add(createMenuItem(
				"Status", null, "Shows information about loaded plugins",
				e -> {
					
					JTabbedPane tabs = new JTabbedPane();

					ComponentListPanel dsPanel = new ComponentListPanel(
						DataSourceLoader
						.getPluginSet()
						.getAll()
						.stream()
						.map(PluginData::new)
						.collect(Collectors.toList())
					);
					
					
					ComponentListPanel filterPanel = new ComponentListPanel(
						FilterLoader
						.getPluginSet()
						.getAll()
						.stream()
						.map(PluginData::new)
						.collect(Collectors.toList())
					);
					
					
					tabs.add("Data Sources", dsPanel);
					tabs.add("Filters", filterPanel);


					
					JDialog dialog = new JDialog(container.getWindow()); //TODO: owner
					dialog.setContentPane(tabs);
					
					dialog.setResizable(false);
					dialog.setPreferredSize(new Dimension(535, 500));
					dialog.setMinimumSize(dialog.getPreferredSize());
					dialog.setTitle("Peakaboo Plugins");
					dialog.setModal(false);
					dialog.setLocationRelativeTo(container.getWindow());
					dialog.setVisible(true);
					
				},
				null, null
		));	
		
		plugins.add(createMenuItem(
				"Open Folder", null, "Opens the plugins folder to add or remove plugin files",
				e -> actionOpenPluginFolder(),null, null));
		
	
		
		menu.add(plugins);
		
		
		
//		menu.add(createMenuItem(
//				"Exit", StockIcon.WINDOW_CLOSE.toMenuIcon(), "Exits the Program",
//				e -> System.exit(0),
//				null, KeyEvent.VK_X
//		));

		menuBar.add(menu);









		// EDIT Menu
		menu = new JMenu("Edit");
		menu.setMnemonic(KeyEvent.VK_E);
		menu.getAccessibleContext().setAccessibleDescription("Edit this data set");


		
		undo = createMenuItem(
				"Undo", StockIcon.EDIT_UNDO.toMenuIcon(), "Undoes a previous action",
				e -> controller.history().undo(),
				KeyStroke.getKeyStroke(KeyEvent.VK_Z, ActionEvent.CTRL_MASK), KeyEvent.VK_U
		);
		menu.add(undo);

		redo = createMenuItem(
				"Redo", StockIcon.EDIT_REDO.toMenuIcon(), "Redoes a previously undone action",
				e -> controller.history().redo(),
				KeyStroke.getKeyStroke(KeyEvent.VK_Y, ActionEvent.CTRL_MASK), KeyEvent.VK_R
		);
		menu.add(redo);

		menuBar.add(menu);






		// VIEW Menu
		menu = new JMenu("View");
		menu.setMnemonic(KeyEvent.VK_V);
		menu.getAccessibleContext().setAccessibleDescription("Change the way the plot is viewed");
		menuBar.add(menu);

		final JMenuItem logPlot, axes, monochrome, title, raw, fittings;

		
		logPlot = createMenuCheckItem(
				"Logarithmic Scale", null, "Toggles the plot between a linear and logarithmic scale",
				b -> {
					controller.settings().setViewLog(b);
				},
				KeyStroke.getKeyStroke(KeyEvent.VK_L, ActionEvent.CTRL_MASK), KeyEvent.VK_L
		);
		
		axes = createMenuCheckItem(
				"Axes", null, "Toggles display of axes and grid lines",
				b -> {
					controller.settings().setShowAxes(b);
				},
				null, null
		);

		title = createMenuCheckItem(
				"Title", null, "Toggles display of the current data set's title",
				b -> {
					controller.settings().setShowTitle(b);
				},
				null, null
		);

		monochrome = createMenuCheckItem(
				"Monochrome", null, "Toggles the monochrome colour palette",
				b -> {
					controller.settings().setMonochrome(b);
				},
				null, KeyEvent.VK_M
		);
		
		

		raw = createMenuCheckItem(
				"Raw Data Outline", null, "Toggles an outline of the original raw data",
				b -> {
					controller.settings().setShowRawData(b);
				},
				null, KeyEvent.VK_O
		);
		
		fittings = createMenuCheckItem(
				"Individual Fittings", null, "Switches between showing all fittings as a single curve and showing all fittings individually",
				b -> {
					controller.settings().setShowIndividualSelections(b);
				},
				null, KeyEvent.VK_O
		);	
		
		menu.add(logPlot);
		menu.add(axes);
		menu.add(title);
		menu.add(monochrome);
		
		menu.addSeparator();
		
		menu.add(raw);
		menu.add(fittings);


		// Element Drawing submenu
		JMenu elementDrawing = new JMenu("Curve Fit");
		final JCheckBoxMenuItem etitles, emarkings, eintensities;

		
		etitles = createMenuCheckItem(
				"Element Names", null, "Label fittings with the names of their elements",
				b -> {
					controller.settings().setShowElementTitles(b);
				},
				null, null
		);
		elementDrawing.add(etitles);

		
		emarkings = createMenuCheckItem(
				"Markings", null, "Label fittings with lines denoting their energies",
				b -> {
					controller.settings().setShowElementMarkers(b);
				},
				null, null
		);
		elementDrawing.add(emarkings);

		
		eintensities = createMenuCheckItem(
				"Heights", null, "Label fittings with their heights",
				b -> {
					controller.settings().setShowElementIntensities(b);
				},
				null, null
		);
		elementDrawing.add(eintensities);

		
		menu.add(elementDrawing);
		
		
		
		
		JMenu escapePeaks = new JMenu("Escape Peaks");
		
		
		final ButtonGroup escapePeakGroup = new ButtonGroup();
		
		for (EscapePeakType t : EscapePeakType.values())
		{
			final JRadioButtonMenuItem escapeItem = new JRadioButtonMenuItem(t.show());
			escapePeakGroup.add(escapeItem);
			escapePeaks.add(escapeItem);
			if (t == EscapePeakType.SILICON) escapeItem.setSelected(true);
			
			
			final EscapePeakType finalt = t;
			
			escapeItem.addActionListener(e -> {
				escapeItem.setSelected(true);
				controller.settings().setEscapePeakType(finalt);
			});
			
			controller.addListener(message -> {
				escapeItem.setSelected( controller.settings().getEscapePeakType() == finalt );
			});

		}
		
		menu.add(escapePeaks);
		

		menu.addSeparator();
		


		final JRadioButtonMenuItem individual, average, maximum;

		ButtonGroup viewGroup = new ButtonGroup();

		individual = createMenuRadioItem(
				ChannelCompositeMode.NONE.show(), 
				null, null, 
				o -> controller.settings().setShowChannelMode(ChannelCompositeMode.NONE), 
				KeyStroke.getKeyStroke(KeyEvent.VK_I, ActionEvent.CTRL_MASK), 
				KeyEvent.VK_I
			);
		viewGroup.add(individual);
		menu.add(individual);
		

		average = createMenuRadioItem(
				ChannelCompositeMode.AVERAGE.show(), 
				null, null, 
				o -> controller.settings().setShowChannelMode(ChannelCompositeMode.AVERAGE), 
				KeyStroke.getKeyStroke(KeyEvent.VK_M, ActionEvent.CTRL_MASK), 
				KeyEvent.VK_M
			);
		viewGroup.add(average);
		menu.add(average);
		

		maximum = createMenuRadioItem(
				ChannelCompositeMode.MAXIMUM.show(), 
				null, null, 
				o -> controller.settings().setShowChannelMode(ChannelCompositeMode.MAXIMUM),
				KeyStroke.getKeyStroke(KeyEvent.VK_T, ActionEvent.CTRL_MASK), 
				KeyEvent.VK_T
			);
		viewGroup.add(maximum);
		menu.add(maximum);
		
		
		

		
		
//		//Mapping Menu
//		menu = new JMenu("Mapping");
//		menu.setMnemonic(KeyEvent.VK_S);
//
//
//		populateFittingMenu(menu);
//
//		menuBar.add(menu);
//		
		
		
		
		
		
		//HELP Menu
		menu = new JMenu("Help");
		menu.setMnemonic(KeyEvent.VK_H);
		
		JMenuItem logs = createMenuItem(
				"Logs", null, null,
				e -> actionShowLogs(),
				null, null
			);
		menu.add(logs);
		
		JMenuItem contents = createMenuItem(
			"Help", StockIcon.BADGE_HELP.toMenuIcon(), null,
			e -> actionHelp(),
			KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0), null
		);
		menu.add(contents);
		
		JMenuItem about = createMenuItem(
			"About", StockIcon.MISC_ABOUT.toMenuIcon(), null,
			e -> actionAbout(),
			null, null
		);
		menu.add(about);
		
		
		
		menuBar.add(menu);
		

		controller.addListener(new EventfulTypeListener<String>() {

			public void change(String s)
			{

				logPlot.setSelected(controller.settings().getViewLog());
				axes.setSelected(controller.settings().getShowAxes());
				title.setSelected(controller.settings().getShowTitle());
				monochrome.setSelected(controller.settings().getMonochrome());

				etitles.setSelected(controller.settings().getShowElementTitles());
				emarkings.setSelected(controller.settings().getShowElementMarkers());
				eintensities.setSelected(controller.settings().getShowElementIntensities());

				switch (controller.settings().getChannelCompositeType())
				{

					case NONE:
						individual.setSelected(true);
						break;
					case AVERAGE:
						average.setSelected(true);
						break;
					case MAXIMUM:
						maximum.setSelected(true);
						break;

				}

				raw.setSelected(controller.settings().getShowRawData());
				fittings.setSelected(controller.settings().getShowIndividualSelections());

			}
		});
		
		
		return menuBar;
	}

	
	private ToolbarImageButton createMenuButton() {
		ToolbarImageButton menuButton = new ToolbarImageButton(StockIcon.ACTION_MENU, "Main Menu");
		JPopupMenu mainMenu = new JPopupMenu();

		boolean first = true;
		for (JMenu menu : createMenus()) {
			
			if (!first) { mainMenu.addSeparator(); }
			first = false;
			
			if (menu.getText() == "View") {
				mainMenu.add(menu);
			} else {
				for (Component item : menu.getMenuComponents()) {
					mainMenu.add(item);
				}
			}
			
		}

		
		menuButton.addActionListener(e -> mainMenu.show(menuButton, (int)(menuButton.getWidth() - mainMenu.getPreferredSize().getWidth()), menuButton.getHeight()));
		return menuButton;
	}


	private JPanel createBottomBar()
	{
		bottomPanel = new ClearPanel();
		bottomPanel.setBorder(Spacing.bTiny());
		bottomPanel.setLayout(new BorderLayout());

		channelLabel.setBorder(Spacing.bSmall());
		bottomPanel.add(channelLabel, BorderLayout.CENTER);

		JPanel zoomPanel = createZoomPanel();
		bottomPanel.add(zoomPanel, BorderLayout.EAST);

		scanSelector = new ClearPanel();
		scanSelector.setLayout(new BoxLayout(scanSelector, BoxLayout.X_AXIS));

		scanNo = new JSpinner();
		scanNo.getEditor().setPreferredSize(new Dimension(50, 0));
		scanLabel = new JLabel("Scan");
		scanLabel.setBorder(Spacing.bSmall());
		scanBlock = new ItemToggleButton(
			StockIcon.CHOOSE_CANCEL,
			"Flag this scan to exclude it and extrapolate it from neighbouring points in maps", "");

		scanSelector.add(scanLabel);
		scanSelector.add(Box.createHorizontalStrut(2));
		scanSelector.add(scanNo);
		scanSelector.add(Box.createHorizontalStrut(4));
		scanSelector.add(scanBlock);
		scanSelector.add(Box.createHorizontalStrut(4));

		scanNo.addChangeListener(new ChangeListener() {

			public void stateChanged(ChangeEvent e)
			{
				JSpinner scan = (JSpinner) e.getSource();
				int value = (Integer) ((scan).getValue());
				controller.settings().setScanNumber(value - 1);
			}
		});
		bottomPanel.add(scanSelector, BorderLayout.WEST);

		scanBlock.setFocusable(false);
		scanBlock.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e)
			{
				if (scanBlock.isSelected()) {
					controller.data().getDiscards().discard(controller.settings().getScanNumber());
				} else {
					controller.data().getDiscards().undiscard(controller.settings().getScanNumber());
				}
			}
		});

		return bottomPanel;

	}

	private JPanel createZoomPanel()
	{
		
		JPanel zoomPanel = new JPanel(new BorderLayout());
		
		zoomSlider = new ZoomSlider(10, 1000, 10);
		zoomSlider.setValue(100);
		zoomSlider.addListener(new EventfulListener() {
			
			public void change()
			{
				controller.settings().setZoom(zoomSlider.getValue() / 100f);
			}
		});
		zoomPanel.add(zoomSlider, BorderLayout.CENTER);

		
		final ImageToggleButton lockHorizontal = new ImageToggleButton(StockIcon.MISC_LOCKED, "", "Lock Vertical Zoom to Window Size");
		lockHorizontal.setSelected(true);
		lockHorizontal.addActionListener(e -> {
			controller.settings().setLockPlotHeight(lockHorizontal.isSelected());
		});
		zoomPanel.add(lockHorizontal, BorderLayout.EAST);
		
		return zoomPanel;
	}
	

	// prompts the user with a file selection dialogue
	// reads the returned file list, loads the related
	// data set, and returns it to the caller
	public void openNewDataset(List<SimpleFileExtension> extensions)
	{
		SwidgetFilePanels.openFiles(this, "Select Data Files to Open", datasetFolder, extensions, files -> {
			if (!files.isPresent()) return;
			datasetFolder = files.get().get(0).getParentFile();
			loadFiles(files.get());
		});
	}


	private void mouseMoveCanvasEvent(int x)
	{

		int channel = canvas.channelFromCoordinate(x);
		float energy = controller.settings().getEnergyForChannel(channel);
		Pair<Float, Float> values = controller.settings().getValueForChannel(channel);

		StringBuilder sb = new StringBuilder();
		String sep = ",  ";

		if (values != null)
		{

			DecimalFormat fmtObj = new DecimalFormat("#######0.00");
			
			sb.append("View: ");
			sb.append(controller.settings().getChannelCompositeType().show());
			sb.append(sep);
			sb.append("Channel: ");
			sb.append(String.valueOf(channel));
			sb.append(sep);
			sb.append("Energy: ");
			sb.append(fmtObj.format(energy));
			sb.append(sep);
			sb.append("Value: ");
			sb.append(fmtObj.format(values.first));
			if (! values.first.equals(values.second)) {
				sb.append(sep);
				sb.append("Unfiltered Value: ");
				sb.append(fmtObj.format(values.second));
			}

		}
		else
		{
			
			sb.append("View: ");
			sb.append(controller.settings().getChannelCompositeType().show());
			sb.append(sep);
			sb.append("Channel: ");
			sb.append("-");
			
		}
		
		channelLabel.setText(sb.toString());
		
	}










	// ////////////////////////////////////////////////////////
	// UI ACTIONS
	// ////////////////////////////////////////////////////////

	private void actionAbout()
	{
		AboutDialogue.Contents contents = new AboutDialogue.Contents();
		contents.name = Version.program_name;
		contents.description = "XRF Analysis Software";
		contents.website = "www.sciencestudio.net";
		contents.copyright = "Copyright &copy; 2009-2018 by The University of Western Ontario and The Canadian Light Source Inc.";
		contents.licence = StringInput.contents(getClass().getResourceAsStream("/peakaboo/licence.txt"));
		contents.credits = StringInput.contents(getClass().getResourceAsStream("/peakaboo/credits.txt"));
		contents.logo = IconFactory.getImageIcon( Version.logo );
		contents.version = Integer.toString(Version.versionNoMajor);
		contents.longVersion = Version.longVersionNo;
		contents.releaseDescription = Version.releaseDescription;
		contents.date = Version.buildDate;
		
		new AboutDialogue(container.getWindow(), contents);
	}
	
	private void actionHelp()
	{
		Apps.browser("http://sciencestudio.net/downloads/Peakaboo4/Peakaboo%204%20Users%20Guide.pdf");
	}
	
	private void actionOpenData()
	{		
		
		
		List<SimpleFileExtension> exts = new ArrayList<>();
		BoltPluginSet<DataSourcePlugin> plugins = DataSourceLoader.getPluginSet();
		for (DataSourcePlugin p : plugins.newInstances()) {
			FileFormat f = p.getFileFormat();
			SimpleFileExtension ext = new SimpleFileExtension(f.getFormatName(), f.getFileExtensions());
			exts.add(ext);
		}

		openNewDataset(exts);
		
		
	}
	

	public void loadFiles(List<File> filenames)
	{

		List<DataSourcePlugin> candidates =  DataSourceLoader.getPluginSet().newInstances();
		List<DataSource> formats = DataSourceLookup.findDataSourcesForFiles(filenames.stream().map(File::toPath).collect(Collectors.toList()), candidates);
		
		if (formats.size() > 1)
		{
			DataSourceSelection selection = new DataSourceSelection();
			DataSource dsp = selection.pickDSP(container.getWindow(), formats);
			if (dsp != null) loadFiles(filenames, dsp);
		}
		else if (formats.size() == 0)
		{
			JOptionPane.showMessageDialog(
					this, 
					"Could not determine the data format of the selected file(s)", 
					"Open Failed", 
					JOptionPane.ERROR_MESSAGE, 
					StockIcon.BADGE_WARNING.toImageIcon(IconSize.ICON)
				);
		}
		else
		{
			loadFiles(filenames, formats.get(0));
		}
		
	}
	
	public void loadFiles(List<File> files, DataSource dsp)
	{
		if (files != null)
		{

			ExecutorSet<DatasetReadResult> reading = controller.data().TASK_readFileListAsDataset(files, dsp);
			
			
			
			ExecutorSetView execPanel = new ExecutorSetView(reading); 
			Mutable<Boolean> finished = new Mutable<Boolean>(false);
			reading.addListener(() -> {
				javax.swing.SwingUtilities.invokeLater(() -> {
					if (reading.isAborted() || reading.getCompleted()){
						if (finished.get()) { return; }
						finished.set(true);
						
						DatasetReadResult result = reading.getResult();
						if (result == null || result.status == ReadStatus.FAILED)
						{
							if (result == null) {
								PeakabooLog.get().log(Level.SEVERE, "Error Opening Data", "Peakaboo could not open this dataset from " + dsp.getFileFormat().getFormatName());
							} else if (result.problem != null) {
								PeakabooLog.get().log(Level.SEVERE, "Error Opening Data: Peakaboo could not open this dataset from " + dsp.getFileFormat().getFormatName(), result.problem);
							} else {
								JOptionPane.showMessageDialog(this, "Peakaboo could not open this dataset.\n" + result.message, "Open Failed", JOptionPane.OK_OPTION, StockIcon.BADGE_WARNING.toImageIcon(IconSize.ICON));
							}
						}

						// set some controls based on the fact that we have just loaded a
						// new data set
						savedSessionFileName = null;
						popModalComponent();
						
					}			
				});
			});
			
			pushModalComponent(execPanel);
			reading.startWorking();
			
			
			


		}
	}
	
	
	public void loadExistingDataSource(DataSource ds, String settings) {
		
		DummyExecutor progress = new DummyExecutor(ds.getScanData().scanCount());
		progress.advanceState();
		ExecutorSet<Boolean> exec = new ExecutorSet<Boolean>("Loading Data Set") {

			@Override
			protected Boolean execute() {
				getController().data().setDataSource(ds, progress, this::isAborted);
				getController().loadSettings(settings, false);
				popModalComponent();
				return true;
			}}; 
			
		
		exec.addExecutor(progress, "Calculating Values");
			
		ExecutorSetView view = new ExecutorSetView(exec);
		pushModalComponent(view);
		exec.startWorking();
		
	}

	
	

	private void actionExportData(DataSink sink) {
		DataSource source = controller.data().getDataSet().getDataSource();

		SimpleFileExtension ext = new SimpleFileExtension(sink.getFormatName(), sink.getFormatExtension());
		SwidgetFilePanels.saveFile(container.getWindow(), "Export Scan Data", exportedDataFileName, ext, file -> {
			if (!file.isPresent()) {
				return;
			}
			try {
				sink.write(source, file.get().toPath());
			} catch (IOException e) {
				PeakabooLog.get().log(Level.SEVERE, "Failed to export data", e);
			}
			
		});

	}

	private void actionMap(FittingTransform type)
	{

		Mutable<Boolean> mapShown = new Mutable<>(false);
		if (!controller.data().hasDataSet()) return;


		final ExecutorSet<MapResultSet> tasks = controller.getMapCreationTask(type);
		if (tasks == null) return;

		ExecutorSetView execPanel = new ExecutorSetView(tasks);
		tasks.addListener(() -> {
			if (tasks.getCompleted())
			{

				if (mapShown.testAndSet(true) == true) {
					return;
				}
				
				MapperFrame mapperWindow;
				MapResultSet results = tasks.getResult();
				MapSetController mapData = new MapSetController();
				

				Coord<Integer> dataDimensions = null;
				Coord<Bounds<Number>> physicalDimensions = null;
				SISize physicalUnit = null;
				
				Optional<PhysicalSize> physical = controller.data().getDataSet().getPhysicalSize();
				if (physical.isPresent()) {
					physicalDimensions = physical.get().getPhysicalDimensions();
					physicalUnit = physical.get().getPhysicalUnit();
				}
				
				if (controller.data().getDataSet().hasGenuineDataSize()) {
					dataDimensions = controller.data().getDataSet().getDataSize().getDataDimensions();
				}
				
				mapData.setMapData(
						results,
						controller.data().getDataSet().getScanData().datasetName(),
						controller.data().getDiscards().list(),
						dataDimensions,
						physicalDimensions,
						physicalUnit
					);
				
				
				mapperWindow = new MapperFrame(container, mapData, null, controller);

				mapperWindow.setVisible(true);

			}
			
			if (tasks.getCompleted() || tasks.isAborted()) {
				popModalComponent();
			}
		});
		
		
		pushModalComponent(execPanel);
		tasks.startWorking();


		

	}


	private void actionSaveSession()
	{

		SimpleFileExtension peakaboo = new SimpleFileExtension("Peakaboo Session File", "peakaboo");
		SwidgetFilePanels.saveFile(this, "Save Session Data", savedSessionFileName, peakaboo, file -> {
			if (!file.isPresent()) {
				return;
			}
			try {
				FileOutputStream os = new FileOutputStream(file.get());
				os.write(controller.saveSettings().getBytes());
				os.close();
				savedSessionFileName = file.get().getParentFile();
			}
			catch (IOException e)
			{
				PeakabooLog.get().log(Level.SEVERE, "Failed to save session", e);
			}
			
		});
	}


	private void actionSavePicture()
	{
		if (saveFilesFolder == null) {
			saveFilesFolder = datasetFolder;
		}
		SavePicture sp = new SavePicture(this, canvas, saveFilesFolder, file -> {
			if (file.isPresent()) {
				saveFilesFolder = file.get().getParentFile();
			}
		});
		sp.show();
		 
	}


	private void actionSaveFilteredData()
	{
		if (saveFilesFolder == null) {
			saveFilesFolder = datasetFolder;
		}
		
		
		//Spectrum data = filters.filterDataUnsynchronized(new ISpectrum(datasetProvider.getScan(ordinal)), false);
		final FilterSet filters = controller.filtering().getActiveFilters();
		System.out.println(filters.getFilters());

		SimpleFileExtension text = new SimpleFileExtension("Text File", "txt");
		SwidgetFilePanels.saveFile(this, "Save Fitted Data to Text File", saveFilesFolder, text, saveFile -> {
			if (!saveFile.isPresent()) {
				return;
			}
			
			saveFilesFolder = saveFile.get().getParentFile();
			
			StreamExecutor<Throwable> streamexec = new StreamExecutor<>("Exporting Data");
			streamexec.setParallel(false);
			streamexec.setTask(controller.data().getDataSet().getScanData(), stream -> {
				
				try {
										
					Mutable<Boolean> errored = new Mutable<>(false);
					OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(saveFile.get()));
					stream.forEach(spectrum -> {
						spectrum = filters.applyFiltersUnsynchronized(spectrum, false);
						try {
							osw.write(spectrum.toString() + "\n");
						} catch (Exception e) { 
							if (!errored.get()) {
								PeakabooLog.get().log(Level.SEVERE, "Failed to save fitted data", e);
								streamexec.abort();
								errored.set(true);
							}
						}
					});

					osw.close();
										
					return null;
				} catch (Exception e) { 
					PeakabooLog.get().log(Level.SEVERE, "Failed to save fitted data", e);
				}
				
				return null;
			});
			
			StreamExecutorView view = new StreamExecutorView(streamexec);
			StreamExecutorPanel panel = new StreamExecutorPanel("Exporting Data", view);
			
			streamexec.addListener(() -> {
				if (streamexec.getState() != StreamExecutor.State.RUNNING) {
					popModalComponent();
				}
				if (streamexec.getState() == StreamExecutor.State.ABORTED) {
					saveFile.get().delete();
				}
			});
			
			pushModalComponent(panel);
			streamexec.start();
			
			
		});
		
	}
	
	private void actionSaveFittingInformation()
	{

		if (saveFilesFolder == null) {
			saveFilesFolder = datasetFolder;
		}

		List<TransitionSeries> tss = controller.fitting().getFittedTransitionSeries();
		

		
		SimpleFileExtension ext = new SimpleFileExtension("Text File", "txt");
		SwidgetFilePanels.saveFile(this, "Save Fitting Information to Text File", saveFilesFolder, ext, file -> {
			if (!file.isPresent()) {
				return;
			}
			try {
				// get an output stream to write the data to
				FileOutputStream os = new FileOutputStream(file.get());
				OutputStreamWriter osw = new OutputStreamWriter(os);
								
				// write out the data
				float intensity;
				for (TransitionSeries ts : tss)
				{

					if (ts.visible)
					{
						intensity = controller.fitting().getTransitionSeriesIntensity(ts);
						osw.write(ts.toString() + ", " + SigDigits.roundFloatTo(intensity, 2) + "\n");
					}
				}
				osw.close();
				os.close();
			}
			catch (IOException e)
			{
				PeakabooLog.get().log(Level.SEVERE, "Failed to save fitting information", e);
			}
			
		});

	}

	public void actionLoadSession() {

		SimpleFileExtension peakaboo = new SimpleFileExtension("Peakaboo Session File", "peakaboo");
		SwidgetFilePanels.openFile(this, "Load Session Data", savedSessionFileName, peakaboo, file -> {
			if (!file.isPresent()) {
				return;
			}
			try {
				controller.loadSettings(StringInput.contents(file.get()), false);
			} catch (IOException e) {
				PeakabooLog.get().log(Level.SEVERE, "Failed to load session", e);
			}
		});

	}

	public void actionShowInfo()
	{
		
		Map<String, String> properties;
		
		properties = new LinkedHashMap<String, String>();
		properties.put("Data Format", "" + controller.data().getDataSet().getDataSource().getFileFormat().getFormatName());
		properties.put("Scan Count", "" + controller.data().getDataSet().getScanData().scanCount());
		properties.put("Channels per Scan", "" + controller.data().getDataSet().getAnalysis().channelsPerScan());
		properties.put("Maximum Intensity", "" + controller.data().getDataSet().getAnalysis().maximumIntensity());
		
		
		
		
		//Extended attributes
		if (controller.data().getDataSet().getMetadata().isPresent()) {
			Metadata metadata = controller.data().getDataSet().getMetadata().get();
			
			properties.put("Date of Creation", metadata.getCreationTime());
			properties.put("Created By", metadata.getCreator());
			
			properties.put("Project Name", metadata.getProjectName());
			properties.put("Session Name", metadata.getSessionName());
			properties.put("Experiment Name", metadata.getExperimentName());
			properties.put("Sample Name", metadata.getSampleName());
			properties.put("Scan Name", metadata.getScanName());
			
			properties.put("Facility", metadata.getFacilityName());
			properties.put("Laboratory", metadata.getLaboratoryName());
			properties.put("Instrument", metadata.getInstrumentName());
			properties.put("Technique", metadata.getTechniqueName());
			
		}
		
		
		
		JPanel panel = new JPanel(new BorderLayout());
		PropertyViewPanel propPanel = new PropertyViewPanel(properties, "Dataset Information");
		propPanel.setBorder(Spacing.bHuge());
		panel.add(propPanel, BorderLayout.CENTER);
		
		ButtonBox box = new ButtonBox(true);
		ImageButton close = new ImageButton(StockIcon.WINDOW_CLOSE, "Close", true);
		box.addRight(close);
		close.addActionListener(e -> {
			this.popModalComponent();
		});
		panel.add(box, BorderLayout.SOUTH);
		
		this.pushModalComponent(panel);
		

	}
	
	public void actionGuessMaxEnergy() {
		
		if (controller == null) return;
		if (controller.fitting().getVisibleTransitionSeries().size() < 2) {
			JOptionPane.showMessageDialog(this, "Attempting to detect an energy calibration requires at least two elements to be fitted.\nTry using 'Elemental Lookup', as 'Guided Fitting' will not work without energy calibration set.");
			return;
		}
		
		
		//setCursor(new Cursor(Cursor.WAIT_CURSOR));
		
		
		StreamExecutorSet<EnergyCalibration> energyTask = AutoEnergyCalibration.propose(
				controller.data().getDataSet().getAnalysis().averagePlot(), 
				controller.fitting().getVisibleTransitionSeries(), 
				controller.data().getDataSet().getAnalysis().channelsPerScan());
		
		
		List<StreamExecutorView> views = energyTask.getExecutors().stream().map(StreamExecutorView::new).collect(Collectors.toList());
		StreamExecutorPanel panel = new StreamExecutorPanel("Detecting Energy Level", views);
				
		energyTask.last().addListener(() -> {
			if (energyTask.last().getState() != StreamExecutor.State.RUNNING) {
				popModalComponent();
			}
			
			if (energyTask.last().getState() == StreamExecutor.State.COMPLETED) {
				EnergyCalibration energy = energyTask.last().getResult().orElse(null);
				if (energy != null) {
					controller.settings().setMinEnergy(energy.getMinEnergy());
					controller.settings().setMaxEnergy(energy.getMaxEnergy());
				}
			}
		});
		
		pushModalComponent(panel);
		energyTask.start();
		
		
		//controller.settings().setMaxEnergy(maxEnergy);
		
		//setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
		
	}
	
	public void actionOpenPluginFolder() {
		File appDataDir = Configuration.appDir("Plugins");
		appDataDir.mkdirs();
		Desktop desktop = Desktop.getDesktop();
		try {
			desktop.open(appDataDir);
		} catch (IOException e1) {
			PeakabooLog.get().log(Level.SEVERE, "Failed to open plugin folder", e1);
		}
	}
	
	
	public void actionShowLogs() {
		File appDataDir = Configuration.appDir("Logging");
		appDataDir.mkdirs();
		Desktop desktop = Desktop.getDesktop();
		try {
			desktop.open(appDataDir);
		} catch (IOException e1) {
			PeakabooLog.get().log(Level.SEVERE, "Failed to open logging folder", e1);
		}
	}

}
