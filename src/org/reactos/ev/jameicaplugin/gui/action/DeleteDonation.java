/*
 * PROJECT:    ReactOS Deutschland e.V. Helper Plugin
 * LICENSE:    GNU GPL v2 or any later version as published by the Free Software Foundation
 * COPYRIGHT:  Copyright 2010-2016 ReactOS Deutschland e.V. <deutschland@reactos.org>
 * AUTHORS:    Colin Finck <colin@reactos.org>
 */

package org.reactos.ev.jameicaplugin.gui.action;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.dialogs.YesNoDialog;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import org.reactos.ev.jameicaplugin.JameicaPlugin;
import org.reactos.ev.jameicaplugin.gui.view.PublicDonationList;
import org.reactos.ev.jameicaplugin.rmi.AdditionalDonation;
import org.reactos.ev.jameicaplugin.rmi.Donation;

public class DeleteDonation implements Action
{
    @Override
    public void handleAction(Object context) throws ApplicationException
    {
        Donation[] donations = null;

        if (context instanceof Donation)
        {
            donations = new Donation[1];
            donations[0] = (Donation) context;
        }
        else if (context instanceof Donation[])
        {
            donations = (Donation[]) context;
        }

        if (donations == null || donations.length == 0)
            throw new ApplicationException(JameicaPlugin.i18n().tr("No donation selected"));

        AdditionalDonation[] additional_donations = new AdditionalDonation[donations.length];

        try
        {
            // Create AdditionalDonation objects
            for (int i = 0; i < donations.length; ++i)
            {
                if (donations[i].getJVereinID() != 0)
                {
                    Application.getCallback().notifyUser(JameicaPlugin.i18n().tr("You can only delete additional donations here!"));
                    return;
                }

                additional_donations[i] = (AdditionalDonation) JameicaPlugin.getDBService().createObject(AdditionalDonation.class, donations[i].getID().toString());
            }

            YesNoDialog dlg = new YesNoDialog(YesNoDialog.POSITION_CENTER);
            dlg.setTitle(JameicaPlugin.i18n().tr("Delete donations"));
            dlg.setText(JameicaPlugin.i18n().tr("Do you really want to delete these donations?"));
            if (!(Boolean) dlg.open())
                return;

            for (AdditionalDonation ad : additional_donations)
                ad.delete();

            GUI.getStatusBar().setSuccessText(JameicaPlugin.i18n().tr("Donations deleted"));

            // Update the donation list manually as Jameica cannot know that
            // this affects all_donations.
            PublicDonationList view = (PublicDonationList) GUI.getCurrentView();
            view.getDonationControl().getDonationList();
        }
        catch (Exception e)
        {
            Logger.error("Error while deleting the donation!", e);
        }
    }
}
