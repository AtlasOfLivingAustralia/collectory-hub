<div class="section">
    <h3><g:message code="dataAccess.title"/></h3>
    <div class="dataAccess btn-group-vertical">
        <h4><a id="totalRecordCountLink" href="${grailsApplication.config.grails.serverURL}/occurrences/search?q=${facet}:${instance.uid}&fq=${grailsApplication.config.hub.queryContext}"></a></h4>

        <a href="${grailsApplication.config.grails.serverURL}/occurrences/search?q=${facet}:${instance.uid}&fq=${grailsApplication.config.hub.queryContext}" class="btn"><i class="icon icon-list"></i> <g:message code="dataAccess.view.records"/></a>

        <g:if test="${!grailsApplication.config.disableLoggerLinks.toBoolean() && grailsApplication.config.loggerURL}">
            <a href="${grailsApplication.config.loggerURL}/reasonBreakdownCSV?eventId=1002&entityUid=${instance.uid}" class="btn"><i class="icon icon-download-alt"></i> <g:message code="dataAccess.download.stats"/></a>
        </g:if>

        <ch:createNewRecordsAlertsLink query="${facet}:${instance.uid}" displayName="${instance.name}"
            linkText="${g.message(code:'dataAccess.alert.records.alt')}" altText="${g.message(code:'dataAccess.alert.records')} ${instance.name}"/>

        <ch:createNewAnnotationsAlertsLink query="${facet}:${instance.uid}" displayName="${instance.name}"
            linkText="${g.message(code:'dataAccess.alert.annotations.alt')}" altText="${g.message(code:'dataAccess.alert.annotations')} ${instance.name}"/>
    </div>
</div>