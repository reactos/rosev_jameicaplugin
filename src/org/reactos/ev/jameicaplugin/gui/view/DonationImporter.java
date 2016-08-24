/*
 * PROJECT:    ReactOS Deutschland e.V. Helper Plugin
 * LICENSE:    GNU GPL v2 or any later version as published by the Free Software Foundation
 * COPYRIGHT:  Copyright 2016 ReactOS Deutschland e.V. <deutschland@reactos.org>
 * AUTHORS:    Colin Finck <colin@reactos.org>
 */

package org.reactos.ev.jameicaplugin.gui.view;

import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.gui.input.DateInput;
import de.willuhn.jameica.gui.internal.buttons.Cancel;
import de.willuhn.jameica.gui.parts.Button;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.util.SimpleContainer;
import de.willuhn.jameica.gui.util.TabGroup;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.jameica.system.Settings;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import java.rmi.RemoteException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.TabFolder;
import org.reactos.ev.jameicaplugin.JameicaPlugin;
import org.reactos.ev.jameicaplugin.gui.action.ExchangeRates;
import org.reactos.ev.jameicaplugin.gui.action.PayPalAPICredentials;
import org.reactos.ev.jameicaplugin.gui.control.TransactionTabControl;
import org.reactos.ev.jameicaplugin.io.JVereinIO;
import org.reactos.ev.jameicaplugin.io.PayPalIO;
import org.reactos.ev.jameicaplugin.io.Transaction;

public class DonationImporter extends AbstractView
{
    private static final SimpleDateFormat yyyyMMddFormat = new SimpleDateFormat("yyyyMMdd");
    private static final Settings settings = new Settings(DonationImporter.class);
    private static final String[] payPalCurrencies = new String[]
    { "EUR", "USD" };

    private DateInput startDateInput;
    private DateInput endDateInput;
    private TabFolder folder;
    private final TransactionTabControl[] tabs = new TransactionTabControl[payPalCurrencies.length];

    private class DownloadDialog extends AbstractDialog<Object>
    {
        private ProgressBar progressBar;
        private Date startDate;
        private Date endDate;

        public DownloadDialog(int position, Date startDate, Date endDate)
        {
            super(position, false);
            this.setSize(600, SWT.DEFAULT);
            setTitle(JameicaPlugin.i18n().tr("Downloading PayPal Transactions..."));

            this.startDate = startDate;
            this.endDate = endDate;
        }

