/*
 * PROJECT:    ReactOS Deutschland e.V. Helper Plugin
 * LICENSE:    GNU GPL v2 or any later version as published by the Free Software Foundation
 * COPYRIGHT:  Copyright 2010 ReactOS Deutschland e.V. <deutschland@reactos.org>
 * AUTHORS:    Colin Finck <colin@reactos.org>
 */

package org.reactos.ev.jameicaplugin.gui.menu;

import de.willuhn.jameica.gui.parts.CheckedSingleContextMenuItem;
import de.willuhn.jameica.gui.parts.ContextMenu;
import de.willuhn.jameica.gui.parts.ContextMenuItem;
import org.reactos.ev.jameicaplugin.JameicaPlugin;
import org.reactos.ev.jameicaplugin.gui.action.DeleteDonation;
import org.reactos.ev.jameicaplugin.gui.action.DonationDetail;
import org.reactos.ev.jameicaplugin.gui.action.NewAdditionalDonation;
import org.reactos.ev.jameicaplugin.gui.control.DonationControl;

public class DonationMenu extends ContextMenu
{
    public DonationMenu(DonationControl control)
    {
        addItem(new ContextMenuItem(JameicaPlugin.i18n().tr("New additional donation"),
                new NewAdditionalDonation(), "document-new.png"));
        addItem(new CheckedSingleContextMenuItem(JameicaPlugin.i18n().tr("Edit"),
                new DonationDetail(), "edit.png"));
        addItem(new ContextMenuItem(JameicaPlugin.i18n().tr("Delete"), new DeleteDonation(),
                "user-trash.png"));
    }
}
