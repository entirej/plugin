
package org.entirej.ide.ui.editors.report.gef.ruler;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.commands.Command;


public class MoveGuideCommand extends Command {
	/** The p delta. */
	private int pDelta;

	/** The guide. */
	private ReportRulerGuide guide;

	/**
	 * Instantiates a new move guide command.
	 * 
	 * @param guide
	 *          the guide
	 * @param positionDelta
	 *          the position delta
	 */
	public MoveGuideCommand(ReportRulerGuide guide, int positionDelta) {
		super("");
		this.guide = guide;
		pDelta = positionDelta;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.gef.commands.Command#execute()
	 */
	public void execute() {
		
		guide.setPosition(guide.getPosition() + pDelta);
		
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.gef.commands.Command#undo()
	 */
	public void undo() {
		guide.setPosition(guide.getPosition() - pDelta);
		
	}

}
