/*
 * PROJECT:    ReactOS Deutschland e.V. Helper Plugin
 * LICENSE:    GNU GPL v2 or any later version as published by the Free Software Foundation
 * COPYRIGHT:  Copyright 2010 ReactOS Deutschland e.V. <deutschland@reactos.org>
 * AUTHORS:    Colin Finck <colin@reactos.org>
 */

package org.reactos.ev.jameicaplugin.gui.action;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.util.ApplicationException;

public class About implements Action
{
    public void handleAction(Object context) throws ApplicationException
    {
        try
        {
            new org.reactos.ev.jameicaplugin.gui.dialog.About(AbstractDialog.POSITION_CENTER).open();
        }
        catch (Exception e)
        {
            throw new ApplicationException("Error while opening the About dialog", e);
        }
    }
}
