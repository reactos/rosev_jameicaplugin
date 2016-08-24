/*
 * PROJECT:    ReactOS Deutschland e.V. Helper Plugin
 * LICENSE:    GNU GPL v2 or any later version as published by the Free Software Foundation
 * COPYRIGHT:  Copyright 2016 ReactOS Deutschland e.V. <deutschland@reactos.org>
 * AUTHORS:    Colin Finck <colin@reactos.org>
 */

package org.reactos.ev.jameicaplugin.io;

import de.willuhn.jameica.gui.Action;
import de.willuhn.util.ApplicationException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.rmi.RemoteException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang.time.DateUtils;
import org.reactos.ev.jameicaplugin.gui.dialog.PayPalAPICredentials;

public class PayPalIO
{
    private final SimpleDateFormat isoDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
    private final String payPalURL = "https://api-3t.paypal.com/nvp";
    private String loginTemplate;

    public PayPalIO()
    {
        // Prepare the Login string.
        loginTemplate = "USER=" + PayPalAPICredentials.settings.getString("username", null);
        loginTemplate += "&PWD=" + PayPalAPICredentials.settings.getString("password", null);
        loginTemplate += "&SIGNATURE=" + PayPalAPICredentials.settings.getString("signature", null);
        loginTemplate += "&VERSION=94";
    }

    private Map<String, String> doAPICall(String request) throws RemoteException
    {
        Map<String, String> responseMap = new HashMap<String, String>();

        try
        {
            // Open a connection to the PayPal API.
            URL url = new URL(payPalURL);
            URLConnection conn = url.openConnection();
            conn.setDoOutput(true);

            // Post our request.
            OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream());
            writer.write(request);
            writer.flush();
            writer.close();

            // Read the string response.
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(conn.getInputStream()));
            String line;
            StringBuilder response = new StringBuilder();

            while ((line = reader.readLine()) != null)
            {
                response.append(line);
            }

            reader.close();

            // Build the response map.
            String[] parameters = response.toString().split("&");

