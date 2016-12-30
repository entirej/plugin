package org.entirej.ide.ui.editors.report.gef.figures;

import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.GridData;
import org.eclipse.draw2d.GridLayout;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.RectangleFigure;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.SWT;
import org.entirej.framework.plugin.reports.EJPluginReportScreenItemProperties;
import org.entirej.framework.plugin.reports.EJPluginReportScreenItemProperties.Line;
import org.entirej.framework.plugin.reports.EJPluginReportScreenItemProperties.Line.LineDirection;

public class ReportFormScreenItemFigure extends RectangleFigure
{
    final EJPluginReportScreenItemProperties model;

    public ReportFormScreenItemFigure(EJPluginReportScreenItemProperties model)
    {
        this.model = model;
        GridLayout manager = new GridLayout();
        manager.marginHeight = 0;
        manager.marginWidth = 0;
        setAlpha(150);
        setLayoutManager(manager);

        switch (model.getType())
        {
            case LINE:
            {
                final EJPluginReportScreenItemProperties.Line line = (Line) model;
                Figure figure = new Figure()
                {
                    protected void paintFigure(Graphics graphics)
                    {
                      super.paintFigure(graphics);
                      Rectangle clientArea = getClientArea();
                      Point top = clientArea.getTop();
                      Point right = clientArea.getRight();
                      final int oldWid = graphics.getLineWidth();
                      graphics.setLineWidth((int) Math.ceil(line.getLineWidth()));
                      switch (line.getLineStyle())
                      {
                          case DOTTED:
                              graphics.setLineStyle(SWT.LINE_DOT);
                              break;
                          case DASHED:
                              graphics.setLineStyle(SWT.LINE_DASH);
                              break;

                          default:
                              graphics.setLineStyle(SWT.LINE_SOLID);
                              break;
                      }
                      if (line.getLineDirection() == LineDirection.TO_DOWN)
                      {
                        graphics.drawLine(clientArea.getLeft(), right);
                        
                      }
                      else
                      {
                        graphics.drawLine(clientArea.getBottom(), top);
                     
                      }
                 

                      graphics.setLineWidth(oldWid);

                    }
                };
                figure.setToolTip(new Label(model.getName()));
                add(figure);

                
                getLayoutManager().setConstraint(figure, new GridData(GridData.FILL, GridData.FILL, true, true));
            }
                break;

            default:
                Label figure = new Label(model.getName());
                add(figure);

               
                getLayoutManager().setConstraint(figure, new GridData(GridData.FILL, GridData.FILL, false, false));
                break;
        }

    }

}
