/*
 * PROJECT:    ReactOS Deutschland e.V. Helper Plugin
 * LICENSE:    GNU GPL v2 or any later version as published by the Free Software Foundation
 * COPYRIGHT:  Copyright 2016 ReactOS Deutschland e.V. <deutschland@reactos.org>
 * AUTHORS:    Colin Finck <colin@reactos.org>
 */

package org.reactos.ev.jameicaplugin.gui.control;

import de.willuhn.jameica.gui.AbstractControl;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.parts.Button;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.gui.util.SimpleContainer;
import de.willuhn.jameica.gui.util.TabGroup;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import java.rmi.RemoteException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.reactos.ev.jameicaplugin.JameicaPlugin;
import org.reactos.ev.jameicaplugin.io.JVereinIO;
import org.reactos.ev.jameicaplugin.io.Transaction;

public class TransactionTabControl extends AbstractControl
{
    private Label accountTotalLabel;
    private Label donationTotalLabel;
    private Label jVereinTotalLabel;
    private Label newJVereinTotalLabel;
    private TablePart donationTable;
    private TablePart otherTable;

    private String currencyCode;
    private ArrayList<Transaction> transactions;
    private Double accountTotal = 0.0;
    private Double donationTotal = 0.0;
    private Double jVereinTotal = 0.0;

    private class AddToJVereinAction implements Action
    {
        @Override
        public void handleAction(Object context) throws ApplicationException
        {
            try
            {
                if (transactions == null || transactions.isEmpty())
                    return;

                JVereinIO jVereinIO = new JVereinIO();

                // For currencies other than EUR, we need the monthly exchange
                // rates to add them.
                // Make sure that the user entered these.
                if (!currencyCode.equals("EUR"))
                {
                    for (Transaction t : transactions)
                    {
                        if (jVereinIO.getExchangeRateToEUR(t.getDate(), currencyCode) == 0)
                        {
                            throw new ApplicationException(
                                    String.format(JameicaPlugin.i18n().tr("No exchange rate entered for %s"), new SimpleDateFormat(
                                            "yyyy-MM").format(t.getDate())));
                        }
                    }
                }

                // Add the donations to JVerein.
                jVereinIO.putDonationTransactions(transactions, currencyCode);

                // Update totals now that the donations have been added and
                // empty the tables.
                setJVereinTotal(jVereinTotal + donationTotal);
                transactions.clear();
                setTransactions(transactions, currencyCode);

                // Report success.
                GUI.getStatusBar().setSuccessText(String.format(JameicaPlugin.i18n().tr("%s donations imported into JVerein"), currencyCode));
            }
            catch (RemoteException e)
            {
                Logger.error("Error while adding JVerein transactions", e);
            }
        }
    }

    private class TransactionUpDownAction implements Action
    {
        @Override
        public void handleAction(Object context) throws ApplicationException
        {
            TablePart table = (TablePart) context;
            Object selection = table.getSelection();
            if (selection == null)
                return;

            // Check if one or multiple transactions are selected.
            Transaction[] selectedTransactions;
            if (selection instanceof Transaction)
            {
                selectedTransactions = new Transaction[1];
                selectedTransactions[0] = (Transaction) selection;
            }
            else
            {
                selectedTransactions = (Transaction[]) selection;
            }

            for (Transaction t : selectedTransactions)
            {
                try
                {
                    // Flip the donation bit and alter the tables accordingly.
                    t.setDonation(!t.isDonation());
                    table.removeItem(t);
                    addTransactionToTable(t, true);
                    updateDonationTotal();
                }
                catch (RemoteException e)
                {
                    Logger.error("Error while altering transaction tables", e);
                }
            }
        }
    }

    private Label addTotalLabel(Composite composite, String caption)
    {
        final GridData leftGridData = new GridData();
        leftGridData.widthHint = 190;

        final GridData rightGridData = new GridData(GridData.HORIZONTAL_ALIGN_END);
        rightGridData.widthHint = 70;

        final Label captionLabel = new Label(composite, SWT.NONE);
        captionLabel.setLayoutData(leftGridData);
        captionLabel.setText(caption);

        final Label label = new Label(composite, SWT.RIGHT);
        label.setLayoutData(rightGridData);

        return label;
    }

    private void addTransactionToTable(Transaction t, Boolean subtract) throws RemoteException
    {
        if (t.isDonation())
        {
            donationTable.addItem(t);
            donationTotal += t.getNetAmount();
        }
        else
        {
            otherTable.addItem(t);
            if (subtract)
                donationTotal -= t.getNetAmount();
        }
    }

