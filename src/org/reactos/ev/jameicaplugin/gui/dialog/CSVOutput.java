/*
 * PROJECT:    ReactOS Deutschland e.V. Helper Plugin
 * LICENSE:    GNU GPL v2 or any later version as published by the Free Software Foundation
 * COPYRIGHT:  Copyright 2010-2018 ReactOS Deutschland e.V. <deutschland@reactos.org>
 * AUTHORS:    Colin Finck <colin@reactos.org>
 */

package org.reactos.ev.jameicaplugin.gui.dialog;

import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.gui.input.TextInput;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.util.SimpleContainer;
import de.willuhn.util.ApplicationException;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.rmi.RemoteException;
import java.util.Calendar;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Listener;
import org.reactos.ev.jameicaplugin.JameicaPlugin;
import org.reactos.ev.jameicaplugin.formatter.CSVFormatter;
import org.reactos.ev.jameicaplugin.rmi.Donation;

public class CSVOutput extends AbstractDialog<Object>
{
    public CSVOutput(int position)
    {
        super(position);
        setTitle(JameicaPlugin.i18n().tr("CSV Output"));
    }

    @Override
    protected void paint(Composite parent) throws Exception
    {
        final Calendar cal = Calendar.getInstance();
        final TextInput year = new TextInput(Integer.toString(cal.get(Calendar.YEAR)));
        year.setName(JameicaPlugin.i18n().tr("Year"));
        year.addListener(new Listener()
        {
            public void handleEvent(Event event)
            {
                try
                {
                    if (((String) year.getValue()).length() != 4)
                        throw new NumberFormatException();

                    Integer.parseInt((String) year.getValue());
                }
                catch (NumberFormatException e)
                {
                    year.setValue(cal.get(Calendar.YEAR));
                }
            }
        });

        ButtonArea buttons = new ButtonArea();
        buttons.addButton("   " + JameicaPlugin.i18n().tr("Save") + "...   ", new Action()
        {
            @Override
            public void handleAction(Object context) throws ApplicationException
            {
                final FileDialog fd = new FileDialog(GUI.getShell(), SWT.SAVE);
                fd.setFileName(year.getValue() + ".csv");
                fd.setFilterExtensions(new String[]
                { "*.csv" });
                fd.setFilterPath(System.getProperty("user.home"));
                fd.setOverwrite(false);
                final String f = fd.open();
                if (f == null || f.length() == 0)
                    return;

                try
                {
                    DBIterator<Donation> donationList = JameicaPlugin.getDBService().createList(Donation.class);
                    CSVFormatter formatter = new CSVFormatter(
                            Integer.parseInt((String) year.getValue()));

                    final PrintWriter pw = new PrintWriter(f);
                    pw.write(formatter.format(donationList));
                    pw.close();

                    close();
                }
                catch (RemoteException e)
                {
                    throw new ApplicationException(e);
                }
                catch (FileNotFoundException e)
                {
                }
            }
        });
        buttons.addButton("   " + JameicaPlugin.i18n().tr("Close") + "   ", new Action()
        {
            @Override
            public void handleAction(Object context) throws ApplicationException
            {
                close();
            }
        });

        SimpleContainer container = new SimpleContainer(parent);
        container.addInput(year);
        container.addButtonArea(buttons);
    }

    @Override
    protected Object getData() throws Exception
    {
        return null;
    }
}
