/*
 * PROJECT:    ReactOS Deutschland e.V. Helper Plugin
 * LICENSE:    GNU GPL v2 or any later version as published by the Free Software Foundation
 * COPYRIGHT:  Copyright 2016 ReactOS Deutschland e.V. <deutschland@reactos.org>
 * AUTHORS:    Colin Finck <colin@reactos.org>
 */

package org.reactos.ev.jameicaplugin.gui.action;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

public class ExchangeRates implements Action
{
    @Override
    public void handleAction(Object context) throws ApplicationException
    {
        try
        {
            new org.reactos.ev.jameicaplugin.gui.dialog.ExchangeRates(
                    AbstractDialog.POSITION_CENTER).open();
        }
        catch (Exception e)
        {
            Logger.error("Error while opening the Exchange Rates dialog", e);
        }
    }
}
