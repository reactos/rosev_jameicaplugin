/*
 * PROJECT:    ReactOS Deutschland e.V. Helper Plugin
 * LICENSE:    GNU GPL v2 or any later version as published by the Free Software Foundation
 * COPYRIGHT:  Copyright 2010 ReactOS Deutschland e.V. <deutschland@reactos.org>
 * AUTHORS:    Colin Finck <colin@reactos.org>
 */

package org.reactos.ev.jameicaplugin.gui.control;

import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.gui.AbstractControl;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.Part;
import de.willuhn.jameica.gui.formatter.CurrencyFormatter;
import de.willuhn.jameica.gui.formatter.DateFormatter;
import de.willuhn.jameica.gui.formatter.Formatter;
import de.willuhn.jameica.gui.input.CheckboxInput;
import de.willuhn.jameica.gui.parts.Column;
import de.willuhn.jameica.gui.parts.TablePart;
import java.rmi.RemoteException;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.reactos.ev.jameicaplugin.JameicaPlugin;
import org.reactos.ev.jameicaplugin.gui.action.DonationDetail;
import org.reactos.ev.jameicaplugin.gui.menu.DonationMenu;
import org.reactos.ev.jameicaplugin.rmi.Donation;

public class DonationControl extends AbstractControl
{
    private TablePart donationList;
    private CheckboxInput showInvalidCheckbox;

    public DonationControl(AbstractView view)
    {
        super(view);
    }

    public Part getDonationList() throws RemoteException
    {
        DBIterator donations = JameicaPlugin.getDBService().createList(Donation.class);

        // Invalid donations have "amount" set to zero due to the way the VIEW
        // is defined
        if (showInvalidCheckbox.isEnabled())
            donations.addFilter("amount = 0");

        if (donationList == null)
        {
            donationList = new TablePart(donations, new DonationDetail());
            donationList.addColumn(JameicaPlugin.i18n().tr("Date"), "date", new DateFormatter(
                    JameicaPlugin.dateFormat));
            donationList.addColumn(JameicaPlugin.i18n().tr("Name"), "name");
            donationList.addColumn(JameicaPlugin.i18n().tr("Amount"), "amount", new CurrencyFormatter(
                    "", JameicaPlugin.currencyFormat));
            donationList.addColumn(JameicaPlugin.i18n().tr("Currency"), "currency");
            donationList.addColumn(JameicaPlugin.i18n().tr("Source"), "jverein_id", new Formatter()
            {
                public String format(Object o)
                {
                    if ((Long) o == 0)
                        return JameicaPlugin.i18n().tr("Additional donation");
                    else
                        return "JVerein";
                }
            }, false, Column.ALIGN_LEFT);
            donationList.addColumn(JameicaPlugin.i18n().tr("Comment"), "comment");

            donationList.setMulti(true);
            donationList.setContextMenu(new DonationMenu(this));
            donationList.setRememberColWidths(true);
            donationList.setRememberOrder(true);
            donationList.setRememberState(true);
            donationList.setSummary(true);
        }
        else
        {
            donationList.removeAll();
            while (donations.hasNext())
                donationList.addItem(donations.next());
        }

        return donationList;
    }

    public CheckboxInput getShowInvalidCheckbox() throws RemoteException
    {
        if (showInvalidCheckbox != null)
            return showInvalidCheckbox;

        showInvalidCheckbox = new CheckboxInput(false);
        showInvalidCheckbox.setName(JameicaPlugin.i18n().tr("Show invalid donations only"));
        showInvalidCheckbox.addListener(new Listener()
        {
            public void handleEvent(Event event)
            {
                if (showInvalidCheckbox.hasChanged())
                {
                    try
                    {
                        getDonationList();
                    }
                    catch (RemoteException e)
                    {
                        e.printStackTrace();
                    }
                }
            }
        });

        return showInvalidCheckbox;
    }
}
