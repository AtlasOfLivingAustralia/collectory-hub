<%@ page contentType="text/html;charset=UTF-8" %>
<html xmlns="http://www.w3.org/1999/html">
<head>
    <title>My datasets | ${grailsApplication.config.skin.appName} | ${grailsApplication.config.skin.orgNameLong}</title>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="layout" content="${grailsApplication.config.skin.layout}"/>
</head>

<body>
<div class="panel panel-default">
    <div class="panel-heading">
        <div class="well pull-right col-sm-3">
            <strong>Tip:</strong> To share your list of datasets with other users, use
        <g:link controller="myDatasets" action="userDatasets" params="${[userId: currentUserId]}">this link</g:link>.
        </div>

        <h1>${grailsApplication.config.skin.appName} - My uploaded datasets</h1>
    </div>

    <div class="panel-body">
        <g:if test="${userUploads}">

            <g:if test="${params.containsKey("deleteSuccess")}">
                <g:if test="${params.deleteSuccess}">
                    <div class="alert alert-info" role="alert">
                        Dataset deleted.
                    </div>
                </g:if>
                <g:else>
                    <div class="alert alert-error" role="alert">
                        Unable to delete this dataset.
                    </div>
                </g:else>
            </g:if>
            <p class="lead">
                Here is a listing of your previously uploaded datasets.<br/>
            </p>

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
                        <g:each in="${userUploads}" var="userUpload">
                            <tr>
                                <td>${userUpload.alaId}</td>
                                <td>${userUpload.uid}</td>
                                <td>${userUpload.name}</td>
                                <td>${userUpload.numberOfRecords}</td>
                                <td>${userUpload.dateCreated}</td>
                                <td>${userUpload.lastUpdated}</td>
                                <td><g:message code="tempDataResource.status.${userUpload.status}" default="${userUpload.status}"></g:message></td>
                                <td>
                                    <g:link class="btn btn-default" controller="tempDataResource" action="viewMetadata"
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

                                    <g:link class="btn btn-default" controller="tempDataResource" action="reload"
                                            params="${[uid: userUpload.uid]}">
                                        <i class="icon-repeat"></i> Reload
                                    </g:link>

                                    <g:link controller="tempDataResource" action="delete"
                                            class="btn btn-danger" params="[uid: userUpload.uid]">Delete</g:link>
                                </td>
                            </tr>
                        </g:each>
                    </table>
                </div>
            </div>
        </g:if>
        <g:else>
            <p class="lead">
                You currently have no uploaded datasets. <g:link uri="/">Click here</g:link> to upload data.</p>
        </g:else>
    </div>
</div>
</body>
</html>