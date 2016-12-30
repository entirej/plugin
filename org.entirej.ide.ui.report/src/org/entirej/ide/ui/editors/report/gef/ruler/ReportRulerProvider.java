
package org.entirej.ide.ui.editors.report.gef.ruler;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.gef.commands.Command;
import org.eclipse.gef.rulers.RulerChangeListener;
import org.eclipse.gef.rulers.RulerProvider;
import org.eclipse.jface.util.IPropertyChangeListener;



public class ReportRulerProvider extends RulerProvider {

	/** The ruler. */
	private ReportRuler ruler;

	/** The ruler listener. */
	private PropertyChangeListener rulerListener = new PropertyChangeListener() {
		public void propertyChange(PropertyChangeEvent evt) {
			if (evt.getPropertyName().equals(ReportRuler.PROPERTY_CHILDREN)) {
				ReportRulerGuide guide = (ReportRulerGuide) evt.getNewValue();
				if (getGuides().contains(guide)) {
					guide.addPropertyChangeListener(guideListener);
				} else {
					guide.removePropertyChangeListener(guideListener);
				}
				for (int i = 0; i < listeners.size(); i++) {
					((RulerChangeListener) listeners.get(i)).notifyGuideReparented(guide);
				}
			} else {
				int newUnit = getUnit();
				for (int i = 0; i < listeners.size(); i++) {
					((RulerChangeListener) listeners.get(i)).notifyUnitsChanged(newUnit);
				}
			}
		}
	};

	/** The guide listener. */
	private PropertyChangeListener guideListener = new PropertyChangeListener() {
		public void propertyChange(PropertyChangeEvent evt) {
			if (evt.getPropertyName().equals(ReportRulerGuide.PROPERTY_CHILDREN)) {
				for (int i = 0; i < listeners.size(); i++) {
					((RulerChangeListener) listeners.get(i)).notifyPartAttachmentChanged(evt.getNewValue(), evt.getSource());
				}
			} else {
				for (int i = 0; i < listeners.size(); i++) {
					((RulerChangeListener) listeners.get(i)).notifyGuideMoved(evt.getSource());
				}
			}
		}
	};
	
	
	
	/**
	 * Instantiates a new report ruler provider.
	 * 
	 * @param ruler
	 *          the ruler
	 */
	public ReportRulerProvider(ReportRuler ruler) {
		this.ruler = ruler;
		this.ruler.addPropertyChangeListener(rulerListener);
		List<ReportRulerGuide> guides = getGuides();
		for (int i = 0; i < guides.size(); i++) {
			(guides.get(i)).addPropertyChangeListener(guideListener);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.gef.rulers.RulerProvider#getAttachedModelObjects(java.lang.Object)
	 */
	public List<Object> getAttachedModelObjects(Object guide) {
		return new ArrayList<Object>(((ReportRulerGuide) guide).getParts());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.gef.rulers.RulerProvider#getCreateGuideCommand(int)
	 */
	public Command getCreateGuideCommand(int position) {
		return new CreateGuideCommand(ruler, position);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.gef.rulers.RulerProvider#getDeleteGuideCommand(java.lang.Object)
	 */
	public Command getDeleteGuideCommand(Object guide) {
		return new DeleteGuideCommand((ReportRulerGuide) guide, ruler);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.gef.rulers.RulerProvider#getMoveGuideCommand(java.lang.Object, int)
	 */
	public Command getMoveGuideCommand(Object guide, int pDelta) {
		return new MoveGuideCommand((ReportRulerGuide) guide, pDelta);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.gef.rulers.RulerProvider#getGuidePositions()
	 */
	public int[] getGuidePositions() {
		List<ReportRulerGuide> guides = getGuides();
		int[] result = new int[guides.size()];
		for (int i = 0; i < guides.size(); i++) {
			result[i] = (guides.get(i)).getPosition();
		}
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.gef.rulers.RulerProvider#getRuler()
	 */
	public Object getRuler() {
		return ruler;
	}

	/**
	 * The unit is read directly form the preferences. If the unit is not found the
	 * default value (pixel) is used
	 */
	@Override
	public int getUnit() {
		
		return RulerProvider.UNIT_PIXELS;
	}
	
	/**
	 * Set the unit of the ruler in the preferences. The listener will then automatically refresh the ruler.
	 * The operation is done only if the new unit is different from the old one
	 */
	@Override
	public void setUnit(int newUnit) {
		
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.gef.rulers.RulerProvider#getGuidePosition(java.lang.Object)
	 */
	public int getGuidePosition(Object guide) {
		return ((ReportRulerGuide) guide).getPosition();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.gef.rulers.RulerProvider#getGuides()
	 */
	public List<ReportRulerGuide> getGuides() {
		return ruler.getGuides();
	}

}
