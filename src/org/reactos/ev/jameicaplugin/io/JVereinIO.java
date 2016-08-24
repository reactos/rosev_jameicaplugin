/*
 * PROJECT:    ReactOS Deutschland e.V. Helper Plugin
 * LICENSE:    GNU GPL v2 or any later version as published by the Free Software Foundation
 * COPYRIGHT:  Copyright 2016 ReactOS Deutschland e.V. <deutschland@reactos.org>
 * AUTHORS:    Colin Finck <colin@reactos.org>
 */

package org.reactos.ev.jameicaplugin.io;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.rmi.Buchung;
import de.jost_net.JVerein.rmi.Konto;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.datasource.rmi.DBService;
import de.willuhn.datasource.rmi.ResultSetExtractor;
import de.willuhn.util.ApplicationException;
import java.rmi.RemoteException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang.StringUtils;
import org.reactos.ev.jameicaplugin.JameicaPlugin;

public class JVereinIO
{
    /** Map a PayPal currency code to a JVerein account ID. */
    private final Map<String, String> currencyCodeToAccountID = new HashMap<String, String>();

    /**
     * All accounting types specifying donations (for looking up donations).
     * Comma-separated in parentheses for a SQL "IN" statement.
     */
    private String donationAccountingTypes;

    /** Accounting type to use when adding a new donation. */
    private Long newDonationAccountingType;

    public JVereinIO()
    {
        // ReactOS Deutschland e.V. specific configuration!!
        currencyCodeToAccountID.put("EUR", "2");
        currencyCodeToAccountID.put("USD", "8");
        donationAccountingTypes = "(1,2)";
        newDonationAccountingType = 1L;
    }

    /**
     * Extract the first column of the first row of a DB result as a double
     * value.
     */
    private class DoubleExtractor implements ResultSetExtractor
    {
        @Override
        public Object extract(ResultSet rs) throws RemoteException, SQLException
        {
            if (!rs.next())
                return new Double(0);

            return new Double(rs.getDouble(1));
        }
    }

    public Double getAccountTotal(String currencyCode) throws RemoteException
    {
        final String accountID = currencyCodeToAccountID.get(currencyCode);
        String sql = "SELECT ";

        // JVerein's entire accounting is in EUR currency.
        // Therefore, we can only get the net amount directly for the EUR
        // account.
        // For non-EUR currencies, the net amount is entered at the
        // beginning of the comment.
        if (currencyCode.equals("EUR"))
            sql += "SUM(betrag)";
        else
            sql += "SUM(CAST(kommentar AS DECIMAL(10,2)))";

        sql += " FROM buchung WHERE konto = ?";

        // Query the JVerein accounting information.
        return (Double) Einstellungen.getDBService().execute(sql, new Object[]
        { accountID }, new DoubleExtractor());
    }

    public ArrayList<Transaction> getDonationTransactions(Date startDate, Date endDate,
            String currencyCode) throws RemoteException
    {
        final String accountID = currencyCodeToAccountID.get(currencyCode);

        // Query the JVerein accounting information.
        final DBService service = Einstellungen.getDBService();
        final DBIterator<Buchung> it = service.createList(Buchung.class);
        it.addFilter("datum >= ? ", startDate);
        it.addFilter("datum <= ? ", endDate);
        it.addFilter("konto = ? ", accountID);
        it.addFilter("buchungsart IN " + donationAccountingTypes);

        // Build an ArrayList of transactions.
        final ArrayList<Transaction> transactions = new ArrayList<Transaction>();

        while (it.hasNext())
        {
            Buchung b = (Buchung) it.next();

            String type = b.getArt();
            String comment = b.getKommentar();

            // The gross amount is always entered like "15 EUR" in the "Art"
            // field of the JVerein transaction.
            Double grossAmount = Double.parseDouble(type.substring(0, type.indexOf(" ")));

            // JVerein's entire accounting is in EUR currency.
            // Therefore, we can only get the net amount directly for the EUR
            // account.
            // For non-EUR currencies, the net amount is entered at the
            // beginning of the comment.
            Double netAmount;
            if (currencyCode.equals("EUR"))
                netAmount = b.getBetrag();
            else
                netAmount = Double.parseDouble(comment.substring(0, comment.indexOf(" ")));

            Boolean anonymous = comment.contains("Anonym");

            Transaction t = new Transaction(b.getID(), b.getDatum(), b.getName(), grossAmount,
                    netAmount, anonymous, comment, true, null);
            transactions.add(t);
        }

        return transactions;
    }

    public Double getExchangeRateToEUR(Date date, String currencyCode) throws RemoteException
    {
        final Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        final int year = cal.get(Calendar.YEAR);
        final int month = cal.get(Calendar.MONTH) + 1;

        final String sql = "SELECT exchange_rate_to_eur FROM exchange_rates "
                + "WHERE year = ? AND month = ? AND currency_code = ?";

        return (Double) JameicaPlugin.getDBService().execute(sql, new Object[]
        { year, month, currencyCode }, new DoubleExtractor());
    }

    public void putDonationTransactions(ArrayList<Transaction> transactions, String currencyCode)
            throws RemoteException
    {
        final String accountID = currencyCodeToAccountID.get(currencyCode);

        // Query the account in the JVerein database.
        final DBService service = Einstellungen.getDBService();
        final DBIterator<Konto> it = service.createList(Konto.class);
        it.addFilter("id = ?", accountID);

        if (!it.hasNext())
            throw new RemoteException("Unable to find the JVerein account");

        Konto k = (Konto) it.next();

        // Add all donations as JVerein transactions.
        for (Transaction t : transactions)
        {
            if (!t.isDonation())
                continue;

            Buchung b = (Buchung) service.createObject(Buchung.class, null);
            b.setKonto(k);
            b.setName(t.getName());

            // JVerein's entire accounting is in EUR currency.
            // Therefore, we can only add EUR values directly as net amount.
            // For all other currencies, we need to perform a currency
            // conversion.
            Double netAmount = t.getNetAmount();
            Date date = t.getDate();
            if (!currencyCode.equals("EUR"))
                netAmount /= getExchangeRateToEUR(date, currencyCode);

            b.setBetrag(netAmount);
            b.setDatum(date);

            // The gross amount and original currency is put in the "Art" field.
            // This information is later used in the public donation list.
            b.setArt(JameicaPlugin.currencyFormatUS.format(t.getGrossAmount()) + " "
                    + currencyCode);

            ArrayList<String> comments = new ArrayList<String>();

            // When our transaction has a currency other than EUR, the net
            // amount above will be the amount currency-converted to EUR.
            // In this case, add the net amount in the original currency to the
            // comments.
            // This must always be the first entry in the comments for
            // getAccountTotal to work!
            if (!currencyCode.equals("EUR"))
            {
                comments.add(JameicaPlugin.currencyFormatUS.format(t.getNetAmount()) + " "
                        + currencyCode + " nach Gebühren");
            }

            // Anonymous donations are also indicated in the comment.
            if (t.isAnonymous())
                comments.add("Anonym");

            b.setKommentar(StringUtils.join(comments, "; "));
            b.setBuchungsart(newDonationAccountingType);

            try
            {
                b.store();
            }
            catch (ApplicationException e)
            {
                throw new RemoteException("Unable to store JVerein transaction", e);
            }
        }
    }
}
