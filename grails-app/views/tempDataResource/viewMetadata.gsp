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
 * Created by Temi on 13/07/2016.
 */
-->
<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <title>Metadata | ${grailsApplication.config.skin.appName} | ${grailsApplication.config.skin.orgNameLong}</title>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="layout" content="${grailsApplication.config.skin.layout}"/>
    <r:require modules="commonStyles"></r:require>
</head>

<body>
<ol class="breadcrumb">
    <li><a href="${createLink(uri: '/')}">Home</a></li>
    <li><a href="${createLink(controller: 'tempDataResource', action: 'myData',)}">My datasets</a></li>
    <li class="active">Dataset</li>
</ol>

<div class="panel panel-default">
    <div class="panel-heading">
        <h1>${name}</h1>
    </div>
    <table class="table table-borderless">
        <tr>
            <td class="text-right"><b>Dataset id</b></td>
            <td>${uid}</td>
        </tr>
        <tr>
            <td class="text-right"><b>Type</b></td>
            <td>${type}</td>
        </tr>
        <tr>
            <td class="text-right"><b>Dataset name</b></td>
            <td>${name}</td>
        </tr>
        <tr>
            <td class="text-right"><b>Date created</b></td>
            <td>${dateCreated}</td>
        </tr>
        <tr>
            <td class="text-right"><b>Last updated</b></td>
            <td>${lastUpdated}</td>
        </tr>
        <tr>
            <td class="text-right"><b>Status</b></td>
            <td><g:message code="tempDataResource.status.${status}" default="${status}"></g:message></td>
        </tr>
        <tr>
            <td class="text-right"><b>Owner</b></td>
            <td>${displayName}</td>
        </tr>
        <tr>
            <td class="text-right"><b>Make contact email public</b></td>
            <td>${isContactPublic}</td>
        </tr>
        <tr>
            <td class="text-right"><b>Description</b></td>
            <td>${description}</td>
        </tr>
        <tr>
            <td class="text-right"><b>Data generalisations</b></td>
            <td>${dataGeneralisations}</td>
        </tr>
        <tr>
            <td class="text-right"><b>Information withheld</b></td>
            <td>${informationWithheld}</td>
        </tr>
        <tr>
            <td class="text-right"><b>License</b></td>
            <td>${license}</td>
        </tr>
        <tr>
            <td class="text-right"><b>Citation</b></td>
            <td>${citation}</td>
        </tr>
        <tr>
            <td class="text-right"><b>Source file</b></td>
            <td><a id="sourceFile" href="${sourceFileUrl}">${sourceFileUrl}</a></td>
        </tr>
        <tr>
            <td class="text-right"><b>Sandbox link</b></td>
            <td><a id="sandboxLink" href="${sandboxLink}">${sandboxLink}</a></td>
        </tr>
        <g:if test="${collectoryLink}">
            <tr>
                <td class="text-right"><b>Collectory link</b></td>
                <td><a id="collectoryLink" href="${collectoryLink}">${collectoryLink}</a></td>
            </tr>
        </g:if>
    </table>
    <g:if test="${canEdit}">
        <div class="panel-footer">
            <div class="row">
                <div class="col-sm-offset-2 col-sm-10">
                    <g:link controller="tempDataResource" action="editMetadata" params="${[uid: uid]}"
                            class="btn btn-primary pull-right">Edit</g:link>
                </div>
            </div>
        </div>
    </g:if>
</div>

<div class="panel panel-default">
    <div class="panel-body">
        <div class="row">
            <div class="col-sm-12">
                <p>Data actions</p>
                <button class="btn btn-default" onclick="window.location = '${sandboxLink}'">View records</button>
                <g:if test="${reloadLink && canEdit}">
                    <button type="submit" class="btn btn-default" onclick="window.location = '${reloadLink}'">Reload data</button>
                </g:if>
                <g:if test="${deleteLink && canEdit}">
                    <button type="submit" class="btn btn-default btn-danger" onclick="window.location = '${deleteLink}'">Delete</button>
                </g:if>
            </div>
        </div>
        <g:if test="${canEdit}">
            <br>
            <div class="row">
                <div class="col-sm-12">
                    <p>Do you want this data to be shared on the Atlas of Living Australia?</p>
                    <button type="submit" class="btn btn-default"
                            onclick="window.location =
                            '${createLink(controller: 'tempDataResource', action: 'submitDataForReview', params: [uid: uid])}'">
                        Submit for review
                    </button>
                </div>
            </div>
        </g:if>
        <g:if test="${isAdmin}">
            <br>
            <div class="row">
                <div class="col-sm-12">
                    <p>Admin actions</p>
                    <button class="btn btn-default" ${canDecline?:'disabled'}
                            onclick="window.location =
                            '${createLink(controller: 'tempDataResource', action: 'decline', params: [uid: uid])}'">
                        Decline
                    </button>

                    <button type="submit"  ${canLoadToProduction?:'disabled'} class="btn btn-default btn-danger"
                            onclick="window.location =
                                    '${createLink(controller: 'tempDataResource', action: 'loadToProduction',
                                     params: [uid: uid])}'">
                        Publish on production
                    </button>
                </div>
            </div>
        </g:if>
    </div>
</div>
</body>
</html>