    private void updateDonationTotal()
    {
        Double newJVereinTotal = jVereinTotal + donationTotal;

        donationTotalLabel.setText(JameicaPlugin.currencyFormat.format(donationTotal));
        newJVereinTotalLabel.setText(JameicaPlugin.currencyFormat.format(newJVereinTotal));
    }

    public TransactionTabControl(AbstractView view)
    {
        super(view);
    }

    public void add(TabGroup tabGroup) throws RemoteException
    {
        final SimpleContainer leftContainer = new SimpleContainer(tabGroup.getComposite(), true);

        // Create both tables.
        donationTable = new TransactionTableControl(view, true).getTable();
        otherTable = new TransactionTableControl(view, false).getTable();

        // Paint the donation table.
        leftContainer.addHeadline(JameicaPlugin.i18n().tr("Donations to import"));
        donationTable.paint(leftContainer.getComposite());

        // Create the buttons for moving transactions around.
        leftContainer.addSeparator();

        final Composite tableButtonsComposite = new Composite(leftContainer.getComposite(),
                SWT.NONE);
        tableButtonsComposite.setLayout(new GridLayout(2, true));
        tableButtonsComposite.setLayoutData(new GridData(SWT.CENTER, SWT.TOP, true, false));

        final Button down = new Button(null, new TransactionUpDownAction(), donationTable);
        down.setIcon("go-down.png");
        down.paint(tableButtonsComposite);

        final Button up = new Button(null, new TransactionUpDownAction(), otherTable);
        up.setIcon("go-up.png");
        up.paint(tableButtonsComposite);

        // Paint the table for other transactions.
        leftContainer.addHeadline(JameicaPlugin.i18n().tr("Duplicates and other Transactions"));
        otherTable.paint(leftContainer.getComposite());

        // Create the right container.
        // Fill vertical, but not horizontal, so that the right container does
        // not occupy half of the tab width.
        final SimpleContainer rightContainer = new SimpleContainer(tabGroup.getComposite());
        rightContainer.getComposite().setLayout(new GridLayout());
        rightContainer.getComposite().setLayoutData(new GridData(
                GridData.FILL_VERTICAL | GridData.VERTICAL_ALIGN_BEGINNING));

        // Create the labels for totals.
        rightContainer.addHeadline(JameicaPlugin.i18n().tr("Totals"));

        final Composite topLabelsComposite = new Composite(rightContainer.getComposite(), SWT.NONE);
        topLabelsComposite.setLayout(new GridLayout(2, false));

        accountTotalLabel = addTotalLabel(topLabelsComposite, JameicaPlugin.i18n().tr("PayPal Account:"));
        jVereinTotalLabel = addTotalLabel(topLabelsComposite, JameicaPlugin.i18n().tr("JVerein Account:"));
        donationTotalLabel = addTotalLabel(topLabelsComposite, JameicaPlugin.i18n().tr("+ Donations:"));

        rightContainer.addSeparator();

        final Composite bottomLabelsComposite = new Composite(rightContainer.getComposite(),
                SWT.NONE);
        bottomLabelsComposite.setLayout(new GridLayout(2, false));

        newJVereinTotalLabel = addTotalLabel(bottomLabelsComposite, JameicaPlugin.i18n().tr("JVerein Account with Donations:"));

        // Finally put the "Add" button in the bottom-right corner of the tab.
        Composite addButtonComposite = new Composite(rightContainer.getComposite(), SWT.NONE);
        addButtonComposite.setLayout(new GridLayout());
        addButtonComposite.setLayoutData(new GridData(GridData.FILL_VERTICAL
                | GridData.HORIZONTAL_ALIGN_END | GridData.VERTICAL_ALIGN_END));

        Button add = new Button(JameicaPlugin.i18n().tr("Import Donations into JVerein"),
                new AddToJVereinAction(), null, false, "list-add.png");
        add.paint(addButtonComposite);
    }

    public void setAccountTotal(Double total)
    {
        accountTotal = total;
        accountTotalLabel.setText(JameicaPlugin.currencyFormat.format(accountTotal));
    }

    public void setJVereinTotal(Double total)
    {
        jVereinTotal = total;
        jVereinTotalLabel.setText(JameicaPlugin.currencyFormat.format(jVereinTotal));
    }

    public void setTransactions(ArrayList<Transaction> transactions, String currencyCode)
            throws RemoteException
    {
        this.currencyCode = currencyCode;
        this.transactions = transactions;

        donationTable.removeAll();
        otherTable.removeAll();
        donationTotal = 0.0;

        for (Transaction t : this.transactions)
        {
            addTransactionToTable(t, false);
        }

        updateDonationTotal();
    }
}
