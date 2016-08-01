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
 * Created by Temi on 24/07/2016.
 */
-->
<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <title>Console | ${grailsApplication.config.skin.appName} | ${grailsApplication.config.skin.orgNameLong}</title>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="layout" content="${grailsApplication.config.skin.layout}"/>
    <r:require modules="commonStyles, console"></r:require>
</head>

<body>
<ol class="breadcrumb">
    <li><a href="${createLink(uri: '/')}">Home</a></li>
    <li><a href="${createLink(controller: 'tempDataResource', action: 'adminList')}">Datasets</a></li>
    <li><a href="${createLink(controller: 'tempDataResource', action: 'viewMetadata', params:[uid: uid])}">Dataset</a></li>
    <li class="active">Jenkins console</li>
</ol>

<div class="panel console-top">
    <div class="panel-body console console-height">
        <div id="console-content">
            <a class="pull-right btn btn-default btn-sm" id="follow" href="#" onclick="">Follow log</a>
            <pre id="console-output" class="borderless console" ><g:if test="${text}">${text}</g:if></pre>
            <div id="last-line">
            </div>
            <div class="row hide" id="console-loading">
                <div class="col-sm-12">
                    <i class="fa fa-cog fa-2x fa-spin"><span class="sr-only">Loading...</span></i>
                </div>
            </div>
            <a id="top" class="pull-right btn btn-default btn-sm" href="#">Top</a>
        </div>
    </div>
</div>
%{--<g:if test="${isMoreData}">--}%
<script>
    var url = "${createLink(controller: 'jenkins', action:  'console')}/${jobName}/${buildNumber}/",
        nextStart = ${nextStart},
        follow = false,
        headerHeight = 60;
    $(document).ready(function () {
        fetchMessages(nextStart)
        $(window).scroll(function () {
            followButtonPosition()
            topButtonPosition()
        });
        $('#top').on('click', reachTop)
        $('#follow').on('click', setFollow)
    })
</script>
%{--</g:if>--}%
</body>
</html>