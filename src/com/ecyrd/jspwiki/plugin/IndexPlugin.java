/*
    JSPWiki - a JSP-based WikiWiki clone.

    Copyright (C) 2002 Janne Jalkanen (Janne.Jalkanen@iki.fi)

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
package com.ecyrd.jspwiki.plugin;

import java.io.StringWriter;
import java.util.*;

import org.apache.log4j.Logger;
import org.apache.oro.text.GlobCompiler;
import org.apache.oro.text.regex.*;

import com.ecyrd.jspwiki.*;
import com.ecyrd.jspwiki.providers.ProviderException;

/**
 *  Builds an index of all pages.
 *  <P>Parameters</P>
 *  <UL>
 *    <LI>itemsPerLine: How many items should be allowed per line before break.
 *    If set to zero (the default), will not write breaks.
 *    <LI>include: Include only these pages.
 *    <LI>exclude: Exclude with this pattern.
 *  </UL>
 *
 *  @author Alain Ravet
 *  @author Janne Jalkanen
 *  @since 1.9.9
 */
public class IndexPlugin implements WikiPlugin
{
    protected static Logger log = Logger.getLogger(IndexPlugin.class);

    public  static final String INITIALS_COLOR                  = "red" ;
    private static final int    DEFAULT_ITEMS_PER_LINE          = 0     ;

    private static final String PARAM_ITEMS_PER_LINE            = "itemsPerLine";
    private static final String PARAM_INCLUDE                   = "include";
    private static final String PARAM_EXCLUDE                   = "exclude";

    private int                 m_currentNofPagesOnLine         = 0;
    private int                 m_itemsPerLine;
    protected String            m_previousPageFirstLetter       = "";
    protected StringWriter      m_bodyPart      =   new StringWriter();
    protected StringWriter      m_headerPart    =   new StringWriter();
    private Pattern             m_includePattern;
    private Pattern             m_excludePattern;


    public String execute( WikiContext wikiContext , Map params )
        throws PluginException
    {
        //
        //  Parse arguments and create patterns.
        //
        PatternCompiler compiler = new GlobCompiler();
        m_itemsPerLine = TextUtil.parseIntParameter( (String) params.get(PARAM_ITEMS_PER_LINE),
                                                     DEFAULT_ITEMS_PER_LINE );
        try
        {
            String ptrn = (String) params.get(PARAM_INCLUDE);
            if( ptrn == null ) ptrn = "*";
            m_includePattern = compiler.compile(ptrn);

            ptrn = (String) params.get(PARAM_EXCLUDE);
            if( ptrn == null ) ptrn = "";
            m_excludePattern = compiler.compile(ptrn);
        }
        catch( MalformedPatternException e )
        {
            throw new PluginException("Illegal pattern detected."); // FIXME, make a proper error.
        }

        //
        //  Get pages, then sort.
        //

        final Collection        allPages      = getAllPagesSortedByName( wikiContext );

        //
        //  Build the page.
        //
        buildIndexPageHeaderAndBody( wikiContext, allPages );

        StringBuffer res = new StringBuffer();

        res.append( "<div class=\"index\">\n" );
        res.append( "<div class=\"header\">\n" );
        res.append( m_headerPart.toString() );
        res.append( "</div>\n" );
        res.append( "<div class=\"body\">\n" );
        res.append( m_bodyPart.toString() );
        res.append( "</div>\n</div>\n" );

        return res.toString();
    }


    private void buildIndexPageHeaderAndBody( WikiContext context,
                                              final Collection allPages )
    {
        PatternMatcher matcher = new Perl5Matcher();

        for( Iterator i = allPages.iterator (); i.hasNext ();)
        {
            WikiPage curPage = (WikiPage) i.next();

            if( matcher.matches( curPage.getName(), m_includePattern ) )
            {
                if( !matcher.matches( curPage.getName(), m_excludePattern ) )
                {
                    ++m_currentNofPagesOnLine;

                    String    pageNameFirstLetter           = curPage.getName().substring(0,1).toUpperCase();
                    boolean   sameFirstLetterAsPreviousPage = m_previousPageFirstLetter.equals(pageNameFirstLetter);

                    if( !sameFirstLetterAsPreviousPage )
                    {
                        addLetterToIndexHeader( pageNameFirstLetter );
                        addLetterHeaderWithLine( pageNameFirstLetter );

                        m_currentNofPagesOnLine   = 1;
                        m_previousPageFirstLetter = pageNameFirstLetter;
                    }

                    addPageToIndex( context, curPage );
                    breakLineIfTooLong();
                }
            }
        } // for
    }


    /**
     *  Gets all pages, then sorts them.
     */
    static Collection getAllPagesSortedByName( WikiContext wikiContext )
    {
        final WikiEngine engine = wikiContext.getEngine();

        final PageManager pageManager = engine.getPageManager();
        if( pageManager == null )
            return null;

        Collection result = new TreeSet( new Comparator() {
            public int compare( Object o1, Object o2 )
            {
                if( o1 == null || o2 == null ) return 0;

                WikiPage page1 = (WikiPage)o1;
                WikiPage page2 = (WikiPage)o2;

                return page1.getName().compareTo( page2.getName() );
            }
        });

        try
        {
            Collection allPages = pageManager.getAllPages();
            result.addAll( allPages );
        }
        catch( ProviderException e )
        {
            log.fatal("PageProvider is unable to list pages: ", e);
        }

        return result;
    }


    private void addLetterToIndexHeader( final String firstLetter )
    {
        final boolean noLetterYetInTheIndex = ! "".equals(m_previousPageFirstLetter);

        if( noLetterYetInTheIndex )
        {
            m_headerPart.write(" - " );
        }

        m_headerPart.write("<a href=\"#"  + firstLetter + "\">" + firstLetter + "</a>" );
    }


    private void addLetterHeaderWithLine( final String firstLetter )
    {
        m_bodyPart.write("\n<br /><br />" +
                         "<span class=\"section\">"+
                         "<a name=\"" + firstLetter + "\">"+
                         firstLetter+"</a></span>" +
                         "<hr />\n" );
    }

    protected void addPageToIndex( WikiContext context, WikiPage curPage )
    {
        final boolean notFirstPageOnLine = 2 <= m_currentNofPagesOnLine;

        if( notFirstPageOnLine )
        {
            m_bodyPart.write(",&nbsp; ");
        }

        m_bodyPart.write("<a href=\""+
                         context.getURL(WikiContext.VIEW, curPage.getName())+"\">"+
                         context.getEngine().beautifyTitleNoBreak(curPage.getName())+
                         "</a>");
    }

    protected void breakLineIfTooLong()
    {
        final boolean limitReached = m_itemsPerLine == m_currentNofPagesOnLine;

        if( limitReached )
        {
            m_bodyPart.write( "<br />\n" );
            m_currentNofPagesOnLine = 0;
        }
    }

}
