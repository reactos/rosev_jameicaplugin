/*
 * PROJECT:    ReactOS Deutschland e.V. Helper Plugin
 * LICENSE:    GNU GPL v2 or any later version as published by the Free Software Foundation
 * COPYRIGHT:  Copyright 2016 ReactOS Deutschland e.V. <deutschland@reactos.org>
 * AUTHORS:    Colin Finck <colin@reactos.org>
 */

package org.reactos.ev.jameicaplugin.gui.dialog;

import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.gui.parts.Button;
import de.willuhn.jameica.gui.parts.TableChangeListener;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import java.rmi.RemoteException;
import java.util.Calendar;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.reactos.ev.jameicaplugin.JameicaPlugin;
import org.reactos.ev.jameicaplugin.rmi.ExchangeRate;

public class ExchangeRates extends AbstractDialog<Object>
{
    private TablePart exchangeRateList;

    public ExchangeRates(int position)
    {
        super(position);
        setSize(SWT.DEFAULT, 600);
        setTitle(JameicaPlugin.i18n().tr("Exchange Rates"));
    }

    @Override
    protected void paint(Composite parent) throws Exception
    {
        final Label intro = new Label(parent, SWT.WRAP);
        intro.setText(String.format(JameicaPlugin.i18n().tr("Enter the data from \"%s\" for the currencies you use here."), "Umsatzsteuer-Umrechnungskurse"));

        final Composite listComposite = new Composite(parent, SWT.NONE);
        listComposite.setLayout(new GridLayout(2, false));
        listComposite.setLayoutData(new GridData(GridData.FILL_BOTH));

        final DBIterator<ExchangeRate> exchangeRates = JameicaPlugin.getDBService().createList(ExchangeRate.class);
        exchangeRates.setOrder("ORDER BY year, month, currency_code");

        exchangeRateList = new TablePart(exchangeRates, null);
        exchangeRateList.addColumn(JameicaPlugin.i18n().tr("Year"), "year", null, true);
        exchangeRateList.addColumn(JameicaPlugin.i18n().tr("Month"), "month", null, true);
        exchangeRateList.addColumn(JameicaPlugin.i18n().tr("Currency Code"), "currency_code", null, true);
        exchangeRateList.addColumn(JameicaPlugin.i18n().tr("Exchange Rate to EUR"), "exchange_rate_to_eur", null, true);
        exchangeRateList.setRememberColWidths(true);
        exchangeRateList.setRememberState(true);
        exchangeRateList.setSummary(false);
        exchangeRateList.addChangeListener(new TableChangeListener()
        {
            @Override
            public void itemChanged(Object object, String attribute, String newValue)
                    throws ApplicationException
            {
                try
                {
                    ExchangeRate er = (ExchangeRate) object;

                    if (attribute.equals("year"))
                        er.setYear(Integer.parseInt(newValue));
                    else if (attribute.equals("month"))
                        er.setMonth(Integer.parseInt(newValue));
                    else if (attribute.equals("currency_code"))
                        er.setCurrencyCode(newValue);
                    else if (attribute.equals("exchange_rate_to_eur"))
                        er.setExchangeRateToEUR(Double.parseDouble(newValue));

                    er.store();
                }
                catch (NumberFormatException e)
                {
                    throw new ApplicationException(JameicaPlugin.i18n().tr("Invalid value"));
                }
                catch (RemoteException e)
                {
                    Logger.error("Unable to change exchange rate", e);
                }
            }
        });
        exchangeRateList.paint(listComposite);

        final Composite sideButtonsComposite = new Composite(listComposite, SWT.NONE);
        sideButtonsComposite.setLayout(new GridLayout());

        final Button addButton = new Button(null, new Action()
        {
            @Override
            public void handleAction(Object context) throws ApplicationException
            {
                try
                {
                    ExchangeRate er = JameicaPlugin.getDBService().createObject(ExchangeRate.class, null);
                    er.setYear(Calendar.getInstance().get(Calendar.YEAR));
                    er.setMonth(0);
                    er.setCurrencyCode("USD");
                    er.setExchangeRateToEUR(0.0);
                    er.store();
                    exchangeRateList.addItem(er);
                }
                catch (RemoteException e)
                {
                    Logger.error("Unable to add exchange rate", e);
                }
            }
        }, null, false, "list-add.png");
        addButton.paint(sideButtonsComposite);

        final Button removeButton = new Button(null, new Action()
        {
            @Override
            public void handleAction(Object context) throws ApplicationException
            {
                try
                {
                    ExchangeRate er = (ExchangeRate) exchangeRateList.getSelection();
                    if (er == null)
                        return;

                    // Just delete the item, it is automatically removed from
                    // the table.
                    er.delete();
                }
                catch (RemoteException e)
                {
                    Logger.error("Unable to remove exchange rate", e);
                }
            }
        }, null, false, "list-remove.png");
        removeButton.paint(sideButtonsComposite);

        Button close = new Button("   " + JameicaPlugin.i18n().tr("Close") + "   ", new Action()
        {
            public void handleAction(Object context) throws ApplicationException
            {
                close();
            }
        });
        close.paint(parent);
    }

    @Override
    protected Object getData() throws Exception
    {
        return null;
    }
}
