package peakaboo.controller.plotter.filtering;

import peakaboo.filter.model.FilterSet;
import scitypes.ReadOnlySpectrum;


public class FilteringModel
{

	// Data related to applying filters. Filters manipulate the dataset
	public FilterSet			filters;
	public ReadOnlySpectrum		filteredPlot;
	
	public FilteringModel()
	{
		filters = new FilterSet();
		filteredPlot = null;
	}
	
}
