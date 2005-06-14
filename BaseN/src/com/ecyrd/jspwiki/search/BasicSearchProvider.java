/*
JSPWiki - a JSP-based WikiWiki clone.

Copyright (C) 2005 Janne Jalkanen (Janne.Jalkanen@iki.fi)

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
package com.ecyrd.jspwiki.search;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Properties;
import java.util.TreeSet;

import org.apache.log4j.Logger;

import com.ecyrd.jspwiki.NoRequiredPropertyException;
import com.ecyrd.jspwiki.QueryItem;
import com.ecyrd.jspwiki.SearchMatcher;
import com.ecyrd.jspwiki.SearchResult;
import com.ecyrd.jspwiki.SearchResultComparator;
import com.ecyrd.jspwiki.WikiEngine;
import com.ecyrd.jspwiki.WikiPage;
import com.ecyrd.jspwiki.providers.ProviderException;
import com.ecyrd.jspwiki.providers.RepositoryModifiedException;
import com.ecyrd.jspwiki.providers.WikiPageProvider;

/**
 *  Interface for the search providers that handle searching the Wiki
 *
 *  @author Arent-Jan Banck for Informatica
 *  @since 2.2.21.
 */
public class BasicSearchProvider implements SearchProvider 
{
    private static final Logger log = Logger.getLogger(BasicSearchProvider.class);

    private WikiEngine m_engine;

    public void initialize(WikiEngine engine, Properties props)
            throws NoRequiredPropertyException, IOException 
    {
        m_engine = engine;
    }

    public void deletePage(WikiPage page) {};
    
    public void addToQueue(WikiPage page, String text) {};
    
    public Collection search( QueryItem[] queryTerms ) throws ProviderException
    {
        return m_engine.getPageManager().getAllPages();
    }

    public Collection findPages( QueryItem[] query )
    {
        TreeSet res = new TreeSet( new SearchResultComparator() );
        SearchMatcher matcher = new SearchMatcher( query );

        Collection allPages = null;
        try
        {
            // basic search simply returns allPages();
            allPages = search(query);
        }
        catch( ProviderException pe )
        {
            log.error( "Unable to retrieve page list", pe );
            return( null );
        }

        Iterator it = allPages.iterator();
        while( it.hasNext() )
        {
            try
            {
                WikiPage page = (WikiPage) it.next();
                if (page != null)
                {
                    String pageName = page.getName();
                    String pageContent = m_engine.getPageManager().getPageText(pageName, WikiPageProvider.LATEST_VERSION);
                    SearchResult comparison = matcher.matchPageContent( pageName, pageContent );

                    if( comparison != null )
                    {
                        res.add( comparison );
                    }
                }
            }
            catch( RepositoryModifiedException rme )
            {
                // FIXME: What to do in this case???
            }
            catch( ProviderException pe )
            {
                log.error( "Unable to retrieve page from cache", pe );
            }
            catch( IOException ioe )
            {
                log.error( "Failed to search page", ioe );
            }
        }

        return( res );
    }

    public Collection findPages(String query) 
    {
        return findPages(m_engine.getSearchManager().parseQuery(query));
    }

    /**
     * @see com.ecyrd.jspwiki.WikiProvider#getProviderInfo()
     */
    public String getProviderInfo()
    {
        return "BasicSearchProvider";
    }

}
