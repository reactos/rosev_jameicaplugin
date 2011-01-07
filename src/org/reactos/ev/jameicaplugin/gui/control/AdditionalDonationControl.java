/*
 * PROJECT:    ReactOS Deutschland e.V. Helper Plugin
 * LICENSE:    GNU GPL v2 or any later version as published by the Free Software Foundation
 * COPYRIGHT:  Copyright 2010 ReactOS Deutschland e.V. <deutschland@reactos.org>
 * AUTHORS:    Colin Finck <colin@reactos.org>
 */

package org.reactos.ev.jameicaplugin.gui.control;

import de.willuhn.jameica.gui.AbstractControl;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.input.CheckboxInput;
import de.willuhn.jameica.gui.input.DateInput;
import de.willuhn.jameica.gui.input.DecimalInput;
import de.willuhn.jameica.gui.input.TextInput;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import java.rmi.RemoteException;
import java.util.Date;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.reactos.ev.jameicaplugin.JameicaPlugin;
import org.reactos.ev.jameicaplugin.rmi.AdditionalDonation;

public class AdditionalDonationControl extends AbstractControl
{
    private AdditionalDonation donation;
    private TextInput id;
    private DateInput date;
    private TextInput name;
    private DecimalInput amount;
    private CheckboxInput anonymous;
    private TextInput currency;
    private TextInput comment;

    public AdditionalDonationControl(AbstractView view)
    {
        super(view);
    }

    private AdditionalDonation getAdditionalDonation() throws RemoteException
    {
        if (donation != null)
            return donation;

        donation = (AdditionalDonation) getCurrentObject();
        if (donation == null)
            donation = (AdditionalDonation) JameicaPlugin.getDBService().createObject(AdditionalDonation.class, null);

        return donation;
    }

    public TextInput getID() throws RemoteException
    {
        if (id != null)
            return id;

        id = new TextInput(getAdditionalDonation().getID(), 10);
        id.setEnabled(false);
        return id;
    }

    public DateInput getDate() throws RemoteException
    {
        if (date != null)
            return date;

        date = new DateInput(getAdditionalDonation().getDate(), JameicaPlugin.dateFormat);
        date.addListener(new Listener()
        {
            public void handleEvent(Event event)
            {
                // Auto-parse the entered date when switching focus to the next
                // input field.
                // Code taken from JVerein.
                Date dt = (Date) date.getValue();
                if (dt == null)
                    return;
            }
        });

        return date;
    }

    public TextInput getName() throws RemoteException
    {
        if (name != null)
            return name;

        name = new TextInput(getAdditionalDonation().getName(), 100);
        return name;
    }

    public CheckboxInput getAnonymous() throws RemoteException
    {
        if (anonymous != null)
            return anonymous;

        anonymous = new CheckboxInput(getAdditionalDonation().isAnonymous());
        return anonymous;
    }

    public DecimalInput getAmount() throws RemoteException
    {
        if (amount != null)
            return amount;

        amount = new DecimalInput(getAdditionalDonation().getAmount(), JameicaPlugin.currencyFormat);
        return amount;
    }

    public TextInput getCurrency() throws RemoteException
    {
        if (currency != null)
            return currency;

        currency = new TextInput(getAdditionalDonation().getCurrency(), 3);
        return currency;
    }

    public TextInput getComment() throws RemoteException
    {
        if (comment != null)
            return comment;

        comment = new TextInput(getAdditionalDonation().getComment(), 1000);
        return comment;
    }

    public void handleStore()
    {
        try
        {
            AdditionalDonation ad = getAdditionalDonation();
            ad.setDate((Date) getDate().getValue());
            ad.setName((String) getName().getValue());
            ad.setAnonymous((Boolean) getAnonymous().getValue());
            ad.setAmount((Double) getAmount().getValue());
            ad.setCurrency((String) getCurrency().getValue());
            ad.setComment((String) getComment().getValue());

            try
            {
                ad.store();
                getID().setValue(ad.getID());
                GUI.getStatusBar().setSuccessText(JameicaPlugin.i18n().tr("Donation saved"));
            }
            catch (ApplicationException e)
            {
                GUI.getStatusBar().setErrorText(e.getMessage());
            }
        }
        catch (RemoteException e)
        {
            Logger.error("Error while saving the donation!", e);
        }
    }
}
