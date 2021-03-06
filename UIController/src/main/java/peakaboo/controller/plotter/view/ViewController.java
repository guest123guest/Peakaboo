package peakaboo.controller.plotter.view;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.logging.Level;

import eventful.Eventful;
import peakaboo.common.Configuration;
import peakaboo.common.PeakabooLog;
import peakaboo.controller.plotter.PlotController;
import peakaboo.controller.settings.SavedPersistence;
import peakaboo.curvefit.curve.fitting.EnergyCalibration;
import peakaboo.display.plot.ChannelCompositeMode;
import scidraw.drawing.ViewTransform;
import scitypes.Pair;
import scitypes.ReadOnlySpectrum;


public class ViewController extends Eventful
{

	
	private ViewModel viewModel;
	private PlotController plot;
	
	public ViewController(PlotController plotController)
	{
		this.plot = plotController;
		viewModel = new ViewModel();
	}

	public ViewModel getViewModel()
	{
		return viewModel;
	}

	private void setUndoPoint(String change)
	{
		plot.history().setUndoPoint(change);
	}
	

	public float getZoom()
	{
		return viewModel.session.zoom;
	}

	public void setZoom(float zoom)
	{
		viewModel.session.zoom = zoom;
		updateListeners();
	}

	public void setShowIndividualSelections(boolean showIndividualSelections)
	{
		viewModel.persistent.showIndividualFittings = showIndividualSelections;
		savePersistentSettings();
		setUndoPoint("Individual Fittings");
		plot.fitting().fittingDataInvalidated();
	}

	public boolean getShowIndividualSelections()
	{
		return viewModel.persistent.showIndividualFittings;
	}

	
	public void setViewLog(boolean log)
	{
		if (log)
		{
			viewModel.session.viewTransform = ViewTransform.LOG;
		}
		else
		{
			viewModel.session.viewTransform = ViewTransform.LINEAR;
		}
		setUndoPoint("Log View");
		updateListeners();
	}

	public boolean getViewLog()
	{
		return viewModel.session.viewTransform == ViewTransform.LOG;
	}

	public void setChannelCompositeMode(ChannelCompositeMode mode)
	{
		viewModel.session.channelComposite = mode;
		setUndoPoint(mode.show());
		plot.filtering().filteredDataInvalidated();
	}
	

	public ChannelCompositeMode getChannelCompositeMode()
	{
		return viewModel.session.channelComposite;
	}

	public void setScanNumber(int number)
	{
		//negative is downwards, positive is upwards
		int direction = number - viewModel.session.scanNumber;

		if (direction > 0)
		{
			number = plot.data().getDataSet().getAnalysis().firstNonNullScanIndex(number);
		}
		else
		{
			number = plot.data().getDataSet().getAnalysis().lastNonNullScanIndex(number);
		}

		if (number == -1)
		{
			updateListeners();
			return;
		}

		
		if (number > plot.data().getDataSet().getScanData().scanCount() - 1) {
			number = plot.data().getDataSet().getScanData().scanCount() - 1;
		}
		if (number < 0) number = 0;
		viewModel.session.scanNumber = number;
		plot.filtering().filteredDataInvalidated();
	}

	public int getScanNumber()
	{
		return viewModel.session.scanNumber;
	}

	public void setShowAxes(boolean axes)
	{
		viewModel.persistent.showAxes = axes;
		savePersistentSettings();
		plot.setAxisPainters(null);
		setUndoPoint("Axes");
		updateListeners();
	}

	public boolean getShowAxes()
	{
		return viewModel.persistent.showAxes;
	}

	public boolean getShowTitle()
	{
		return viewModel.persistent.showPlotTitle;
	}

	public void setShowTitle(boolean show)
	{
		viewModel.persistent.showPlotTitle = show;
		savePersistentSettings();
		plot.setAxisPainters(null);
		setUndoPoint("Title");
		updateListeners();
	}

