/* 
    JSPWiki - a JSP-based WikiWiki clone.

    Copyright (C) 2001-2005 Janne Jalkanen (Janne.Jalkanen@iki.fi)

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

import javax.servlet.*;
import javax.servlet.http.*;
import java.util.*;
import java.io.*;

import org.apache.log4j.Logger;

import com.ecyrd.jspwiki.util.HttpUtil;

/**
 */
public class WikiServlet
    extends HttpServlet
{
    private WikiEngine m_engine;
    Logger log = Logger.getLogger(this.getClass().getName());

    public void init( ServletConfig config )
        throws ServletException 
    {
        super.init( config );

        m_engine         = WikiEngine.getInstance( config );
        Properties props = m_engine.getWikiProperties();

        log.info("WikiServlet initialized.");
    }

    public void doGet( HttpServletRequest req, HttpServletResponse res ) 
        throws IOException, ServletException 
    {
        String pageName = DefaultURLConstructor.parsePageFromURL( req,
                                                                  m_engine.getContentEncoding() );

        log.info("Request for page: "+pageName);

        RequestDispatcher dispatcher = req.getRequestDispatcher("/Wiki.jsp?page="+m_engine.encodeName(pageName)+"&amp;"+req.getQueryString() );

        dispatcher.forward( req, res );
    }
}


