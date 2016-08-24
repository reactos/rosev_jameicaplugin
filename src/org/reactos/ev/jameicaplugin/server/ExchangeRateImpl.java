/*
 * PROJECT:    ReactOS Deutschland e.V. Helper Plugin
 * LICENSE:    GNU GPL v2 or any later version as published by the Free Software Foundation
 * COPYRIGHT:  Copyright 2016 ReactOS Deutschland e.V. <deutschland@reactos.org>
 * AUTHORS:    Colin Finck <colin@reactos.org>
 */

package org.reactos.ev.jameicaplugin.server;

import de.willuhn.datasource.db.AbstractDBObject;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import java.rmi.RemoteException;
import org.reactos.ev.jameicaplugin.JameicaPlugin;
import org.reactos.ev.jameicaplugin.rmi.ExchangeRate;

public class ExchangeRateImpl extends AbstractDBObject implements ExchangeRate
{
    private static final long serialVersionUID = 5198539453995722877L;

    public ExchangeRateImpl() throws RemoteException
    {
        super();
    }

    @Override
    protected String getTableName()
    {
        return "exchange_rates";
    }

    @Override
    public String getPrimaryAttribute() throws RemoteException
    {
        return "id";
    }

    @Override
    public Integer getYear() throws RemoteException
    {
        return (Integer) getAttribute("year");
    }

    @Override
    public Integer getMonth() throws RemoteException
    {
        return (Integer) getAttribute("month");
    }

    @Override
    public String getCurrencyCode() throws RemoteException
    {
        return (String) getAttribute("currency_code");
    }

    @Override
    public Double getExchangeRateToEUR() throws RemoteException
    {
        return (Double) getAttribute("exchange_rate_to_eur");
    }

    @Override
    public void setYear(Integer year) throws RemoteException
    {
        setAttribute("year", year);
    }

    @Override
    public void setMonth(Integer month) throws RemoteException
    {
        setAttribute("month", month);
    }

    @Override
    public void setCurrencyCode(String currencyCode) throws RemoteException
    {
        setAttribute("currency_code", currencyCode);
    }

    @Override
    public void setExchangeRateToEUR(Double exchangeRateToEUR) throws RemoteException
    {
        setAttribute("exchange_rate_to_eur", exchangeRateToEUR);
    }

    @Override
    protected void insertCheck() throws ApplicationException
    {
        try
        {
            // Check the values, support zero values for month and exchange rate
            // though for the Add command.
            if (getYear() < 2000 || getYear() > 2099)
            {
                throw new ApplicationException(
                        JameicaPlugin.i18n().tr("Please enter a valid year"));
            }
            else if (getMonth() < 0 || getMonth() > 12)
            {
                throw new ApplicationException(
                        JameicaPlugin.i18n().tr("Please enter a valid month"));
            }
            else if (getCurrencyCode() == null || getCurrencyCode().length() != 3)
            {
                throw new ApplicationException(
                        JameicaPlugin.i18n().tr("Please enter a 3-character currency code"));
            }
            else if (getExchangeRateToEUR() < 0.0)
            {
                throw new ApplicationException(
                        JameicaPlugin.i18n().tr("Please enter a valid exchange rate"));
            }
        }
        catch (RemoteException e)
        {
            Logger.error("Error while checking the values", e);
        }
    }
}
