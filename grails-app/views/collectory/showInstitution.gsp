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
<html>
<head>
    <g:set var="collectoryService" bean="collectoryHubService"></g:set>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="breadcrumb" content="${instance.name}"/>
    <meta name="layout" content="${grailsApplication.config.skin.layout}"/>
    <title><ch:pageTitle>${instance.name}</ch:pageTitle></title>

    <script type="text/javascript" language="javascript" src="http://www.google.com/jsapi"></script>

    <asset:stylesheet src="collectory-hub"/>
    <asset:javascript src="collectory-hub"/>

    <asset:script type="text/javascript" disposition="head">
      biocacheServicesUrl = "${grailsApplication.config.biocacheServicesUrl}";
      biocacheWebappUrl = "${grailsApplication.config.grails.serverURL}";
      loadLoggerStats = ${!grailsApplication.config.disableLoggerLinks.toBoolean()};
      var COLLECTORY_CONF = { contextPath: "${grailsApplication.config.contextPath}", locale: "" }
      $(document).ready(function () {
        $("a#lsid").fancybox({
            'hideOnContentClick': false,
            'titleShow': false,
            'autoDimensions': false,
            'width': 600,
            'height': 180
        });
        $("a.current").fancybox({
            'hideOnContentClick': false,
            'titleShow': false,
            'titlePosition': 'inside',
            'autoDimensions': true,
            'width': 300
        });
      });
    </asset:script>
</head>

<body>
<div id="content">
    <div id="header" class="collectory">
        <div class="row">
            <div class="col-sm-8">
                <h1>${instance.name}</h1>
                <g:set var="parents" value="${instance.parentInstitutions}"/>
                <g:each var="p" in="${parents}">
                    <h2><g:link action="show" id="${p.uid}">${p.name}</g:link></h2>
                </g:each>
                <ch:valueOrOtherwise value="${instance.acronym}"><span
                        class="acronym">Acronym: ${instance.acronym}</span></ch:valueOrOtherwise>
            </div>

            <div class="col-sm-4">
                <g:if test="${instance.logoRef && instance.logoRef.uri}">
                    <img class="institutionImage"
                         src='${instance.logoRef.uri}'/>
                </g:if>
            </div>
        </div>
    </div><!--close header-->
    <div class="row">
        <div class="col-sm-8">
                <g:if test="${instance.pubDescription}">
                    <h2><g:message code="public.des" /></h2>
                    <ch:formattedText>${instance.pubDescription}</ch:formattedText>
                    <g:if test="${instance.techDescription}">
                        <ch:formattedText>${instance.techDescription}</ch:formattedText>
                    </g:if>
                </g:if>
                <g:if test="${instance.focus}">
                    <h2><g:message code="public.si.content.label02" /></h2>
                    <ch:formattedText>${instance.focus}</ch:formattedText>
                </g:if>
                <g:if test="${instance.collections?.size()}">
                    <h2><g:message code="public.si.content.label03" /></h2>
                    <ol>
                        <g:each var="c" in="${instance.collections.sort { it.name }}">
                            <li><g:link controller="public" action="show"
                                        id="${c.uid}">${c?.name}</g:link> ${c?.makeAbstract(400)}</li>
                        </g:each>
                    </ol>
                </g:if>
                <g:if test="instance.linkedRecordProviders?.size()">
                    <h2><g:message code="public.si.content.label05" /></h2>
                    <ol>
                        <g:each var="c" in="${instance.linkedRecordProviders.sort { it.name }}">
                            <li><g:link controller="collectory" action="showDataResource"
                                        id="${c.uid}">${c?.name}</g:link></li>
                        </g:each>
                    </ol>
                </g:if>
                <div id='usage-stats'>
                    <h2><g:message code="public.usagestats.label" /></h2>

                    <div id='usage'>
                        <p><g:message code="public.usage.des" />...</p>
                    </div>
                </div>

                <h2><g:message code="public.si.content.label04" /></h2>

                <div>
                    <p style="padding-bottom:8px;"><span
                            id="numBiocacheRecords"><g:message code="public.numbrs.des01" /></span> <g:message code="public.numbrs.des02" />.
                    </p>
                    %{--<ch:recordsLink--}%
                            %{--entity="${instance}"><g:message code="public.numbrs.link" /> ${instance.name}.</ch:recordsLink>--}%
                </div>

                <div id="recordsBreakdown" class="section vertical-charts">
                    <div id="charts"></div>
                </div>

                <ch:lastUpdated date="${instance.lastUpdated}"/>

            </div><!--close section-->
        <div class="col-sm-4">
                <g:if test="${instance.imageRef && instance.imageRef.file}">
                    <div class="section">
                        <img alt="${instance.imageRef.file}"
                             src="${resource(absolute: "true", dir: "data/institution/", file: instance.imageRef.file)}"/>
                        <ch:formattedText
                                pClass="caption">${instance.imageRef.caption}</ch:formattedText>
                        <ch:valueOrOtherwise value="${instance.imageRef?.attribution}"><p
                                class="caption">${instance.imageRef.attribution}</p></ch:valueOrOtherwise>
                        <ch:valueOrOtherwise value="${instance.imageRef?.copyright}"><p
                                class="caption">${instance.imageRef.copyright}</p></ch:valueOrOtherwise>
                    </div>
                </g:if>

                <div id="dataAccessWrapper" style="display:none;">
                    <g:render template="dataAccess" model="[instance:instance, facet:'institution_uid']"/>
                </div>

                <div class="section">
                    <h3><g:message code="public.location" /></h3>
                    <g:if test="${instance.address != null && !collectoryService.isAddressEmpty(instance.address)}">
                        <p>
                            <ch:valueOrOtherwise
                                    value="${instance.address?.street}">${instance.address?.street}<br/></ch:valueOrOtherwise>
                            <ch:valueOrOtherwise
                                    value="${instance.address?.city}">${instance.address?.city}<br/></ch:valueOrOtherwise>
                            <ch:valueOrOtherwise
                                    value="${instance.address?.state}">${instance.address?.state}</ch:valueOrOtherwise>
                            <ch:valueOrOtherwise
                                    value="${instance.address?.postcode}">${instance.address?.postcode}<br/></ch:valueOrOtherwise>
                            <ch:valueOrOtherwise
                                    value="${instance.address?.country}">${instance.address?.country}<br/></ch:valueOrOtherwise>
                        </p>
                    </g:if>
                    <g:if test="${instance.email}"><ch:emailLink>${instance.email}</ch:emailLink><br/></g:if>
                    <ch:ifNotBlank value='${instance.phone}'/>
                </div>

            <!-- contacts -->
                <g:render template="contacts" model="${[contacts: collectoryService.getPublicContactsPrimaryFirst(instance.contacts)]}"/>

            <!-- web site -->
                <g:if test="${instance.websiteUrl}">
                    <div class="section">
                        <h3><g:message code="public.website" /></h3>

                        <div class="webSite">
                            <a class='external' target="_blank"
                               href="${instance.websiteUrl}"><g:message code="public.si.website.link01" /> <ch:institutionType
                                    inst="${instance}"/><g:message code="public.si.website.link02" /></a>
                        </div>
                    </div>
                </g:if>

            </div>
        </div><!--close content-->
