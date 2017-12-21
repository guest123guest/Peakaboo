package peakaboo.controller.plotter.fitting;

import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import eventful.EventfulType;
import fava.Fn;
import fava.datatypes.Pair;
import peakaboo.controller.plotter.IPlotController;
import peakaboo.curvefit.controller.TSOrdering;
import peakaboo.curvefit.model.FittingModel;
import peakaboo.curvefit.model.FittingResult;
import peakaboo.curvefit.model.FittingResultSet;
import peakaboo.curvefit.model.FittingSet;
import peakaboo.curvefit.model.transitionseries.EscapePeakType;
import peakaboo.curvefit.model.transitionseries.TransitionSeries;
import peakaboo.curvefit.model.transitionseries.TransitionSeriesType;
import peakaboo.curvefit.peaktable.PeakTable;
import scitypes.ReadOnlySpectrum;
import scitypes.SpectrumCalculations;


public class FittingController extends EventfulType<Boolean> implements IFittingController
{

	FittingModel fittingModel;
	IPlotController plot;
	
	public FittingController(IPlotController plotController)
	{
		this.plot = plotController;
		fittingModel = new FittingModel();
	}
	
	@Override
	public FittingModel getFittingModel()
	{
		return fittingModel;
	}
	
	private void setUndoPoint(String change)
	{
		plot.history().setUndoPoint(change);
	}
	
	
	@Override
	public void addTransitionSeries(TransitionSeries e)
	{
		if (e == null) return;
		fittingModel.selections.addTransitionSeries(e);
		setUndoPoint("Add Fitting");
		fittingDataInvalidated();
	}

	@Override
	public void addAllTransitionSeries(List<TransitionSeries> tss)
	{
		for (TransitionSeries ts : tss)
		{
			fittingModel.selections.addTransitionSeries(ts);
		}
		setUndoPoint("Add Fittings");
		fittingDataInvalidated();
	}

	@Override
	public void clearTransitionSeries()
	{
		
		fittingModel.selections.clear();
		setUndoPoint("Clear Fittings");
		fittingDataInvalidated();
	}

	@Override
	public void removeTransitionSeries(TransitionSeries e)
	{
		
		fittingModel.selections.remove(e);
		setUndoPoint("Remove Fitting");
		fittingDataInvalidated();
	}

	@Override
	public List<TransitionSeries> getFittedTransitionSeries()
	{
		return fittingModel.selections.getFittedTransitionSeries();
	}

	@Override
	public List<TransitionSeries> getUnfittedTransitionSeries(final TransitionSeriesType tst)
	{
		final List<TransitionSeries> fitted = getFittedTransitionSeries();
		return PeakTable.getAllTransitionSeries().stream().filter(ts -> (!fitted.contains(ts)) && tst.equals(ts.type)).collect(toList());
	}

	@Override
	public void setTransitionSeriesVisibility(TransitionSeries e, boolean show)
	{
		fittingModel.selections.setTransitionSeriesVisibility(e, show);
		setUndoPoint("Fitting Visiblitiy");
		fittingDataInvalidated();
	}

	@Override
	public boolean getTransitionSeriesVisibility(TransitionSeries e)
	{
		return e.visible;
	}

	@Override
	public List<TransitionSeries> getVisibleTransitionSeries()
	{
		return getFittedTransitionSeries().stream().filter(ts -> ts.visible).collect(toList());
	}

	@Override
	public float getTransitionSeriesIntensity(TransitionSeries ts)
	{
		plot.regenerateCahcedData();

		if (fittingModel.selectionResults == null) return 0.0f;

		for (FittingResult result : fittingModel.selectionResults.fits)
		{
			if (result.transitionSeries == ts) {
				float max = SpectrumCalculations.max(result.fit);
				if (Float.isNaN(max)) max = 0f;
				return max;
			}
		}
		return 0.0f;

	}

	@Override
	public void moveTransitionSeriesUp(TransitionSeries e)
	{
		fittingModel.selections.moveTransitionSeriesUp(e);
		setUndoPoint("Move Fitting Up");
		fittingDataInvalidated();
	}

	@Override
	public void moveTransitionSeriesUp(List<TransitionSeries> tss)
	{
		fittingModel.selections.moveTransitionSeriesUp(tss);
		setUndoPoint("Move Fitting Up");
		fittingDataInvalidated();
	}
	