	public void setMonochrome(boolean mono)
	{
		viewModel.persistent.monochrome = mono;
		savePersistentSettings();
		setUndoPoint("Monochrome");
		updateListeners();
	}

	public boolean getMonochrome()
	{
		return viewModel.persistent.monochrome;
	}

	public void setShowElementTitles(boolean show)
	{
		viewModel.persistent.showElementFitTitles = show;
		savePersistentSettings();
		setUndoPoint("Fitting Titles");
		updateListeners();
	}

	public void setShowElementMarkers(boolean show)
	{
		viewModel.persistent.showElementFitMarkers = show;
		savePersistentSettings();
		setUndoPoint("Fitting Markers");
		updateListeners();
	}

	public void setShowElementIntensities(boolean show)
	{
		viewModel.persistent.showElementFitIntensities = show;
		savePersistentSettings();
		setUndoPoint("Fitting Heights");
		updateListeners();
	}

	public boolean getShowElementTitles()
	{
		return viewModel.persistent.showElementFitTitles;
	}

	public boolean getShowElementMarkers()
	{
		return viewModel.persistent.showElementFitMarkers;
	}

	public boolean getShowElementIntensities()
	{
		return viewModel.persistent.showElementFitIntensities;
	}

	public void setShowRawData(boolean show)
	{
		viewModel.session.backgroundShowOriginal = show;
		setUndoPoint("Raw Data Outline");
		updateListeners();
	}

	public boolean getShowRawData()
	{
		return viewModel.session.backgroundShowOriginal;
	}
	
	public float getEnergyForChannel(int channel)
	{
		if (!plot.data().hasDataSet()) return 0.0f;
		EnergyCalibration calibration = new EnergyCalibration(
				plot.fitting().getMinEnergy(), 
				plot.fitting().getMaxEnergy(), 
				plot.data().getDataSet().getAnalysis().channelsPerScan()
			);
		return calibration.energyFromChannel(channel);
	}

	public Pair<Float, Float> getValueForChannel(int channel)
	{
		if (channel == -1) return null;
		if (channel >= plot.data().getDataSet().getAnalysis().channelsPerScan()) return null;

		Pair<ReadOnlySpectrum, ReadOnlySpectrum> scans = plot.getDataForPlot();
		if (scans == null) return new Pair<Float, Float>(0.0f, 0.0f);

		return new Pair<Float, Float>(scans.first.get(channel), scans.second.get(channel));
	}

	
	public boolean getLockPlotHeight() {
		return viewModel.session.lockPlotHeight;
	}
	public void setLockPlotHeight(boolean lock) {
		viewModel.session.lockPlotHeight = lock;
		updateListeners();
	}
	
	

	
	/**
	 * This should really only be called at creation time, since it loads settings 
	 * from disk and does not create an undo point.
	 */
	public void loadPersistentSettings() {
		File file = new File(Configuration.appDir() + "/settings.yaml");
		if (!file.exists()) {
			savePersistentSettings();
		}
		try {
			
			byte[] bytes = Files.readAllBytes(file.toPath());
			String yaml = new String(bytes);
			SavedPersistence saved = SavedPersistence.deserialize(yaml);
			saved.loadInto(plot);
					
		} catch (IOException e) {
			PeakabooLog.get().log(Level.WARNING, "Could not load persistent settings", e);
		}
		
		plot.filtering().filteredDataInvalidated();
		plot.fitting().fittingDataInvalidated();
		updateListeners();
		
	}
	
	
	private void savePersistentSettings() {
		File file = new File(Configuration.appDir() + "/settings.yaml");
		try {
			
			SavedPersistence saved = SavedPersistence.storeFrom(plot);
			String yaml = saved.serialize();
			byte[] bytes = yaml.getBytes();
			Files.write(file.toPath(), bytes);
			
		} catch (IOException e) {
			PeakabooLog.get().log(Level.WARNING, "Could not save persistent settings", e);
		}
	}





	
}
