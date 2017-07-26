<%@ page contentType="text/html;charset=UTF-8" %>
<html xmlns="http://www.w3.org/1999/html">
<head>
    <title>Chart options | ${grailsApplication.config.skin.appName} | ${grailsApplication.config.skin.orgNameLong}</title>
    <meta name="breadcrumbs"
          content="${grailsApplication.config.skin.appName ? grailsApplication.config.grails.serverURL + ',' + grailsApplication.config.skin.appName + '\\' : ''}${createLink(controller: 'tempDataResource', action: 'myData')},My datasets"/>
    <meta name="breadcrumb" content="Chart options"/>

    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <meta name="layout" content="${grailsApplication.config.skin.layout}" />
    <asset:stylesheet src="temp-style"/>
</head>
<body>
<h1>${metadata.name} - Chart display options</h1>
<div class="panel panel-default">
    <div class="panel-body">
    <g:form action="saveChartOptions">
        <input type="hidden" name="tempUid" value="${tempUid}"/>
        <input type="hidden" name="origin" value="${origin}"/>

    <div class="pull-right">
        <input class="btn btn-primary" type="submit" value="Save" />
    </div>
    <p>
        Configure the chart to show for your dataset.<br/>
        You can drag and drop the table to adjust the order the charts are presented.
        <br/>
        Please click save to store your configuration for this dataset.
    </p>
    <table class="table table-condensed table-striped">
        <thead>
        <tr>
            <th>Field name</th>
            <th>Chart type</th>
            <th>Show</th>
        </tr>
        </thead>
        <tbody class="customIndexes">
        <g:each in="${chartConfig}" var="chartCfg" status="indexStatus">
            <tr>
                <td>
                    <g:formatFieldName value="${chartCfg.field}"/>
                    <input type="hidden" name="field" value="${chartCfg.field}"/>
                </td>
                <td>
                    <g:select name="format" from="['pie', 'bar', 'column', 'line', 'scatter']" value="${chartCfg.format}"/>
                </td>
                <td>
                    <input type="hidden" name="visible_${indexStatus}" value="off" />
                    <input type="checkbox" name="visible_${indexStatus}"
                           <g:if test="${chartCfg.visible.toBoolean()}">checked="true"</g:if>
                    />
                </td>
            </tr>
        </g:each>
        </tbody>
    </table>
    <div class="pull-right">
        <input class="btn btn-primary" type="submit" value="Save" />
        <g:link class="btn btn-default" controller="tempDataResource" action="${origin != 'adminList' ? 'myData': 'adminList'}">Cancel</g:link>
    </div>

    </g:form>
    </div>
</div>
</body>

<asset:script>
    $(function  () {
        $(".customIndexes").sortable();
        $( ".customIndexes" ).disableSelection();
    });
</asset:script>

</html>