	@Override
	public void moveTransitionSeriesDown(TransitionSeries e)
	{
		fittingModel.selections.moveTransitionSeriesDown(e);
		setUndoPoint("Move Fitting Down");
		fittingDataInvalidated();
	}

	@Override
	public void moveTransitionSeriesDown(List<TransitionSeries> tss)
	{
		fittingModel.selections.moveTransitionSeriesDown(tss);
		setUndoPoint("Move Fitting Down");
		fittingDataInvalidated();
	}

	@Override
	public void fittingDataInvalidated()
	{
		// Clear cached values, since they now have to be recalculated
		fittingModel.selectionResults = null;

		// this will call update listener for us
		fittingProposalsInvalidated();

	}

	
	@Override
	public void addProposedTransitionSeries(TransitionSeries e)
	{
		fittingModel.proposals.addTransitionSeries(e);
		fittingProposalsInvalidated();
	}

	@Override
	public void removeProposedTransitionSeries(TransitionSeries e)
	{
		fittingModel.proposals.remove(e);
		fittingProposalsInvalidated();
	}

	@Override
	public void clearProposedTransitionSeries()
	{
		fittingModel.proposals.clear();
		fittingProposalsInvalidated();
	}

	@Override
	public List<TransitionSeries> getProposedTransitionSeries()
	{
		return fittingModel.proposals.getFittedTransitionSeries();
	}

	@Override
	public void commitProposedTransitionSeries()
	{
		addAllTransitionSeries(fittingModel.proposals.getFittedTransitionSeries());
		fittingModel.proposals.clear();
		fittingDataInvalidated();
	}

	@Override
	public void fittingProposalsInvalidated()
	{
		// Clear cached values, since they now have to be recalculated
		fittingModel.proposalResults = null;
		updateListeners(false);
	}

	@Override
	public void setEscapeType(EscapePeakType type)
	{
		fittingModel.selections.setEscapeType(type);
		fittingModel.proposals.setEscapeType(type);
		
		fittingDataInvalidated();
		
		setUndoPoint("Escape Peaks");
		updateListeners(false);
	}

	@Override
	public EscapePeakType getEscapeType()
	{
		return plot.settings().getEscapePeakType();
	}

	@Override
	public void optimizeTransitionSeriesOrdering()
	{
		
		
		
		//all visible TSs
		final List<TransitionSeries> tss = new ArrayList<>(getVisibleTransitionSeries());
				
		//all invisible TSs
		List<TransitionSeries> invisibles = getFittedTransitionSeries().stream().filter(e -> !tss.contains(e)).collect(toList());
				
		
		//find all the TSs which overlap with other TSs
		final List<TransitionSeries> overlappers = tss.stream().filter(ts -> {
			return TSOrdering.getTSsOverlappingTS(
					ts, 
					tss, 
					plot.settings().getEnergyPerChannel(), 
					plot.data().getDataSet().channelsPerScan(),
					plot.settings().getEscapePeakType()
				).size() != 0;
		}).collect(Collectors.toList());
		
		
		//then get all the TSs which don't overlap
		List<TransitionSeries> nonOverlappers = tss.stream().filter(e -> !overlappers.contains(e)).collect(Collectors.toList());
	
		

		//score each of the overlappers w/o competition
		List<Pair<TransitionSeries, Float>> scoredOverlappers = overlappers.stream().map((TransitionSeries ts) -> {
			return new Pair<TransitionSeries, Float>(
				ts, 
				TSOrdering.fScoreTransitionSeries(
						plot.settings().getEscapePeakType(), 
						plot.settings().getEnergyPerChannel(), 
						plot.filtering().getFilteredPlot()
					).apply(ts)
			);
		}).collect(Collectors.toList());
		
		//sort all the overlappig visible elements according to how strongly they would fit on their own (ie no competition)
		Fn.sortBy(scoredOverlappers, new Comparator<Float>() {
			
			public int compare(Float f1, Float f2)
			{
				return (f2.compareTo(f1));
				
			}
		}, e -> e.second);
		
		
		
		//find the optimal ordering of the visible overlapping TSs based on how they fit with competition
		List<TransitionSeries> bestfit = optimizeTSOrderingHelper(
				scoredOverlappers.stream().map(e -> e.first).collect(Collectors.toList()), 
				new ArrayList<TransitionSeries>()
			);
		

		
		//List<TransitionSeries> bestfit = TSOrdering.optimizeTSOrdering(getEnergyPerChannel(), tss, filteringController.getFilteredPlot());

		//re-add all of the overlappers
		bestfit.addAll(nonOverlappers);
		
		//re-add all of the invisible TSs
		bestfit.addAll(invisibles);
		
		
		//set the TS selection for the model to be the ordering we have just calculated
		clearTransitionSeries();
		addAllTransitionSeries(bestfit);
		setUndoPoint("Fitting Ordering");
		updateListeners(false);
		
	}
		