            for (String p : parameters)
            {
                String[] nameValue = p.split("=");
                responseMap.put(nameValue[0], URLDecoder.decode(nameValue[1], "utf-8"));
            }
        }
        catch (Exception e)
        {
            throw new RemoteException("Unable to perform PayPal API call", e);
        }

        return responseMap;
    }

    /**
     * Attempts to figure out if the donor doesn't want his name to be
     * published.
     * 
     * @param comment
     *        The comment supplied with the PayPal transaction.
     * 
     * @return true if the function detected anonymity, false otherwise.
     */
    private Boolean isDonationAnonymous(String comment)
    {
        if (comment == null)
            return false;

        final String lowerCaseComment = comment.toLowerCase();

        if (lowerCaseComment.contains("anonym"))
            return true;

        if (lowerCaseComment.contains("name"))
            return true;

        return false;
    }

    private void addPayPalTransaction(ArrayList<Transaction> transactions,
            Map<String, String> responseMap, int i, String currencyCode) throws RemoteException
    {
        // Ignore any incomplete transactions.
        if (!responseMap.get("L_STATUS" + i).equals("Completed"))
            return;

        // Even though we already filter by currency in the TransactionSearch
        // call, some transactions with a different currency pass through.
        // Filter them here again.
        if (!responseMap.get("L_CURRENCYCODE" + i).equals(currencyCode))
            return;

        // Parse the ISO-8601 formatted date.
        Date date;
        try
        {
            date = isoDateFormat.parse(responseMap.get("L_TIMESTAMP" + i));
        }
        catch (ParseException e)
        {
            throw new RemoteException("Unable to parse the transaction date", e);
        }

        // Get our field information or make some guesses.
        String type = responseMap.get("L_TYPE" + i);
        Boolean isDonation = (type.equals("Donation") || type.equals("Recurring Payment"));

        Boolean isAnonymous = false;
        String id = responseMap.get("L_TRANSACTIONID" + i);
        String name = responseMap.get("L_NAME" + i);
        Double grossAmount = Double.parseDouble(responseMap.get("L_AMT" + i));
        Double netAmount = Double.parseDouble(responseMap.get("L_NETAMT" + i));
        String comment = null;

        // Put "donations" in the other table, which are entirely eaten up by
        // fees.
        if (netAmount == 0.0)
            isDonation = false;

        // For donations, perform a GetTransactionDetails request to get the
        // comment.
        if (isDonation)
        {
            String request = loginTemplate;
            request += "&METHOD=GetTransactionDetails";
            request += "&TRANSACTIONID=" + id;
            Map<String, String> detailsResponseMap = doAPICall(request);

            // Check for success.
            if (detailsResponseMap.get("ACK").equals("Success"))
            {
                // Get the comment and check if it indicates an anonymous
                // donation.
                comment = detailsResponseMap.get("NOTE");
                isAnonymous = isDonationAnonymous(comment);
            }
            else
            {
                throw new RemoteException(
                        "GetTransactionDetails failed with: " + detailsResponseMap.get("ACK"));
            }
        }

        Transaction t = new Transaction(id, date, name, grossAmount, netAmount, isAnonymous,
                comment, isDonation, type);
        transactions.add(t);
    }

    public Map<String, Double> getBalances() throws RemoteException
    {
        // Build a per-currency map for the balances.
        Map<String, Double> balancesMap = new HashMap<String, Double>();

        // Do the GetBalance API call.
        String request = loginTemplate;
        request += "&METHOD=GetBalance";
        request += "&RETURNALLCURRENCIES=1";
        Map<String, String> responseMap = doAPICall(request);

        // Check for success.
        if (responseMap.get("ACK").equals("Success"))
        {
            // Loop through all balances, break when no entry can be found in
            // the map.
            for (int i = 0; responseMap.get("L_CURRENCYCODE" + i) != null; i++)
            {
                String currencyCode = responseMap.get("L_CURRENCYCODE" + i);
                Double balance = Double.parseDouble(responseMap.get("L_AMT" + i));
                balancesMap.put(currencyCode, balance);
            }
        }
        else
        {
            throw new RemoteException("GetBalance failed with: " + responseMap.get("ACK"));
        }

        return balancesMap;
    }

    public ArrayList<Transaction> getTransactions(Date startDate, Date endDate, String currencyCode,
            Action progressAction) throws RemoteException
    {
        // Prepare the PayPal NVP API request.
        String requestTemplate = loginTemplate;
        requestTemplate += "&METHOD=TransactionSearch";
        requestTemplate += "&CURRENCYCODE=" + currencyCode;

        // Build an ArrayList of transactions.
        ArrayList<Transaction> transactions = new ArrayList<Transaction>();

        // For reporting progress.
        int daysProcessed = 0;
        int daysToProcess = (int) ((endDate.getTime() - startDate.getTime()) / 86400000) + 1;

        // PayPal's API can only return a maximum of 100 transactions in a row.
        // When there are more, it fails with ACK code "SuccessWithWarning".
        // There is no documentation what to do to properly get all results and
        // no duplicates.
        // There is a StackOverflow discussion about this
        // (http://stackoverflow.com/questions/16312839), but it's not even
        // defined whether older or newer results are returned first.
        //
        // Therefore, I do a transaction search for every single day here.
        // This also allows me to report progress.
        while (!startDate.after(endDate))
        {
            Date iterationEndDate = DateUtils.addDays(startDate, 1);

            // Do the TransactionSearch API call.
            String request = requestTemplate;
            request += "&STARTDATE=" + isoDateFormat.format(startDate);
            request += "&ENDDATE=" + isoDateFormat.format(iterationEndDate);
            Map<String, String> responseMap = doAPICall(request);

            // Check for success.
            if (responseMap.get("ACK").equals("Success"))
            {
                // Loop through all transactions, break when no entry can be
                // found in the map.
                for (int i = 0; responseMap.get("L_TIMESTAMP" + i) != null; i++)
                {
                    addPayPalTransaction(transactions, responseMap, i, currencyCode);
                }
            }
            else
            {
                throw new RemoteException(
                        "TransactionSearch failed with: " + responseMap.get("ACK"));
            }

            // Calculate and report progress.
            daysProcessed++;
            Integer percentage = 100 * daysProcessed / daysToProcess;

            try
            {
                progressAction.handleAction(percentage);
            }
            catch (ApplicationException e)
            {
                throw new RemoteException("Unable to report progress", e);
            }

            // Move on to the next day.
            startDate = iterationEndDate;
        }

        return transactions;
    }
}
