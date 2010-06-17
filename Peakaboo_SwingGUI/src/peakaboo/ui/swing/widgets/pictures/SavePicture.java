package peakaboo.ui.swing.widgets.pictures;


import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.LinkedList;
import java.util.List;

import javax.jnlp.FileContents;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;

import peakaboo.common.Env;
import peakaboo.controller.CanvasController;
import peakaboo.fileio.IOCommon;
import peakaboo.ui.swing.fileio.SwingIO;
import peakaboo.ui.swing.widgets.ClearPanel;
import peakaboo.ui.swing.widgets.Spacing;
import peakaboo.ui.swing.widgets.ImageButton;
import peakaboo.ui.swing.widgets.dialogues.SimpleIODialogues;
import peakaboo.ui.swing.widgets.toggle.ComplexToggle;
import peakaboo.ui.swing.widgets.toggle.ComplexToggleGroup;


public class SavePicture extends JDialog
{

	private CanvasController	controller;
	private String				startingFolder;
	ComplexToggleGroup			group;


	JPanel				controlsPanel;
	
	public SavePicture(Frame owner, CanvasController controller, String startingFolder)
	{

		super(owner, "Save as Image");

		this.controller = controller;
		this.startingFolder = startingFolder;

		init(owner);

	}

	public SavePicture(Dialog owner, CanvasController controller, String startingFolder)
	{

		super(owner, "Save as Image");

		this.controller = controller;
		this.startingFolder = startingFolder;

		init(owner);

	}
	
	private void init(Component owner)
	{

		controlsPanel = new ClearPanel();

		controlsPanel.setLayout(new BorderLayout());
		controlsPanel.add(createOptionsPane(), BorderLayout.CENTER);
		controlsPanel.add(createControlPanel(), BorderLayout.SOUTH);

		add(controlsPanel);

		int height = getPreferredSize().height;
		setPreferredSize(new Dimension(500, height));

		setResizable(false);

		pack();
		setLocationRelativeTo(owner);
		setModal(true);
		setVisible(true);
	}


	public JPanel createControlPanel()
	{

		JPanel buttonBox = new ClearPanel();
		buttonBox.setLayout(new BoxLayout(buttonBox, BoxLayout.LINE_AXIS));


		ImageButton ok = new ImageButton("document-save", "Save", true);
		ImageButton cancel = new ImageButton("cancel", "Cancel", true);

		ok.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e)
			{

				int selection = group.getToggledIndex();
				Cursor oldCursor = getCursor();
				setCursor(new Cursor(Cursor.WAIT_CURSOR));
				if (selection == 0) savePNG();
				if (selection == 1) saveSVG();
				if (selection == 2) savePDF();
				setCursor(oldCursor);

			}
		});

		cancel.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e)
			{
				setVisible(false);
			}
		});

		buttonBox.add(Box.createHorizontalGlue());
		buttonBox.add(ok);
		buttonBox.add(Box.createHorizontalStrut(5));
		buttonBox.add(cancel);

		buttonBox.setBorder(Spacing.bHuge());

		return buttonBox;

	}


	public JPanel createOptionsPane()
	{

		JPanel panel = new ClearPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));

		group = new ComplexToggleGroup();

		ComplexToggle png, svg, pdf;

		png = new ComplexToggle("picture_png", "Pixel Image (PNG)",
				"Pixel based images are a grid of coloured dots. They have a fixed size and level of detail.", group);

		svg = new ComplexToggle("picture_svg", "Vector Image (SVG)",
				"Vector images use points, lines, and curves to define an image. They can be scaled to any size.",
				group);

		pdf = new ComplexToggle("picture_pdf", "PDF File", "PDF files are a more print-oriented vector image format.",
				group);


		panel.add(png);
		panel.add(svg);
		panel.add(pdf);

		group.setToggled(0);

		panel.setBorder(Spacing.bHuge());

		return panel;

	}


	private void savePNG()
	{

		try
		{
			
			controlsPanel.setEnabled(false);
			controlsPanel.setCursor(new Cursor(Cursor.WAIT_CURSOR));
			
			ByteArrayOutputStream baos = SwingIO.getSaveFileBuffer();				
			controller.writePNG(baos);

			String result = SwingIO.saveFile(this, "Save Picture As...", "png", "Portable Network Graphic", startingFolder, baos);

			if (result != null) setVisible(false);
			setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
			setEnabled(true);
			
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}


	private void saveSVG()
	{


		try
		{
			
			controlsPanel.setEnabled(false);
			controlsPanel.setCursor(new Cursor(Cursor.WAIT_CURSOR));
			
			ByteArrayOutputStream baos = SwingIO.getSaveFileBuffer();				
			controller.writeSVG(baos);

			String result = SwingIO.saveFile(this, "Save Picture As...", "svg", "Scalable Vector Graphic", startingFolder, baos);

			if (result != null) setVisible(false);
			setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
			setEnabled(true);
			
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}



	}


	private void savePDF()
	{

		try
		{
			
			controlsPanel.setEnabled(false);
			controlsPanel.setCursor(new Cursor(Cursor.WAIT_CURSOR));
			
			ByteArrayOutputStream baos = SwingIO.getSaveFileBuffer();				
			controller.writePDF(baos);

			String result = SwingIO.saveFile(this, "Save Picture As...", "pdf", "Portable Document Format", startingFolder, baos);

			if (result != null) setVisible(false);
			setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
			setEnabled(true);
			
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	
	public String getStartingFolder()
	{
		return startingFolder;
	}

	

}