	public List<TransitionSeries> proposeTransitionSeriesFromChannel(final int channel, TransitionSeries currentTS)
	{
		
		if (! plot.data().hasDataSet() ) return null;
		
		return TSOrdering.proposeTransitionSeriesFromChannel(
				plot.settings().getEscapePeakType(),
				plot.settings().getEnergyPerChannel(),
				plot.filtering().getFilteredPlot(),
				fittingModel.selections,
				fittingModel.proposals,
				channel,
				currentTS	
		);
	}

	@Override
	public boolean canMap()
	{
		return ! (getFittedTransitionSeries().size() == 0 || plot.data().getDataSet().getScanData().scanCount() == 0);
	}

	// =============================================
	// Helper Functions for IFittingController
	// =============================================
	private List<TransitionSeries> optimizeTSOrderingHelper(List<TransitionSeries> unfitted, List<TransitionSeries> fitted)
	{
		
		//assumption: unfitted will be in sorted order based on how well each TS fits independently
		if (unfitted.size() == 0) return fitted;
		
		int n = 4;
		
		List<TransitionSeries> topn = unfitted.subList(0, n);
		unfitted.removeAll(topn);
		List<List<TransitionSeries>> perms = Fn.permutations(topn);
				
		//function to score an ordering of Transition Series
		final Function<List<TransitionSeries>, Float> scoreTSs = tss -> {
				
			final Function<TransitionSeries, Float> scoreTS = TSOrdering.fScoreTransitionSeries(
					plot.settings().getEscapePeakType(), 
					plot.settings().getEnergyPerChannel(), 
					plot.filtering().getFilteredPlot()
				);
			
			Float score = 0f;
			for (TransitionSeries ts : tss)
			{
				score = scoreTS.apply(ts);
			}
			return score;
		};
	
		
		//find the best fitting for the currently selected fittings
		List<TransitionSeries> bestfit = perms.stream().reduce((l1, l2) -> {
			Float s1, s2; //scores
			s1 = scoreTSs.apply(l1);
			s2 = scoreTSs.apply(l2);				
			
			if (s1 < s2) return l1;
			return l2;	
		}).get();

		
		//add the best half of the fitted elements to the fititngs list
		//and the rest back into the start of the unfitted elements list
		fitted.addAll(bestfit.subList(0, n/2));
		bestfit.removeAll(fitted);
		unfitted.addAll(0, bestfit);
		
		
		//recurse
		return optimizeTSOrderingHelper(unfitted, fitted);

				
	}

	@Override
	public void setFittingParameters(float energyPerChannel)
	{

		int scanSize = 0;
		
		plot.getDR().unitSize = energyPerChannel;
		fittingModel.selections.setDataParameters(scanSize, energyPerChannel, plot.settings().getEscapePeakType());
		fittingModel.proposals.setDataParameters(scanSize, energyPerChannel, plot.settings().getEscapePeakType());

		setUndoPoint("Calibration");
		plot.filtering().filteredDataInvalidated();
	}

	

	@Override
	public void calculateProposalFittings()
	{
		fittingModel.proposalResults = fittingModel.proposals.calculateFittings(fittingModel.selectionResults.residual);
	}

	@Override
	public void calculateSelectionFittings(ReadOnlySpectrum data)
	{
		fittingModel.selectionResults = fittingModel.selections.calculateFittings(data);
	}

	@Override
	public boolean hasProposalFitting()
	{
		return fittingModel.proposalResults != null;
	}

	@Override
	public boolean hasSelectionFitting()
	{
		return fittingModel.selectionResults != null;
	}

	@Override
	public FittingSet getFittingSelections()
	{
		return fittingModel.selections;
	}

	@Override
	public FittingResultSet getFittingProposalResults()
	{
		return fittingModel.proposalResults;
	}

	@Override
	public FittingResultSet getFittingSelectionResults()
	{
		return fittingModel.selectionResults;
	}
	
	
}