/*
 * PROJECT:    ReactOS Deutschland e.V. Helper Plugin
 * LICENSE:    GNU GPL v2 or any later version as published by the Free Software Foundation
 * COPYRIGHT:  Copyright 2016 ReactOS Deutschland e.V. <deutschland@reactos.org>
 * AUTHORS:    Colin Finck <colin@reactos.org>
 */

package org.reactos.ev.jameicaplugin.rmi;

import de.willuhn.datasource.rmi.DBObject;
import java.rmi.RemoteException;

public interface ExchangeRate extends DBObject
{
    public Integer getYear() throws RemoteException;

    public Integer getMonth() throws RemoteException;

    public String getCurrencyCode() throws RemoteException;

    public Double getExchangeRateToEUR() throws RemoteException;

    public void setYear(Integer year) throws RemoteException;

    public void setMonth(Integer month) throws RemoteException;

    public void setCurrencyCode(String currencyCode) throws RemoteException;

    public void setExchangeRateToEUR(Double exchangeRateToEUR) throws RemoteException;
}
