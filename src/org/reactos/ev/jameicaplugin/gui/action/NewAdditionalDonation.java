/*
 * PROJECT:    ReactOS Deutschland e.V. Helper Plugin
 * LICENSE:    GNU GPL v2 or any later version as published by the Free Software Foundation
 * COPYRIGHT:  Copyright 2010-2016 ReactOS Deutschland e.V. <deutschland@reactos.org>
 * AUTHORS:    Colin Finck <colin@reactos.org>
 */

package org.reactos.ev.jameicaplugin.gui.action;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import java.rmi.RemoteException;
import org.reactos.ev.jameicaplugin.JameicaPlugin;
import org.reactos.ev.jameicaplugin.gui.view.AdditionalDonationDetail;
import org.reactos.ev.jameicaplugin.rmi.AdditionalDonation;

public class NewAdditionalDonation implements Action
{
    @Override
    public void handleAction(Object context) throws ApplicationException
    {
        AdditionalDonation ad;

        try
        {
            ad = (AdditionalDonation) JameicaPlugin.getDBService().createObject(AdditionalDonation.class, null);
            GUI.startView(AdditionalDonationDetail.class, ad);
        }
        catch (RemoteException e)
        {
            Logger.error("Error while creating a new additional donation", e);
        }
    }
}