        @Override
        protected void paint(Composite parent) throws Exception
        {
            // Draw the Progress dialog.
            progressBar = new ProgressBar(parent, SWT.SMOOTH);
            progressBar.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

            final Button cancel = new Cancel();
            cancel.paint(parent);

            // Do the time-consuming downloading in a separate thread.
            Thread workThread = new Thread()
            {
                private PayPalIO payPalIO;
                private JVereinIO jVereinIO;
                private Map<String, Double> balancesMap;

                /**
                 * Compare downloaded PayPal transactions with already existing
                 * JVerein transactions and mark duplicates in the PayPal
                 * transactions list.
                 */
                private void markDuplicateTransactions(ArrayList<Transaction> payPalTransactions,
                        ArrayList<Transaction> jVereinTransactions)
                {
                    for (Transaction p : payPalTransactions)
                    {
                        for (Transaction j : jVereinTransactions)
                        {
                            // Only compare year, month and day components of
                            // the date.
                            String pDate = yyyyMMddFormat.format(p.getDate());
                            String jDate = yyyyMMddFormat.format(j.getDate());

                            if (pDate.equals(jDate) && p.getName().equals(j.getName())
                                    && p.getGrossAmount().equals(j.getGrossAmount())
                                    && p.getNetAmount().equals(j.getNetAmount()))
                            {
                                // Mark as duplicate by moving it to the other
                                // table.
                                p.setDonation(false);
                            }
                        }
                    }
                }

                /**
                 * Download the transactions for a single currency.
                 * 
                 * @param i
                 *        Index of the currency as in payPalCurrencies.
                 */
                private void downloadCurrencyTransactions(int i) throws RemoteException
                {
                    final String currencyCode = payPalCurrencies[i];
                    final TransactionTabControl tab = tabs[i];

                    // Update the totals.
                    final Double accountTotal = balancesMap.get(currencyCode);
                    final Double jVereinTotal = jVereinIO.getAccountTotal(currencyCode);
                    GUI.getDisplay().asyncExec(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            tab.setAccountTotal(accountTotal);
                            tab.setJVereinTotal(jVereinTotal);
                        }
                    });

                    // Import the PayPal transactions.
                    final int percentageOffset = i * 100 / payPalCurrencies.length;
                    final ArrayList<Transaction> payPalTransactions = payPalIO.getTransactions(startDate, endDate, currencyCode, new Action()
                    {
                        @Override
                        public void handleAction(Object context) throws ApplicationException
                        {
                            final Integer percentage = (Integer) context;

                            GUI.getDisplay().asyncExec(new Runnable()
                            {
                                @Override
                                public void run()
                                {
                                    // If the user clicked Cancel, the progress
                                    // bar may have been disposed, but this
                                    // Runnable is still queued.
                                    // So check for disposal first.
                                    if (!progressBar.isDisposed())
                                    {
                                        // Calculate a total percentage out of
                                        // the per-currency percentage.
                                        progressBar.setSelection(percentageOffset
                                                + percentage / payPalCurrencies.length);
                                    }
                                }
                            });
                        }
                    });

                    // Import the related JVerein transactions.
                    final ArrayList<Transaction> jVereinTransactions = jVereinIO.getDonationTransactions(startDate, endDate, currencyCode);

                    // Check for PayPal transactions that have already been
                    // added to JVerein.
                    // Mark them as no donations, so they appear in the other
                    // table and are not imported again.
                    markDuplicateTransactions(payPalTransactions, jVereinTransactions);

                    // Finally show the PayPal transactions.
                    GUI.getDisplay().asyncExec(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            try
                            {
                                tab.setTransactions(payPalTransactions, currencyCode);
                            }
                            catch (RemoteException e)
                            {
                                Logger.error("Unable to set transactions", e);
                            }
                        }
                    });
                }

                @Override
                public void run()
                {
                    try
                    {
                        payPalIO = new PayPalIO();
                        jVereinIO = new JVereinIO();

                        // Get the PayPal balances.
                        balancesMap = payPalIO.getBalances();

                        for (int i = 0; i < payPalCurrencies.length; i++)
                        {
                            downloadCurrencyTransactions(i);
                        }

                        // Close the dialog when we're done.
                        // Give the GUI updater Runnables some time to update
                        // the progress bar before we close (just for eye-candy,
                        // not required).
                        sleep(500);
                        close();
                    }
                    catch (Exception e)
                    {
                        Logger.error("Error while downloading transactions", e);
                    }
                }
            };
            workThread.start();
        }

        @Override
        protected Object getData() throws Exception
        {
            return null;
        }
    }

    @Override
    public void bind() throws Exception
    {
        GUI.getView().setTitle(JameicaPlugin.i18n().tr("Donation Importer"));

        // Create a lean layout with less spacing for the control widgets to
        // make more room for the tables.
        final GridLayout singleColumnLayout = new GridLayout();
        singleColumnLayout.marginHeight = 0;
        singleColumnLayout.verticalSpacing = 0;

        final GridLayout threeColumnLayout = new GridLayout(3, false);
        threeColumnLayout.marginHeight = 0;
        threeColumnLayout.verticalSpacing = 0;

        // Add a group for the control widgets.
        // As we don't want Jameica to make the widgets right-aligned and
        // full-size, we put them into extra composites.
        final Group group = new Group(getParent(), SWT.NONE);
        group.setLayout(singleColumnLayout);
        group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        // Add the settings buttons.
        final Composite firstRowComposite = new Composite(group, SWT.NONE);
        firstRowComposite.setLayout(singleColumnLayout);

        final ButtonArea settingsButtons = new ButtonArea();
        settingsButtons.addButton(JameicaPlugin.i18n().tr("Exchange Rates"), new ExchangeRates(), null, false, "invest.png");
        settingsButtons.addButton(JameicaPlugin.i18n().tr("PayPal API Credentials"), new PayPalAPICredentials(), null, false, "seahorse-preferences.png");
        settingsButtons.paint(firstRowComposite);

        // Add the control widgets.
        final Composite secondRowComposite = new Composite(group, SWT.NONE);
        secondRowComposite.setLayout(threeColumnLayout);
        secondRowComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        final GridData dateGridData = new GridData();
        dateGridData.widthHint = 200;

        final SimpleContainer startDateContainer = new SimpleContainer(secondRowComposite);
        startDateContainer.getComposite().setLayoutData(dateGridData);
        startDateInput = new DateInput(
                yyyyMMddFormat.parse(settings.getString("startdate", "20121201")),
                JameicaPlugin.dateFormat);
        startDateContainer.addLabelPair(JameicaPlugin.i18n().tr("Start Date"), startDateInput);

        final SimpleContainer endDateContainer = new SimpleContainer(secondRowComposite);
        endDateContainer.getComposite().setLayoutData(dateGridData);
        endDateInput = new DateInput(
                yyyyMMddFormat.parse(settings.getString("enddate", "20121231")),
                JameicaPlugin.dateFormat);
        endDateContainer.addLabelPair(JameicaPlugin.i18n().tr("End Date"), endDateInput);

        final Button downloadButton = new Button(
                JameicaPlugin.i18n().tr("Download PayPal Transactions"), new Action()
                {
                    @Override
                    public void handleAction(Object context) throws ApplicationException
                    {
                        // Save the entered dates.
                        final Date startDate = (Date) startDateInput.getValue();
                        final Date endDate = (Date) endDateInput.getValue();
                        settings.setAttribute("startdate", yyyyMMddFormat.format(startDate));
                        settings.setAttribute("enddate", yyyyMMddFormat.format(endDate));

                        // Open the Download dialog, which itself does the
                        // downloading.
                        try
                        {
                            new DownloadDialog(AbstractDialog.POSITION_CENTER, startDate,
                                    endDate).open();
                        }
                        catch (OperationCanceledException e)
                        {
                            // Do nothing.
                        }
                        catch (Exception e)
                        {
                            Logger.error("Error during download", e);
                        }
                    }
                }, null, true, "document-save.png");
        downloadButton.paint(secondRowComposite);

        // Add the tabs.
        folder = new TabFolder(getParent(), SWT.NONE);
        folder.setLayoutData(new GridData(GridData.FILL_BOTH));

        for (int i = 0; i < payPalCurrencies.length; i++)
        {
            // Create the tab for this currency.
            TabGroup tabGroup = new TabGroup(folder, "PayPal " + payPalCurrencies[i]);
            tabs[i] = new TransactionTabControl(this);
            tabs[i].add(tabGroup);
        }
    }
}
