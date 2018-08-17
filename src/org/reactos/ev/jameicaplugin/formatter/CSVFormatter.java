/*
 * PROJECT:    ReactOS Deutschland e.V. Helper Plugin
 * LICENSE:    GNU GPL v2 or any later version as published by the Free Software Foundation
 * COPYRIGHT:  Copyright 2010-2018 ReactOS Deutschland e.V. <deutschland@reactos.org>
 * AUTHORS:    Colin Finck <colin@reactos.org>
 */

package org.reactos.ev.jameicaplugin.formatter;

import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.gui.formatter.Formatter;
import de.willuhn.logging.Logger;
import java.rmi.RemoteException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import org.reactos.ev.jameicaplugin.JameicaPlugin;
import org.reactos.ev.jameicaplugin.rmi.Donation;

public class CSVFormatter implements Formatter
{
    private static final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private int year;

    public CSVFormatter(int year)
    {
        this.year = year;
    }

    @Override
    public String format(Object o)
    {
        @SuppressWarnings("unchecked")
        DBIterator<Donation> donationList = (DBIterator<Donation>) o;

        String csv = "";

        try
        {
            final Calendar cal = Calendar.getInstance();

            cal.set(year, 0, 0, 0, 0, 0);
            donationList.addFilter("date >= ?", cal.getTime());

            cal.set(year + 1, 0, 0, 0, 0, 0);
            donationList.addFilter("date < ?", cal.getTime());

            donationList.setOrder("ORDER BY date DESC");

            while (donationList.hasNext())
            {
                Donation d = (Donation) donationList.next();

                if (d.isAnonymous())
                    csv += "Anonymous";
                else
                    csv += d.getName();

                csv += String.format(";%s", dateFormat.format(d.getDate()));
                csv += String.format(";%s %s", d.getCurrency(), JameicaPlugin.currencyFormatUS.format(d.getAmount()));
                csv += "\n";
            }
        }
        catch (RemoteException e)
        {
            Logger.error("Error while formatting as CSV", e);
        }

        return csv;
    }
}
