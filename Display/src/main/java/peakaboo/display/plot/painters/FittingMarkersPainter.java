package peakaboo.display.plot.painters;

import java.awt.Color;

import peakaboo.curvefit.curve.fitting.FittingParameters;
import peakaboo.curvefit.curve.fitting.FittingResult;
import peakaboo.curvefit.curve.fitting.FittingResultSet;
import peakaboo.curvefit.peak.escape.EscapePeakType;
import peakaboo.curvefit.peak.fitting.FittingFunction;
import peakaboo.curvefit.peak.transition.Transition;
import peakaboo.curvefit.peak.transition.TransitionSeries;
import scidraw.drawing.DrawingRequest;
import scidraw.drawing.painters.PainterData;
import scidraw.drawing.plot.PlotDrawing;
import scidraw.drawing.plot.painters.PlotPainter;
import scitypes.ISpectrum;
import scitypes.Spectrum;


/**
 * 
 * A {@link PlotPainter} for {@link PlotDrawing}s which draws lines at the centrepoints of {@link Transition}s
 * 
 * @author Nathaniel Sherry, 2009
 * 
 */

public class FittingMarkersPainter extends PlotPainter
{

	private FittingResultSet	fitResults;
	private EscapePeakType		escapeType;
	private Color				colour;

	/**
	 * Create a FittingMarkersPainter
	 * @param fitResults the {@link FittingResultSet} for the data being drawn
	 * @param escapeType the {@link EscapePeakType} used to generate the {@link FittingResultSet}
	 * @param c the {@link Color} to use when drawing the markings
	 */
	public FittingMarkersPainter(FittingResultSet fitResults, EscapePeakType escapeType, Color c)
	{
		this.fitResults = fitResults;
		this.escapeType = escapeType;
		this.colour = new Color(c.getRed(), c.getGreen(), c.getBlue());
	}


	@Override
	public void drawElement(PainterData p)
	{
		DrawingRequest dr = p.dr;
		float channel, markerHeight;
		Spectrum markerHeights = new ISpectrum(dr.dataWidth);

		p.context.save();
		p.context.setLineWidth(1.0f);
		p.context.setSource(colour);
		
		for (FittingResult fit : fitResults.getFits()) {

			
			for (int i = 0; i < p.dr.dataWidth; i++) {
				markerHeights.set(i, 0.0f);
			}

			TransitionSeries ts = fit.getTransitionSeries();
			for (Transition t : ts) {

				channel = fitResults.getParameters().getCalibration().fractionalChannelFromEnergy(t.energyValue);
				if (channel >= p.dr.dataWidth || channel < 0) continue;
				
				FittingParameters parameters = fitResults.getParameters();
				FittingFunction fitFn = parameters.forTransition(t, ts.type);

				
				//get a height value from the fitting function, then apply the same transformation as the fitting did
				markerHeight = fitFn.forEnergy(t.energyValue) * fit.getTotalScale();
							
				//markerHeights.set((int) channel, markerHeight);
				
				float positionX = getXForChannel(p, channel);
				
				markerHeight = transformValueForPlot(p.dr, markerHeight);
				
				p.context.moveTo(positionX, p.plotSize.y);
				p.context.lineTo(positionX, p.plotSize.y * (1.0f - markerHeight) );
				
				
				if (escapeType.get().hasOffset())
				{
					for (Transition esc : escapeType.get().offset()) {
					
						channel = fitResults.getParameters().getCalibration().fractionalChannelFromEnergy(t.energyValue - esc.energyValue);
						if (channel < 0) continue;
						
						positionX = getXForChannel(p, channel);
						
						FittingFunction escFn = parameters.forEscape(t, esc, ts.element, ts.type);
						markerHeight = escFn.forEnergy(t.energyValue) * fit.getTotalScale();
						//markerHeight *= Curve.escapeIntensity(fit.getTransitionSeries().element);
						//markerHeight *= esc.relativeIntensity;
						markerHeight = transformValueForPlot(p.dr, markerHeight);
						
					
						p.context.moveTo(positionX, p.plotSize.y);
						p.context.lineTo(positionX, p.plotSize.y * (1.0f - markerHeight) );
						
					}
				}


			}

			p.context.stroke();

		}
		
		
		p.context.restore();
		
	}

	public float getXForChannel(PainterData p, float channel)
	{
		float channelWidth = p.plotSize.x / p.dr.dataWidth;
		return channel * channelWidth;
	}
	

}
