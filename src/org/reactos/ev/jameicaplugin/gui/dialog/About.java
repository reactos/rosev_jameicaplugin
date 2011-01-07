/*
 * PROJECT:    ReactOS Deutschland e.V. Helper Plugin
 * LICENSE:    GNU GPL v2 or any later version as published by the Free Software Foundation
 * COPYRIGHT:  Copyright 2010 ReactOS Deutschland e.V. <deutschland@reactos.org>
 * AUTHORS:    Colin Finck <colin@reactos.org>
 */

package org.reactos.ev.jameicaplugin.gui.dialog;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.gui.parts.FormTextPart;
import de.willuhn.jameica.gui.util.ButtonArea;
import de.willuhn.jameica.gui.util.LabelGroup;
import de.willuhn.jameica.gui.util.SWTUtil;
import de.willuhn.util.ApplicationException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.reactos.ev.jameicaplugin.JameicaPlugin;

public class About extends AbstractDialog
{
    public About(int position)
    {
        super(position, false);
        setTitle(JameicaPlugin.i18n().tr("About"));
        setPanelText("ReactOS Deutschland e.V. Helper Plugin");
    }

    protected void paint(Composite parent) throws Exception
    {
        Label l = GUI.getStyleFactory().createLabel(parent, SWT.BORDER);
        l.setImage(SWTUtil.getImage("reactos.jpg"));

        LabelGroup group = new LabelGroup(parent, JameicaPlugin.i18n().tr("About"));

        FormTextPart text = new FormTextPart();
        text.setText("<form>"
                + "<p><b>ReactOS Deutschland e.V. Helper Plugin</b></p>"
                + "<p>Licence: GPLv2 or any later version (http://www.gnu.org/copyleft/gpl.html)</p>"
                + "<p>Copyright 2010 ReactOS Deutschland e.V. (deutschland@reactos.org)</p>"
                + "<p>http://ev.reactos.org</p>" + "</form>");
        group.addPart(text);

        ButtonArea buttons = group.createButtonArea(1);
        buttons.addButton("   " + JameicaPlugin.i18n().tr("Close") + "   ", new Action()
        {
            public void handleAction(Object context) throws ApplicationException
            {
                close();
            }
        });

        getShell().pack();
    }

    protected Object getData() throws Exception
    {
        return null;
    }
}
