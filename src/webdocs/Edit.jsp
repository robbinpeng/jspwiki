<%@ page import="org.apache.log4j.*" %>
<%@ page import="com.ecyrd.jspwiki.*" %>
<%@ page import="java.util.*" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="com.ecyrd.jspwiki.tags.WikiTagBase" %>
<%@ page import="com.ecyrd.jspwiki.WikiProvider" %>
<%@ page errorPage="/Error.jsp" %>
<%@ taglib uri="/WEB-INF/jspwiki.tld" prefix="wiki" %>

<%! 
    public void jspInit()
    {
        wiki = WikiEngine.getInstance( getServletConfig() );
    }

    Category log = Category.getInstance("JSPWiki");
    WikiEngine wiki;
%>


<%
    String action  = request.getParameter("action");
    String ok      = request.getParameter("ok");
    String preview = request.getParameter("preview");
    String cancel  = request.getParameter("cancel");
    String comment = request.getParameter("comment");
    String author  = wiki.safeGetParameter( request, "author" );

    WikiContext wikiContext = wiki.createContext( request, 
                                                  comment != null ? WikiContext.COMMENT : WikiContext.EDIT );
    String pagereq = wikiContext.getPage().getName();

    NDC.push( wiki.getApplicationName()+":"+pagereq );    

    WikiPage wikipage = pageContext.getPage();
    WikiPage latestversion = wiki.getPage( pagereq );

    if( latestversion == null )
    {
        latestversion = wikiContext.getPage();
    }

    pageContext.setAttribute( WikiTagBase.ATTR_CONTEXT,
                              wikiContext,
                              PageContext.REQUEST_SCOPE );

    //
    //  Set the response type before we branch.
    //

    response.setContentType("text/html; charset="+wiki.getContentEncoding() );
    response.setHeader( "Cache-control", "max-age=0" );
    response.setDateHeader( "Expires", new Date().getTime() );
    response.setDateHeader( "Last-Modified", new Date().getTime() );

    //log.debug("Request character encoding="+request.getCharacterEncoding());
    //log.debug("Request content type+"+request.getContentType());
    log.debug("preview="+preview+", ok="+ok);

    if( ok != null )
    {
        log.info("Saving page "+pagereq+". User="+request.getRemoteUser()+", host="+request.getRemoteHost() );

        //  FIXME: I am not entirely sure if the JSP page is the
        //  best place to check for concurrent changes.  It certainly
        //  is the best place to show errors, though.
       
        long pagedate   = Long.parseLong(request.getParameter("edittime"));

        Date change = latestversion.getLastModified();

        if( change != null && change.getTime() != pagedate )
        {
            //
            // Someone changed the page while we were editing it!
            //

            log.info("Page changed, warning user.");

            pageContext.forward( "PageModified.jsp" );
            return;
        }

        //
        //  We expire ALL locks at this moment, simply because someone has
        //  already broken it.
        //
        PageLock lock = wiki.getPageManager().getCurrentLock( wikipage );
        wiki.getPageManager().unlockPage( lock );
        session.removeAttribute( "lock-"+pagereq );

        //
        //  If this is a comment, then we just append it to the page.
        //  If it is a full edit, then we will replace the previous contents.
        //
        if( comment != null )
        {
            StringBuffer pageText = new StringBuffer(wiki.getText( pagereq ));
            pageText.append( "\n\n----\n\n" );
            pageText.append( wiki.safeGetParameter( request, "text" ) );

            if( author != null )
            {
                Calendar cal = Calendar.getInstance();
                SimpleDateFormat fmt = new SimpleDateFormat("dd-MMM-yyyy");

                pageText.append("\n\n--"+author+", "+fmt.format(cal.getTime()));
            }

            wiki.saveText( wikipage, pageText.toString(), request );
        }
        else
        {
            wiki.saveText( wikipage,
                           wiki.safeGetParameter( request, "text" ),
                           request );
        }

        response.sendRedirect(wiki.getViewURL(pagereq));
        return;
    }
    else if( preview != null )
    {
        log.debug("Previewing "+pagereq);
        pageContext.forward( "Preview.jsp" );
    }
    else if( cancel != null )
    {
        log.debug("Cancelled editing "+pagereq);
        PageLock lock = (PageLock) session.getAttribute( "lock-"+pagereq );

        if( lock != null )
        {
            wiki.getPageManager().unlockPage( lock );
            session.removeAttribute( "lock-"+pagereq );
        }
        response.sendRedirect( wiki.getViewURL(pagereq) );
        return;
    }

    log.info("Editing page "+pagereq+". User="+request.getRemoteUser()+", host="+request.getRemoteHost() );

    //
    //  Determine and store the date the latest version was changed.  Since
    //  the newest version is the one that is changed, we need to track
    //  that instead of the edited version.
    //
    long lastchange = 0;
    
    Date d = latestversion.getLastModified();
    if( d != null ) lastchange = d.getTime();

    pageContext.setAttribute( "lastchange",
                              Long.toString( lastchange ),
                              PageContext.REQUEST_SCOPE );

    //
    //  Attempt to lock the page.
    //
    PageLock lock = wiki.getPageManager().lockPage( wikipage, 
                                                    wiki.getValidUserName(request) );

    if( lock != null )
    {
        session.setAttribute( "lock-"+pagereq, lock );
    }

    String contentPage = "templates/"+wikiContext.getTemplate()+"/EditTemplate.jsp";
%>

<wiki:Include page="<%=contentPage%>" />

<%
    NDC.pop();
    NDC.remove();
%>
