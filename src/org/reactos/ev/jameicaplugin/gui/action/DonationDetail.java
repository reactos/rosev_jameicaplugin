/*
 * PROJECT:    ReactOS Deutschland e.V. Helper Plugin
 * LICENSE:    GNU GPL v2 or any later version as published by the Free Software Foundation
 * COPYRIGHT:  Copyright 2010 ReactOS Deutschland e.V. <deutschland@reactos.org>
 * AUTHORS:    Colin Finck <colin@reactos.org>
 */

package org.reactos.ev.jameicaplugin.gui.action;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.gui.action.BuchungAction;
import de.jost_net.JVerein.rmi.Buchung;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.util.ApplicationException;
import java.rmi.RemoteException;
import org.reactos.ev.jameicaplugin.JameicaPlugin;
import org.reactos.ev.jameicaplugin.gui.view.AdditionalDonationDetail;
import org.reactos.ev.jameicaplugin.rmi.AdditionalDonation;
import org.reactos.ev.jameicaplugin.rmi.Donation;

public class DonationDetail implements Action
{
    public void handleAction(Object context) throws ApplicationException
    {
        Donation d = (Donation) context;

        try
        {
            if (d.getJVereinID() == 0)
            {
                // This is a donation in the "additional_donations" table.
                // Open our editor.
                AdditionalDonation ad = (AdditionalDonation) JameicaPlugin.getDBService().createObject(AdditionalDonation.class, d.getID().toString());
                GUI.startView(AdditionalDonationDetail.class, ad);
            }
            else
            {
                // This is a donation added through a JVerein booking.
                // Open it in JVerein's editor.
                Buchung b = (Buchung) Einstellungen.getDBService().createObject(Buchung.class, d.getJVereinID().toString());
                new BuchungAction(false).handleAction(b);
            }
        }
        catch (RemoteException e)
        {
            throw new ApplicationException("Error while handling the donation action!");
        }
    }
}
