/*
 * PROJECT:    ReactOS Deutschland e.V. Helper Plugin
 * LICENSE:    GNU GPL v2 or any later version as published by the Free Software Foundation
 * COPYRIGHT:  Copyright 2016 ReactOS Deutschland e.V. <deutschland@reactos.org>
 * AUTHORS:    Colin Finck <colin@reactos.org>
 */

package org.reactos.ev.jameicaplugin.formatter;

import de.willuhn.jameica.gui.formatter.Formatter;

public class BooleanFormatter implements Formatter
{
    @Override
    public String format(Object o)
    {
        Boolean b = (Boolean) o;
        if (b)
            return "X";
        else
            return "";
    }
}
