/*
 * PROJECT:    ReactOS Deutschland e.V. Helper Plugin
 * LICENSE:    GNU GPL v2 or any later version as published by the Free Software Foundation
 * COPYRIGHT:  Copyright 2016 ReactOS Deutschland e.V. <deutschland@reactos.org>
 * AUTHORS:    Colin Finck <colin@reactos.org>
 */

package org.reactos.ev.jameicaplugin.gui.control;

import de.willuhn.jameica.gui.AbstractControl;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.formatter.CurrencyFormatter;
import de.willuhn.jameica.gui.formatter.DateFormatter;
import de.willuhn.jameica.gui.parts.Column;
import de.willuhn.jameica.gui.parts.TableChangeListener;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import java.rmi.RemoteException;
import org.reactos.ev.jameicaplugin.JameicaPlugin;
import org.reactos.ev.jameicaplugin.formatter.BooleanFormatter;
import org.reactos.ev.jameicaplugin.io.Transaction;

public class TransactionTableControl extends AbstractControl
{
    private Boolean changeable;
    private TablePart table;

    public TransactionTableControl(AbstractView view, Boolean changeable)
    {
        super(view);
        this.changeable = changeable;
    }

    public TablePart getTable()
    {
        if (table != null)
            return table;

        // Add the transaction table.
        // As this list is empty at start, Jameica has no means of figuring out
        // the value types and thus doesn't know when to right-align columns.
        // Therefore, we set the alignment manually here.
        table = new TablePart(null);
        table.addColumn("ID", "id");
        table.addColumn(JameicaPlugin.i18n().tr("Date"), "date", new DateFormatter(
                JameicaPlugin.dateFormat));
        table.addColumn(JameicaPlugin.i18n().tr("Name"), "name", null, changeable);
        table.addColumn(JameicaPlugin.i18n().tr("Gross Amount"), "grossamount", new CurrencyFormatter(
                "", JameicaPlugin.currencyFormat), false, Column.ALIGN_RIGHT);
        table.addColumn(JameicaPlugin.i18n().tr("Net Amount"), "netamount", new CurrencyFormatter(
                "", JameicaPlugin.currencyFormat), false, Column.ALIGN_RIGHT);
        table.addColumn(JameicaPlugin.i18n().tr("Anonymous"), "anonymous", new BooleanFormatter(), changeable);
        table.addColumn(JameicaPlugin.i18n().tr("Comment"), "comment");
        table.addColumn(JameicaPlugin.i18n().tr("Type"), "type");

        if (changeable)
        {
            // Enable the user to edit some fields of the transaction list to
            // correct mistakes.
            table.addChangeListener(new TableChangeListener()
            {
                @Override
                public void itemChanged(Object object, String attribute, String newValue)
                        throws ApplicationException
                {
                    Transaction t = (Transaction) object;

                    if (attribute.equals("name"))
                    {
                        t.setName(newValue);
                    }
                    else if (attribute.equals("anonymous"))
                    {
                        Boolean b = false;
                        if (newValue.equalsIgnoreCase("x"))
                            b = true;

                        t.setAnonymous(b);
                    }

                    // Ensure that the table shows exactly the data from the
                    // Transaction object.
                    try
                    {
                        table.updateItem(t, t);
                    }
                    catch (RemoteException e)
                    {
                        Logger.error("Unable to reload transactions", e);
                    }
                }
            });
        }

        table.setMulti(true);
        table.setRememberColWidths(true);
        table.setRememberOrder(true);
        table.setRememberState(true);
        table.setSummary(true);

        return table;
    }
}