</div>
<asset:script type="text/javascript">
      // configure the charts
      var facetChartOptions = {
          /* base url of the collectory */
          collectionsUrl: "${grailsApplication.config.grails.serverURL}",
          /* base url of the biocache ws*/
          biocacheServicesUrl: biocacheServicesUrl,
          /* base url of the biocache webapp*/
          biocacheWebappUrl: biocacheWebappUrl,
          /* a uid or list of uids to chart - either this or query must be present
            (unless the facet data is passed in directly AND clickThru is set to false) */
          instanceUid: "${collectoryService.descendantUids(instance)?.join(',')}",
          /* the list of charts to be drawn (these are specified in the one call because a single request can get the data for all of them) */
          charts: ['country','state','species_group','assertions','type_status',
              'biogeographic_region','state_conservation','occurrence_year']
      }
      var taxonomyChartOptions = {
          /* base url of the collectory */
          collectionsUrl: "${grailsApplication.config.grails.serverURL}",
          /* base url of the biocache ws*/
          biocacheServicesUrl: biocacheServicesUrl,
          /* base url of the biocache webapp*/
          biocacheWebappUrl: biocacheWebappUrl,
          /* a uid or list of uids to chart - either this or query must be present */
          instanceUid: "${collectoryService.descendantUids(instance)?.join(',')}",
          /* threshold value to use for automagic rank selection - defaults to 55 */
          threshold: 55,
          rank: "${null}"
      }

    /************************************************************\
    * Actions when page is loaded
    \************************************************************/
    function onLoadCallback() {

      // stats
      if(loadLoggerStats){
        loadDownloadStats("${grailsApplication.config.logger.baseURL}", "${instance.uid}","${instance.name}", "1002");
      }

      // records
      $.ajax({
        url: urlConcat(biocacheServicesUrl, "/occurrences/search.json?pageSize=0&q=") + buildQueryString("${collectoryService.descendantUids(instance)?.join(',')}"),
        dataType: 'jsonp',
        timeout: 20000,
        complete: function(jqXHR, textStatus) {
            if (textStatus == 'timeout') {
                noData();
                alert('Sorry - the request was taking too long so it has been cancelled.');
            }
            if (textStatus == 'error') {
                noData();
                alert('Sorry - the records breakdowns are not available due to an error.');
            }
        },
        success: function(data) {
            // check for errors
            if (data.length == 0 || data.totalRecords == undefined || data.totalRecords == 0) {
                noData();
            } else {
                setNumbers(data.totalRecords);
                // draw the charts
                drawFacetCharts(data, facetChartOptions);
                if(data.totalRecords > 0){
                    $('#dataAccessWrapper').css({display:'block'});
                    $('#totalRecordCountLink').html(data.totalRecords.toLocaleString() + ' records');
                }
            }
        }
      });

      // taxon chart
      loadTaxonomyChart(taxonomyChartOptions);
    }
    /************************************************************\
    *
    \************************************************************/

    google.load("visualization", "1", {packages:["corechart"]});
    google.setOnLoadCallback(onLoadCallback);

</asset:script>
</body>
</html>