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
<%@ page contentType="text/html;charset=UTF-8" %>
<html xmlns="http://www.w3.org/1999/html">
<head>
    <title>My datasets | ${grailsApplication.config.skin.appName} | ${grailsApplication.config.skin.orgNameLong}</title>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="layout" content="${grailsApplication.config.skin.layout}"/>
    <r:require modules="commonStyles"></r:require>
    <g:set var="statusMessage" value="${message(code: "tempDataResource.status.${params.status}")}"/>
</head>

<body>
<ol class="breadcrumb">
    <li><a href="${createLink(uri: '/')}">Home</a></li>
    <li class="active">My datasets</li>
</ol>

<div class="panel panel-default">
    <div class="panel-heading">
        <h1>${grailsApplication.config.skin.appName} - My uploaded datasets</h1>
    </div>

    <div class="panel-body">
        <p class="lead">
            Here is a listing of your previously uploaded datasets.<br/>
        </p>

        <div class="row">
            <div class="col-sm-9">
            </div>

            <div class="col-sm-3">
                <g:link uri="/" class="btn btn-primary pull-right">Add new dataset</g:link>
            </div>
        </div>

        <div class="row">
            <div class="col-sm-12">
                <table class="table">
                    <thead>
                    <tr>
                        <th>User Id</th>
                        <th>Dataset Id</th>
                        <th>Dataset name</th>
                        <th>Number of records</th>
                        <th>Date created</th>
                        <th>Date updated</th>
                        <th>Status</th>
                        <th></th>
                    </tr>
                    </thead>
                    <g:if test="${resources}">
                        <g:each in="${resources}" var="userUpload">
                            <tr>
                                <td>${userUpload.alaId}</td>
                                <td>${userUpload.uid}</td>
                                <td>${userUpload.name}</td>
                                <td>${userUpload.numberOfRecords}</td>
                                <td>${userUpload.dateCreated}</td>
                                <td>${userUpload.lastUpdated}</td>
                                <td><g:message code="tempDataResource.status.${userUpload.status}"
                                               default="${userUpload.status}"></g:message></td>
                                <td>
                                    <g:link class="btn btn-default btn-primary" controller="tempDataResource"
                                            action="viewMetadata"
                                            params="${[uid: userUpload.uid]}">
                                        <i class="icon-cog"></i> View details
                                    </g:link>

                                    <a class="btn btn-default"
                                       href="${userUpload.uiUrl ?: grailsApplication.config.sandboxHubsWebapp}/occurrences/search?q=data_resource_uid:${userUpload.uid}"><i
                                            class="icon-th-list"></i> View records</a>

                                    <g:if test="${!grailsApplication.config.sandbox.hideCharts.toBoolean()}">
                                        <g:link class="btn btn-default" controller="myDatasets" action="chartOptions"
                                                params="${[tempUid: userUpload.uid]}">
                                            <i class="icon-cog"></i> Configure charts
                                        </g:link>
                                    </g:if>
                                </td>
                            </tr>
                        </g:each>
                    </g:if>
                    <g:else>
                        <tr>
                            <td colspan="8">
                                <h4>Could not find any datasets ${params.status ? "with status ${statusMessage}" : ''}.</h4>
                            </td>
                        </tr>
                    </g:else>
                </table>
            </div>
        </div>

        <div class="row">
            <div class="col-sm-8">
                <div class="form-horizontal">
                    <div class="form-group">
                        <label class="col-sm-2 control-label">Order by:</label>

                        <div class="col-sm-10">
                            <div class="btn-group" role="group">
                                <g:link class="btn btn-default ${params.sortField == 'dateCreated' ? 'active' : ''}"
                                        controller="tempDataResource" action="myData"
                                        params="${params + [sortField: 'dateCreated', sortOrder: 'desc']}">Date created</g:link>
                                <g:link class="btn btn-default ${params.sortField == 'lastUpdated' ? 'active' : ''}"
                                        controller="tempDataResource" action="myData"
                                        params="${params + [sortField: 'lastUpdated', sortOrder: 'desc']}">Last updated</g:link>
                                <g:link class="btn btn-default ${(params.sortField == 'numberOfRecords') && (params.sortOrder == 'desc') ? 'active' : ''}"
                                        controller="tempDataResource" action="myData"
                                        params="${params + [sortField: 'numberOfRecords', sortOrder: 'desc']}">Most number of records</g:link>
                                <g:link class="btn btn-default ${(params.sortField == 'numberOfRecords') && (params.sortOrder == 'asc') ? 'active' : ''}"
                                        controller="tempDataResource" action="myData"
                                        params="${params + [sortField: 'numberOfRecords', sortOrder: 'asc']}">Least number of records</g:link>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <div class="col-sm-4">
                <g:paginate class="pull-right pagination-control" next="Next" prev="Previous"
                            maxsteps="0" controller="tempDataResource"
                            action="myData" total="${total}" params="${params}"/>
            </div>
        </div>

        <div class="row">
            <div class="col-sm-8">
                <div class="form-horizontal">
                    <div class="form-group">
                        <label class="col-sm-2 control-label">Filter by status:</label>
                        <div class="col-sm-10">
                            <div class="btn-group" role="group">
                                <g:link class="btn btn-default ${params.status in [null, ''] ? 'active' : ''}"
                                        controller="tempDataResource" action="myData"
                                        params="${[status: '']}">All</g:link>
                                <g:each in="${statuses}" var="status">
                                    <g:link class="btn btn-default ${status == params.status ? 'active' : ''}"
                                            controller="tempDataResource" action="myData"
                                            params="${[status: status]}"><g:message
                                            code="tempDataResource.status.${status}"></g:message></g:link>
                                </g:each>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>
</div>
</body>
</html>