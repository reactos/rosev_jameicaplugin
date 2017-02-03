/*
 * PROJECT:    ReactOS Deutschland e.V. Helper Plugin
 * LICENSE:    GNU GPL v2 or any later version as published by the Free Software Foundation
 * COPYRIGHT:  Copyright 2016-2017 ReactOS Deutschland e.V. <deutschland@reactos.org>
 * AUTHORS:    Colin Finck <colin@reactos.org>
 */

package org.reactos.ev.jameicaplugin.io;

import de.willuhn.datasource.GenericObject;
import java.rmi.RemoteException;
import java.util.Date;
import org.reactos.ev.jameicaplugin.formatter.NameFormatter;

public class Transaction implements GenericObject
{
    private String id;
    private Date date;
    private String name;
    private Double grossAmount;
    private Double netAmount;
    private Boolean anonymous;
    private String comment;
    private Boolean donation;
    private String type;

    public Transaction(String id, Date date, String name, Double grossAmount, Double netAmount,
            Boolean anonymous, String comment, Boolean donation, String type)
    {
        this.id = id;
        this.date = date;
        setName(name);
        this.grossAmount = grossAmount;
        this.netAmount = netAmount;
        this.anonymous = anonymous;
        this.comment = comment;
        this.donation = donation;
        this.type = type;
    }

    @Override
    public boolean equals(GenericObject arg0) throws RemoteException
    {
        if (arg0 == null || !(arg0 instanceof Transaction))
        {
            return false;
        }

        return this.getID().equals(arg0.getID());
    }

    @Override
    public Object getAttribute(String arg0) throws RemoteException
    {
        if (arg0.equals("id"))
        {
            return id;
        }
        else if (arg0.equals("date"))
        {
            return date;
        }
        else if (arg0.equals("name"))
        {
            return name;
        }
        else if (arg0.equals("grossamount"))
        {
            return grossAmount;
        }
        else if (arg0.equals("netamount"))
        {
            return netAmount;
        }
        else if (arg0.equals("anonymous"))
        {
            return anonymous;
        }
        else if (arg0.equals("comment"))
        {
            return comment;
        }
        else if (arg0.equals("donation"))
        {
            return donation;
        }
        else if (arg0.equals("type"))
        {
            return type;
        }

        throw new RemoteException(String.format("Invalid attribute: %s", arg0));
    }

    @Override
    public String[] getAttributeNames() throws RemoteException
    {
        return new String[]
        { "id", "date", "name", "grossamount", "netamount", "anonymous", "comment", "donation",
                "type" };
    }

    @Override
    public String getID() throws RemoteException
    {
        return id;
    }

    public Date getDate()
    {
        return date;
    }

    public String getName()
    {
        return name;
    }

    public Double getGrossAmount()
    {
        return grossAmount;
    }

    public Double getNetAmount()
    {
        return netAmount;
    }

    public Boolean isAnonymous()
    {
        return anonymous;
    }

    public String getComment()
    {
        return comment;
    }

    public Boolean isDonation()
    {
        return donation;
    }

    public String getType()
    {
        return type;
    }

    @Override
    public String getPrimaryAttribute() throws RemoteException
    {
        return "id";
    }

    public void setName(String name)
    {
        NameFormatter formatter = new NameFormatter();
        this.name = formatter.format(name);
    }

    public void setAnonymous(Boolean anonymous)
    {
        this.anonymous = anonymous;
    }

    public void setDonation(Boolean donation)
    {
        this.donation = donation;
    }
}
