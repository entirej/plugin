
package org.entirej.ide.ui.editors.report.gef.ruler;

import org.eclipse.gef.commands.Command;

/*
 * The Class CreateGuideCommand.
 * 
 * @author Chicu Veaceslav
 */
public class CreateGuideCommand extends Command {

	/** The guide. */
	private ReportRulerGuide guide;

	/** The parent. */
	private ReportRuler parent;

	/** The position. */
	private int position;

	/**
	 * Instantiates a new creates the guide command.
	 * 
	 * @param parent
	 *          the parent
	 * @param position
	 *          the position
	 */
	public CreateGuideCommand(ReportRuler parent, int position) {
		super("");
		this.parent = parent;
		this.position = position;
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
		if (guide == null)
			guide = new ReportRulerGuide(!parent.isHorizontal());
		guide.setPosition(position);
		parent.addGuide(guide);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.gef.commands.Command#undo()
	 */
	public void undo() {
		parent.removeGuide(guide);
	}

}
