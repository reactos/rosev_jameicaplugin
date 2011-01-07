/*
 * PROJECT:    ReactOS Deutschland e.V. Helper Plugin
 * LICENSE:    GNU GPL v2 or any later version as published by the Free Software Foundation
 * COPYRIGHT:  Copyright 2010 ReactOS Deutschland e.V. <deutschland@reactos.org>
 * AUTHORS:    Colin Finck <colin@reactos.org>
 */

package org.reactos.ev.jameicaplugin.server;

import de.jost_net.JVerein.server.Util;
import de.willuhn.datasource.db.AbstractDBObject;
import de.willuhn.util.ApplicationException;
import java.rmi.RemoteException;
import java.util.Date;
import org.reactos.ev.jameicaplugin.JameicaPlugin;
import org.reactos.ev.jameicaplugin.rmi.AdditionalDonation;

public class AdditionalDonationImpl extends AbstractDBObject implements AdditionalDonation
{
    private static final long serialVersionUID = -1373294026459728754L;

    public AdditionalDonationImpl() throws RemoteException
    {
        super();
    }

    protected String getTableName()
    {
        return "additional_donations";
    }

    public String getPrimaryAttribute() throws RemoteException
    {
        return "id";
    }

    public Date getDate() throws RemoteException
    {
        return (Date) getAttribute("date");
    }

    public String getName() throws RemoteException
    {
        return (String) getAttribute("name");
    }

    public Boolean isAnonymous() throws RemoteException
    {
        return Util.getBoolean(getAttribute("anonymous"));
    }

    public Double getAmount() throws RemoteException
    {
        return (Double) getAttribute("amount");
    }

    public String getCurrency() throws RemoteException
    {
        return (String) getAttribute("currency");
    }

    public String getComment() throws RemoteException
    {
        return (String) getAttribute("comment");
    }

    public void setDate(Date date) throws RemoteException
    {
        setAttribute("date", date);
    }

    public void setName(String name) throws RemoteException
    {
        setAttribute("name", name);
    }

    public void setAnonymous(Boolean anonymous) throws RemoteException
    {
        setAttribute("anonymous", anonymous);
    }

    public void setAmount(Double amount) throws RemoteException
    {
        setAttribute("amount", amount);
    }

    public void setCurrency(String currency) throws RemoteException
    {
        setAttribute("currency", currency);
    }

    public void setComment(String comment) throws RemoteException
    {
        setAttribute("comment", comment);
    }

    protected void insertCheck() throws ApplicationException
    {
        try
        {
            if (getDate() == null)
                throw new ApplicationException(JameicaPlugin.i18n().tr("Please enter a date"));
            else if (getName() == null)
                throw new ApplicationException(JameicaPlugin.i18n().tr("Please enter a name"));
            else if (getAmount() == null)
                throw new ApplicationException(JameicaPlugin.i18n().tr("Please enter an amount"));
            else if (getCurrency() == null || getCurrency().length() != 3)
                throw new ApplicationException(
                        JameicaPlugin.i18n().tr("Please enter a 3-character currency code"));
        }
        catch (RemoteException e)
        {
            throw new ApplicationException("Error while checking the values!");
        }
    }
}
