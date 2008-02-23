/* 
    JSPWiki - a JSP-based WikiWiki clone.

    Copyright (C) 2001-2002 Janne Jalkanen (Janne.Jalkanen@iki.fi)

    This program is free software; you can redistribute it and/or modify
    it under the terms of the GNU Lesser General Public License as published by
    the Free Software Foundation; either version 2.1 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Lesser General Public License for more details.

    You should have received a copy of the GNU Lesser General Public License
    along with this program; if not, write to the Free Software
    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package com.ecyrd.jspwiki;

import java.util.ArrayList;
import java.util.Collection;

/**
 *  Just a simple class collecting all of the links
 *  that come in.
 *
 *  @author Janne Jalkanen
 */
public class LinkCollector
    implements StringTransmutator
{
    private ArrayList m_items = new ArrayList();

    /**
     * Returns a List of Strings representing links.
     * @return the link collection
     */
    public Collection getLinks()
    {
        return m_items;
    }

    /**
     * {@inheritDoc}
     */
    public String mutate( WikiContext context, String in )
    {
        m_items.add( in );

        return in;
    }
}
