package peakaboo.controller.plotter.filtering;

import java.util.ArrayList;
import java.util.List;

import peakaboo.filter.model.Filter;
import peakaboo.filter.model.FilterSet;
import peakaboo.filter.model.SerializedFilter;

public class SavedFilteringSession {
	
	
	public List<SerializedFilter> filters = new ArrayList<>();
	
	
	public SavedFilteringSession storeFrom(FilteringController controller) {
		for (Filter filter : controller.filteringModel.filters) {
			this.filters.add(new SerializedFilter(filter));
		}
		return this;
	}
	
	public SavedFilteringSession loadInto(FilteringController controller) {
		FilterSet filterset = controller.getFilteringModel().filters;
		filterset.clear();
		for (SerializedFilter f : this.filters) {
			filterset.add(f.getFilter());
		}
		return this;
	}
	
	
	
}
