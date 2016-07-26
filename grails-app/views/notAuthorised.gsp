<%@ page import="org.apache.commons.httpclient.util.URIUtil" %>
<!--
/*
 * Copyright (C) 2016 Atlas of Living Australia
 * All Rights Reserved.
 *
 * The contents of this file are subject to the Mozilla Public
 * License Version 1.1 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of
 * the License at http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS
 * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * rights and limitations under the License.
 * 
 * Created by Temi on 26/07/2016.
 */
-->
<!DOCTYPE html>
<html>
<head>
    <title>Authorisation failed!</title>
    <meta name="layout" content="${grailsApplication.config.skin.layout}"/>
</head>
<body>

<h3>Authorisation failed!</h3>
<p>You are not authorised to do the chosen action. Are you logged in? <a href="${grailsApplication.config.casServerLoginUrl}?service=${org.apache.commons.httpclient.util.URIUtil.encodeWithinQuery(createLink( uri: "/", absolute: true))}">Click here to login</a></p>
<br>
<h3>Quick links</h3>
<ol>
    <li><a href="${createLink(uri: '/')}">Home</a></li>
    <li><a href="${createLink(controller: 'tempDataResource', action: 'myData',)}">My datasets</a></li>
</ol>
</body>
</html>