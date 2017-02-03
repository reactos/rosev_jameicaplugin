/*
 * PROJECT:    ReactOS Deutschland e.V. Helper Plugin
 * LICENSE:    GNU GPL v2 or any later version as published by the Free Software Foundation
 * COPYRIGHT:  Copyright 2017 ReactOS Deutschland e.V. <deutschland@reactos.org>
 * AUTHORS:    Colin Finck <colin@reactos.org>
 */

package org.reactos.ev.jameicaplugin.formatter;

import static gcardone.junidecode.Junidecode.unidecode;

import de.willuhn.jameica.gui.formatter.Formatter;
import java.nio.charset.Charset;
import org.apache.commons.lang.WordUtils;

public class NameFormatter implements Formatter
{
    /**
     * Converts the name into a readable ISO-8859-1 version.
     */
    @Override
    public String format(Object o)
    {
        String name = (String) o;

        // Perform a transliteration if the name cannot be represented in
        // ISO-8859-1.
        if (!Charset.forName("ISO_8859_1").newEncoder().canEncode(name))
            name = unidecode(name);

        // Capitalize each word of the name.
        name = WordUtils.capitalizeFully(name, new char[]
        { ' ', '-', '.', ',', '\'' });

        return name;
    }
}
