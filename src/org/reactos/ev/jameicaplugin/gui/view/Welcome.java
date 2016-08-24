/*
 * PROJECT:    ReactOS Deutschland e.V. Helper Plugin
 * LICENSE:    GNU GPL v2 or any later version as published by the Free Software Foundation
 * COPYRIGHT:  Copyright 2010 ReactOS Deutschland e.V. <deutschland@reactos.org>
 * AUTHORS:    Colin Finck <colin@reactos.org>
 */

package org.reactos.ev.jameicaplugin.gui.view;

import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.util.LabelGroup;
import org.reactos.ev.jameicaplugin.JameicaPlugin;

public class Welcome extends AbstractView
{
    @Override
    public void bind() throws Exception
    {
        GUI.getView().setTitle("ReactOS Deutschland e.V. Helper Plugin");

        LabelGroup group = new LabelGroup(this.getParent(), JameicaPlugin.i18n().tr("Welcome"));
        group.addText(JameicaPlugin.i18n().tr("This plugin extends the foundation management software for ReactOS-specific tasks."), false);
    }
}
