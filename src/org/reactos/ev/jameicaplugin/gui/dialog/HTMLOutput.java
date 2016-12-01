/*
 * PROJECT:    ReactOS Deutschland e.V. Helper Plugin
 * LICENSE:    GNU GPL v2 or any later version as published by the Free Software Foundation
 * COPYRIGHT:  Copyright 2010-2016 ReactOS Deutschland e.V. <deutschland@reactos.org>
 * AUTHORS:    Colin Finck <colin@reactos.org>
 */

package org.reactos.ev.jameicaplugin.gui.dialog;

import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.util.Color;
import de.willuhn.util.ApplicationException;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Text;
import org.reactos.ev.jameicaplugin.JameicaPlugin;
import org.reactos.ev.jameicaplugin.formatter.HTMLFormatter;
import org.reactos.ev.jameicaplugin.rmi.Donation;

public class HTMLOutput extends AbstractDialog<Object>
{
    public HTMLOutput(int position)
    {
        super(position);
        setSize(500, 600);
        setTitle(JameicaPlugin.i18n().tr("HTML Output"));
    }

    @Override
    protected void paint(Composite parent) throws Exception
    {
        DBIterator<Donation> donationList = JameicaPlugin.getDBService().createList(Donation.class);
        HTMLFormatter formatter = new HTMLFormatter();

        final Text text = new Text(parent,
                SWT.BORDER | SWT.MULTI | SWT.READ_ONLY | SWT.WRAP | SWT.V_SCROLL);
        text.setBackground(Color.BACKGROUND.getSWTColor());
        text.setLayoutData(new GridData(GridData.FILL_BOTH));
        text.setText(formatter.format(donationList));

        ButtonArea buttons = new ButtonArea();
        buttons.addButton("   " + JameicaPlugin.i18n().tr("Copy") + "   ", new Action()
        {
            @Override
            public void handleAction(Object context) throws ApplicationException
            {
                final Clipboard cb = new Clipboard(GUI.getDisplay());
                cb.setContents(new Object[]
                { text.getText() }, new Transfer[]
                { TextTransfer.getInstance() });
            }
        });
        buttons.addButton("   " + JameicaPlugin.i18n().tr("Save") + "...   ", new Action()
        {
            @Override
            public void handleAction(Object context) throws ApplicationException
            {
                final FileDialog fd = new FileDialog(GUI.getShell(), SWT.SAVE);
                fd.setFileName("html-output.txt");
                fd.setFilterExtensions(new String[]
                { "*.txt" });
                fd.setOverwrite(false);
                final String f = fd.open();
                if (f == null || f.length() == 0)
                    return;

                try
                {
                    final PrintWriter pw = new PrintWriter(f);
                    pw.write(text.getText());
                    pw.close();
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
        buttons.paint(parent);
    }

    @Override
    protected Object getData() throws Exception
    {
        return null;
    }
}
