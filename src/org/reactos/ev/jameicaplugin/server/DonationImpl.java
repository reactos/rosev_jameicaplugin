/*
 * PROJECT:    ReactOS Deutschland e.V. Helper Plugin
 * LICENSE:    GNU GPL v2 or any later version as published by the Free Software Foundation
 * COPYRIGHT:  Copyright 2010 ReactOS Deutschland e.V. <deutschland@reactos.org>
 * AUTHORS:    Colin Finck <colin@reactos.org>
 */

package org.reactos.ev.jameicaplugin.server;

import de.jost_net.JVerein.server.Util;
import de.willuhn.datasource.db.AbstractDBObject;
import java.rmi.RemoteException;
import java.util.Date;
import org.reactos.ev.jameicaplugin.rmi.Donation;

public class DonationImpl extends AbstractDBObject implements Donation
{
    private static final long serialVersionUID = -2948636993177716350L;

    public DonationImpl() throws RemoteException
    {
        super();
    }

    protected String getTableName()
    {
        return "all_donations";
    }

    public String getPrimaryAttribute() throws RemoteException
    {
        return "id";
    }

    public Long getJVereinID() throws RemoteException
    {
        return (Long) getAttribute("jverein_id");
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
}
