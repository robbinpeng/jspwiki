/* 
    JSPWiki - a JSP-based WikiWiki clone.

    Licensed to the Apache Software Foundation (ASF) under one
    or more contributor license agreements.  See the NOTICE file
    distributed with this work for additional information
    regarding copyright ownership.  The ASF licenses this file
    to you under the Apache License, Version 2.0 (the
    "License"); you may not use this file except in compliance
    with the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing,
    software distributed under the License is distributed on an
    "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
    KIND, either express or implied.  See the License for the
    specific language governing permissions and limitations
    under the License.  
 */
/*
 * (C) Janne Jalkanen 2005
 * 
 */
package org.apache.wiki.ui;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class RedirectCommandTest extends TestCase
{
    protected void tearDown() throws Exception
    {
    }

    public void testStaticCommand()
    {
        Command a = RedirectCommand.REDIRECT;
        assertEquals( "", a.getRequestContext() );
        assertEquals( "", a.getJSP() );
        assertEquals( "%u%n", a.getURLPattern() );
        assertNull( a.getContentTemplate() );
        assertNull( a.getTarget() );
        assertEquals( a, RedirectCommand.REDIRECT );
    }
    
    public void testTargetedCommand()
    {
        Command a = RedirectCommand.REDIRECT;
        
        // Test with local JSP
        Command b = a.targetedCommand( "%uTestPage.jsp" );
        assertEquals( "", b.getRequestContext() );
        assertEquals( "TestPage.jsp", b.getJSP() );
        assertEquals( "%uTestPage.jsp", b.getURLPattern() );
        assertNull( b.getContentTemplate() );
        assertEquals( "%uTestPage.jsp", b.getTarget() );
        assertNotSame( RedirectCommand.REDIRECT, b );
        
        // Test with non-local URL
        b = a.targetedCommand( "http://www.yahoo.com" );
        assertEquals( "", b.getRequestContext() );
        assertEquals( "http://www.yahoo.com", b.getJSP() );
        assertEquals( "http://www.yahoo.com", b.getURLPattern() );
        assertNull( b.getContentTemplate() );
        assertEquals( "http://www.yahoo.com", b.getTarget() );
        assertNotSame( RedirectCommand.REDIRECT, b );
    }
    
    public static Test suite()
    {
        return new TestSuite( RedirectCommandTest.class );
    }
}