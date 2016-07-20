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
 * Created by Temi on 12/07/2016.
 */
-->
<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <title>Edit metadata | ${grailsApplication.config.skin.appName} | ${grailsApplication.config.skin.orgNameLong}</title>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="layout" content="${grailsApplication.config.skin.layout}"/>
</head>

<body>
<ol class="breadcrumb">
    <li><a href="${createLink(uri: '/')}">Home</a></li>
    <li><a href="${createLink(controller: 'tempDataResource', action: 'myData')}">My datasets</a></li>
    <li class="active">Dataset</li>
</ol>

<div class="panel panel-default">
    <div class="panel-heading">
        <h1>Edit metadata</h1>
    </div>
    <form class="form-horizontal" action="${createLink(controller:'tempDataResource', action: 'saveTempDataResource')}"
        method="post">
    <div class="panel-body">
            <div class="form-group">
                <label for="uid" class="col-sm-2 control-label">Dataset id</label>
                <div class="col-sm-10">
                    <input type="text" class="form-control" id="uid" name="uid" readonly value="${uid}">
                </div>
            </div>
            <div class="form-group">
                <label for="type" class="col-sm-2 control-label">Type</label>
                <div class="col-sm-10">
                    <input type="text" class="form-control" id="type" name="type" readonly value="${type}">
                </div>
            </div>
            <div class="form-group">
                <label for="name" class="col-sm-2 control-label">Dataset name</label>
                <div class="col-sm-10">
                    <input type="text" class="form-control" id="name" name="name" value="${name}">
                </div>
            </div>
            <div class="form-group">
                <label for="dateCreated" class="col-sm-2 control-label">Date created</label>
                <div class="col-sm-10">
                    <input type="text" class="form-control" id="dateCreated" name="dateCreated" readonly value="${dateCreated}">
                </div>
            </div>
            <div class="form-group">
                <label for="lastUpdated" class="col-sm-2 control-label">Last updated</label>
                <div class="col-sm-10">
                    <input type="text" class="form-control" id="lastUpdated" name="lastUpdated" readonly value="${lastUpdated}">
                </div>
            </div>
            <div class="form-group">
                <label for="status" class="col-sm-2 control-label">Status</label>
                <div class="col-sm-10">
                    <span class="form-control" id="status" readonly>
                           <g:message code="tempDataResource.status.${status}" default="${status}"></g:message></span>
                </div>
            </div>
            <div class="form-group">
                <label for="owner" class="col-sm-2 control-label">Owner</label>
                <div class="col-sm-10">
                    <input type="text" name="displayName" class="form-control" id="owner" readonly value="${displayName}">
                </div>
            </div>
            <div class="form-group">
                <label for="isContactPublic" class="col-sm-2 control-label">Make contact email public</label>
                <div class="col-sm-10">
                    <input type="checkbox" class="form-control" id="isContactPublic" name="isContactPublic"
                        ${(isContactPublic?:'false').toBoolean()?'checked':''} value="true"/>
                </div>
            </div>
            <div class="form-group">
                <label for="description" class="col-sm-2 control-label">Description</label>
                <div class="col-sm-10">
                    <textarea class="form-control" id="description" name="description">${description}</textarea>
                </div>
            </div>
            <div class="form-group">
                <label for="dataGeneralisations" class="col-sm-2 control-label">Data generalisations</label>
                <div class="col-sm-10">
                    <textarea class="form-control" id="dataGeneralisations" name="dataGeneralisations">${dataGeneralisations}</textarea>
                </div>
            </div>
            <div class="form-group">
                <label for="informationWithheld" class="col-sm-2 control-label">Information withheld</label>
                <div class="col-sm-10">
                    <textarea type="text" class="form-control" id="informationWithheld" name="informationWithheld">${informationWithheld}</textarea>
                </div>
            </div>
            <div class="form-group">
                <label for="license" class="col-sm-2 control-label">License:</label>
                <div class="col-sm-10">
                    <select type="text" class="form-control" id="license" name="license" value="${license}">
                        <option>CC-BY</option>
                        <option>CC-BY-NC</option>
                        <option>CC-0 3.0 Australia</option>
                        <option>CC-0 4.0 International</option>
                    </select>
                </div>
            </div>
            <div class="form-group">
                <label for="citation" class="col-sm-2 control-label">Citation</label>
                <div class="col-sm-10">
                    <textarea type="text" class="form-control" id="citation" name="citation">${citation}</textarea>
                </div>
            </div>
            <div class="form-group">
                <label for="sourceFileUrl" class="col-sm-2 control-label">Source File</label>
                <div class="col-sm-10">
                    <a id="sourceFileUrl" name="sourceFileUrl" class="form-control" href="${sourceFileUrl}">${sourceFileUrl}</a>
                </div>
            </div>
            <div class="form-group">
                <label for="sandboxLink" class="col-sm-2 control-label">Sandbox link</label>
                <div class="col-sm-10">
                    <a id="sandboxLink" name="sandboxLink" class="form-control" href="${sandboxLink}">${sandboxLink}</a>
                </div>
            </div>
    </div>
    <div class="panel-footer">
        <div class="row">
            <div class="col-sm-offset-2 col-sm-10">
                <div class="pull-right">
                    <button type="submit" class="btn btn-primary">Save</button>
                    <a class="btn btn-default"
                            href="${createLink(controller: 'tempDataResource', action: 'viewMetadata', params:[uid: uid])}">
                        Cancel
                    </a>
                </div>
            </div>
        </div>
    </div>
    </form>
</div>
</body>
</html>