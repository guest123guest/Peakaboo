package peakaboo.filter.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import autodialog.model.Parameter;
import autodialog.model.Value;
import peakaboo.common.Version;
import scidraw.drawing.plot.painters.PlotPainter;
import scitypes.ISpectrum;
import scitypes.Range;
import scitypes.ReadOnlySpectrum;
import scitypes.Spectrum;

/**
 * 
 * This abstract class defines a filter for a {@link Spectrum} of data. A large part of this abstract
 * class is for use in UIs, as it focuses on classifying the type and name of the filter, and the
 * types of parameters. This provides a way for UIs to display the filter and let the user change the
 * settings.
 * 
 * 
 * @author Nathaniel Sherry, 2009-2012
 * 
 */

public abstract class AbstractFilter implements Serializable, FilterPlugin
{
	
	private List<Value<?>>		parameters;
	public boolean				enabled;
	
	protected ReadOnlySpectrum	previewCache;
	protected ReadOnlySpectrum	calculatedData;
	
	
	//==============================================
	// PLUGIN METHODS
	//==============================================	

	@Override
	public String pluginName() {
		return getFilterName();
	}

	@Override
	public String pluginDescription() {
		return getFilterDescription();
	}
	
	
	public AbstractFilter()
	{
		this.parameters = new ArrayList<>();
		this.enabled = true;
	}

	/* (non-Javadoc)
	 * @see peakaboo.filter.model.Filter#getFilterName()
	 */
	@Override
	public abstract String getFilterName();
	
	/* (non-Javadoc)
	 * @see peakaboo.filter.model.Filter#getFilterDescription()
	 */
	@Override
	public abstract String getFilterDescription();
	

	/* (non-Javadoc)
	 * @see peakaboo.filter.model.Filter#getFilterType()
	 */
	@Override
	public abstract FilterType getFilterType();


	/* (non-Javadoc)
	 * @see peakaboo.filter.model.Filter#getParameters()
	 */
	@Override
	public final List<Value<?>> getParameters()
	{
		return this.parameters;
	}
	
	/* (non-Javadoc)
	 * @see peakaboo.filter.model.Filter#setParameters(java.util.Map)
	 */
	@Override
	public final void setParameters(List<Value<?>> params)
	{
		parameters = params;
	}
	
	protected void addParameter(Parameter<?> param)
	{
		parameters.add(param);
	}
	
	protected void addParameter(Parameter<?>... params)
	{
		for (Parameter<?> param : params) { addParameter(param); }
	}
	

	/* (non-Javadoc)
	 * @see peakaboo.filter.model.Filter#getParameter(java.lang.Integer)
	 */
	@Override
	public final Value<?> getParameter(Integer key)
	{
		return parameters.get(key);
	}

	protected final void setPreviewCache(ReadOnlySpectrum data)
	{
		this.previewCache = new ISpectrum(data);
	}


	/* (non-Javadoc)
	 * @see peakaboo.filter.model.Filter#initialize()
	 */
	@Override
	public abstract void initialize();
	
	/* (non-Javadoc)
	 * @see peakaboo.filter.model.Filter#getPainter()
	 */
	@Override
	public abstract PlotPainter getPainter();
	

	protected abstract ReadOnlySpectrum filterApplyTo(ReadOnlySpectrum data, boolean cache);
	
	/* (non-Javadoc)
	 * @see peakaboo.filter.model.Filter#canFilterSubset()
	 */
	@Override
	public abstract boolean canFilterSubset();
	
	
	/* (non-Javadoc)
	 * @see peakaboo.filter.model.Filter#filter(scitypes.Spectrum, boolean)
	 */
	@Override
	public ReadOnlySpectrum filter(ReadOnlySpectrum data, boolean cache)
	{
		
		try{
			ReadOnlySpectrum newdata = filterApplyTo(data, cache);
			if (newdata != null) return newdata;
			return data;
		}
		catch(Throwable e)
		{
			System.out.println(getFilterName() + " Filter Failed");
			if (!Version.release) e.printStackTrace();
			return data;
		}
		
	}
	
		
	

	public String toString()
	{
		return this.getFilterName();
	}
	
	
	public boolean isEnabled() {
		return enabled;
	}
	
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
	
	
	
	@Override
	public List<Object> save() {
		List<Object> valuemap = new ArrayList<>();
		for (Value<?> param : getParameters()) {
			Object value = param.getValue();
			if (value instanceof Filter) {
				value = new SerializedFilter((Filter)value);
			}
			valuemap.add(value);
		}
		return valuemap;
	}
	
	@Override
	public void load(List<Object> settings) {
		for (Integer i : new Range(0, settings.size()-1)) {
			Value<Object> param = (Value<Object>) getParameters().get(i);
			Object loaded = settings.get(i);
			if (loaded instanceof SerializedFilter) {
				loaded = ((SerializedFilter) loaded).getFilter();
			}
			param.setValue(loaded);
		}
	}
	

	

	
	
}