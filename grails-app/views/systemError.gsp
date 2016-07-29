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
 * Created by Temi on 19/07/2016.
 */
-->
<!DOCTYPE html>
<html>
<head>
    <title><g:if env="development">Grails Runtime Exception</g:if><g:else>Error</g:else></title>
    <meta name="layout" content="${grailsApplication.config.skin.layout}"/>
</head>
<body>
    <h2>Error</h2>
    <p>An error has occurred.</p>
    <br>
    <h4>Quick links</h4>
    <ol>
        <li><a href="${createLink(uri: '/')}">Home</a></li>
        <li><a href="${createLink(controller: 'tempDataResource', action: 'myData',)}">My datasets</a></li>
    </ol>
</body>
</html>

