
package org.entirej.ide.ui.editors.report.gef.ruler;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.gef.commands.Command;


public class DeleteGuideCommand extends Command {

	/** The parent. */
	private ReportRuler parent;

	/** The guide. */
	private ReportRulerGuide guide;

	/** The old parts. */
	private Map<IGuidebleElement, Integer> oldParts;

	/**
	 * Instantiates a new delete guide command.
	 * 
	 * @param guide
	 *          the guide
	 * @param parent
	 *          the parent
	 */
	public DeleteGuideCommand(ReportRulerGuide guide, ReportRuler parent) {
		super("");
		this.guide = guide;
		this.parent = parent;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.gef.commands.Command#canUndo()
	 */
	public boolean canUndo() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.gef.commands.Command#execute()
	 */
	public void execute() {
		oldParts = new HashMap<IGuidebleElement, Integer>(guide.getMap());
		for (IGuidebleElement part : guide.getParts()) {
			guide.detachPart(part);
		}
		parent.removeGuide(guide);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.gef.commands.Command#undo()
	 */
	public void undo() {
		parent.addGuide(guide);
		for (IGuidebleElement part : guide.getParts()) {
			guide.attachPart(part, (oldParts.get(part)).intValue());
		}
	}
}
