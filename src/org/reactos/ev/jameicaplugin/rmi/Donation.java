/*
 * PROJECT:    ReactOS Deutschland e.V. Helper Plugin
 * LICENSE:    GNU GPL v2 or any later version as published by the Free Software Foundation
 * COPYRIGHT:  Copyright 2010 ReactOS Deutschland e.V. <deutschland@reactos.org>
 * AUTHORS:    Colin Finck <colin@reactos.org>
 */

package org.reactos.ev.jameicaplugin.rmi;

import de.willuhn.datasource.rmi.DBObject;
import java.rmi.RemoteException;
import java.util.Date;

public interface Donation extends DBObject
{
    public Long getJVereinID() throws RemoteException;

    public Date getDate() throws RemoteException;

    public String getName() throws RemoteException;

    public Boolean isAnonymous() throws RemoteException;

    public Double getAmount() throws RemoteException;

    public String getCurrency() throws RemoteException;

    public String getComment() throws RemoteException;
}
