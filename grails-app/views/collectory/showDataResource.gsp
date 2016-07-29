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
<%@ page import="java.text.DecimalFormat; java.text.SimpleDateFormat" %>
<g:set var="collectoryService" bean="collectoryHubService"></g:set>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="layout" content="${grailsApplication.config.skin.layout}"/>
    <title><ch:pageTitle>${instance.name}</ch:pageTitle></title>
    <r:script disposition="head">
        var COLLECTORY_CONF = { contextPath: "${grailsApplication.config.contextPath}", locale: "" }
        // define biocache server
        bieUrl = "${grailsApplication.config.bie.baseURL}";
        loadLoggerStats = ${!grailsApplication.config.disableLoggerLinks.toBoolean()};
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
    </r:script>
    <r:require modules="collectory"></r:require>
    <r:require modules="jquery, fancybox, jquery_jsonp, jstree, jquery_ui_custom, charts, datadumper"/>
</head>
<body class="nav-datasets">
<div id="content">
<div id="header">
    <div class="row-fluid">
        <div class="span8">
            <h1>${instance.name}</h1>
            <g:set var="dp" value="${instance.dataProvider}"/>
            <g:if test="${dp}">
                <h2><g:link action="show" id="${dp.uid}">${dp.name}</g:link></h2>
            </g:if>
            <g:if test="${instance.linkedRecordConsumers}">
                <h2><ch:getInstitutionLink institutions="${instance.linkedRecordConsumers}"></ch:getInstitutionLink></h2>
            </g:if>
            <ch:valueOrOtherwise value="${instance.acronym}"><span
                    class="acronym">Acronym: ${instance.acronym}</span></ch:valueOrOtherwise>
            <g:if test="${instance.guid}">
                <span class="lsid"><a href="#lsidText" id="lsid" class="local"
                                  title="Life Science Identifier (pop-up)"><g:message code="public.lsid" /></a></span>
            </g:if>
            <div style="display:none; text-align: left;">
                <div id="lsidText" style="text-align: left;">
                    <b><a class="external_icon" href="http://lsids.sourceforge.net/"
                          target="_blank"><g:message code="public.lsidtext.link" />:</a></b>

                    %{--<p><ch:guid target="_blank" guid='${instance.guid}'/></p>--}%

                    <p><g:message code="public.lsidtext.des" />.</p>
                </div>
            </div>
        </div>

        <div class="span4">
            <g:if test="${dp?.logoRef?.file}">
                <g:link action="show" id="${dp.uid}">
                    <img class="institutionImage"
                         src='${resource(absolute: "true", dir: "data/dataProvider/", file: dp.logoRef.file)}'/>
                </g:link>
            </g:if>
            <g:elseif test="${instance?.logoRef?.file}">
                <img class="institutionImage"
                     src='${resource(absolute: "true", dir: "data/dataResource/", file: instance.logoRef.file)}'/>
            </g:elseif>
        </div>
    </div>
</div><!--close header-->
<div class="row-fluid">
    <div class="span8">
        <g:if test="${instance.pubDescription || instance.techDescription || instance.focus}">
            <h2><g:message code="public.des" /></h2>
        </g:if>
        <g:if test="${instance.pubDescription}">
            <ch:formattedText>${instance.pubDescription}</ch:formattedText>
        </g:if>
        <g:if test="${instance.techDescription}">
            <ch:formattedText>${instance.techDescription}</ch:formattedText>
        </g:if>
        <g:if test="${instance.focus}">
            <ch:formattedText>${instance.focus}</ch:formattedText>
        </g:if>
        <g:if test="${instance.resourceType && instance.status}">
            <ch:dataResourceContribution resourceType="${instance.resourceType}" status="${instance.status}" tag="p"/>
        </g:if>

        <g:if test="${instance.contentTypes}">
            <h2><g:message code="public.sdr.content.label02" /></h2>
            <ch:contentTypes types="${instance.contentTypes}"/>
        </g:if>
        <h2><g:message code="public.sdr.content.label03" /></h2>
        <g:if test="${instance.citation}">
            <ch:formattedText>${instance.citation}</ch:formattedText>
        </g:if>
        <g:else>
            <p><g:message code="public.sdr.content.des01" />.</p>
        </g:else>

        <g:if test="${instance.rights || instance.creativeCommons}">
            <h2><g:message code="public.sdr.content.label04" /></h2>
            <ch:formattedText>${instance.rights}</ch:formattedText>
            <g:if test="${instance.creativeCommons}">
                <p><ch:displayLicenseType type="${instance.licenseType}" version="${instance.licenseVersion}"/></p>
            </g:if>
        </g:if>

        <g:if test="${instance.dataGeneralizations}">
            <h2><g:message code="public.sdr.content.label05" /></h2>
            <ch:formattedText>${instance.dataGeneralizations}</ch:formattedText>
        </g:if>

        <g:if test="${instance.informationWithheld}">
            <h2><g:message code="public.sdr.content.label06" /></h2>
            <ch:formattedText>${instance.informationWithheld}</ch:formattedText>
        </g:if>

        <g:if test="${instance.downloadLimit}">
            <h2><g:message code="public.sdr.content.label07" /></h2>

            <p><g:message code="public.sdr.content.des02" /> ${instance.downloadLimit} <g:message code="public.sdr.content.des03" />.</p>
        </g:if>

        <div id="pagesContributed"></div>

        <g:if test="${instance.resourceType == 'website' && (instance.lastChecked || instance.dataCurrency)}">
            <h2><g:message code="public.sdr.content.label08" /></h2>

            <p>
                %{--<ch:lastChecked date="${instance.lastChecked}"/>--}%
                %{--<ch:dataCurrency date="${instance.dataCurrency}"/></p>--}%
        </g:if>

        <g:if test="${!grailsApplication.config.disableLoggerLinks.toBoolean() && (instance.resourceType == 'website' || instance.resourceType == 'records')}">
            <div id='usage-stats'>
                <h2><g:message code="public.sdr.usagestats.labe" /></h2>

                <div id='usage'>
                    <p><g:message code="public.usage.des" />...</p>
                </div>
                <g:if test="${instance.resourceType == 'website'}">
                    <div id="usage-visualization" style="width: 600px; height: 200px;"></div>
                </g:if>
            </div>
        </g:if>

        <g:if test="${instance.resourceType == 'records'}">
            <h2><g:message code="public.sdr.content.label09" /></h2>

            <div>
                <p><span
                        id="numBiocacheRecords"><g:message code="public.sdr.content.des04" /></span> <g:message code="public.sdr.content.des05" />.
                %{--<ch:lastChecked date="${instance.lastChecked}"/>--}%
                %{--<ch:dataCurrency date="${instance.dataCurrency}"/>--}%
                </p>
                %{--<ch:recordsLink--}%
                        %{--collection="${instance}"><g:message code="public.sdr.content.link01" /> ${instance.name} <g:message code="public.sdr.content.link02" />.</ch:recordsLink>--}%
                <ch:downloadPublicArchive uid="${instance.uid}" available="${instance.publicArchiveAvailable}"/>
            </div>
        </g:if>
        <g:if test="${instance.resourceType == 'records'}">
            <div id="recordsBreakdown" class="section vertical-charts">
                <g:if test="${!grailsApplication.config.disableOverviewMap.toBoolean()}">
                    <h3><g:message code="public.sdr.content.label10" /></h3>
                    <ch:recordsMapDirect uid="${instance.uid}"/>
                </g:if>
                <div id="tree" class="well"></div>
                <div id="charts"></div>
            </div>
        </g:if>
        <ch:lastUpdated date="${instance.lastUpdated}"/>
    </div><!--close column-one-->
    <div class="span4">
        <g:if test="${instance.imageRef && instance.imageRef.file}">
            <div class="section">
                <img alt="${instance.imageRef?.file}"
                     src="${resource(absolute: "true", dir: "data/dataResource/", file: instance.imageRef?.file)}"/>
                <ch:formattedText
                        pClass="caption">${instance.imageRef?.caption}</ch:formattedText>
                <ch:valueOrOtherwise value="${instance.imageRef?.attribution}"><p
                        class="caption">${instance.imageRef?.attribution}</p></ch:valueOrOtherwise>
                <ch:valueOrOtherwise value="${instance.imageRef?.copyright}"><p
                        class="caption">${instance.imageRef?.copyright}</p></ch:valueOrOtherwise>
            </div>
        </g:if>

        <div id="dataAccessWrapper" style="display:none;">
            <g:render template="dataAccess" model="[instance:instance, facet:'data_resource_uid']"/>
        </div>

        <!-- use parent location if the collection is blank -->
        <g:set var="address" value="${instance.address}"/>
        <g:if test="${address == null || collectoryService.isAddressEmpty(address)}">
            <g:if test="${instance.dataProvider}">
                <g:set var="address" value="${instance.dataProvider?.address}"/>
            </g:if>
        </g:if>

        <g:if test="${address != null && !(collectoryService.isAddressEmpty(address))}">
            <div class="section">
                <h3><g:message code="public.location" /></h3>

                <g:if test="${!(collectoryService.isAddressEmpty(address))}">
                    <p>
                        <ch:valueOrOtherwise value="${address?.street}">${address?.street}<br/></ch:valueOrOtherwise>
                        <ch:valueOrOtherwise value="${address?.city}">${address?.city}<br/></ch:valueOrOtherwise>
                        <ch:valueOrOtherwise value="${address?.state}">${address?.state}</ch:valueOrOtherwise>
                        <ch:valueOrOtherwise value="${address?.postcode}">${address?.postcode}<br/></ch:valueOrOtherwise>
                        <ch:valueOrOtherwise value="${address?.country}">${address?.country}<br/></ch:valueOrOtherwise>
                    </p>
                </g:if>

                <g:if test="${instance.email}"><ch:emailLink>${instance.email}</ch:emailLink><br/></g:if>
                <ch:ifNotBlank value='${instance.phone}'/>
            </div>
        </g:if>

    <!-- contacts -->
        <g:set var="contacts" value="${collectoryService.getPublicContactsPrimaryFirst(instance.contacts)}"/>
        <g:render template="contacts" model="${[contacts:contacts]}"/>

    <!-- web site -->
        <g:if test="${instance.resourceType == 'species-list'}">
            <div class="section">
                <h3><g:message code="public.sdr.content.label12" /></h3>
                <div class="webSite">
                    <a class='external_icon' target="_blank"
                       href="${grailsApplication.config.speciesListToolUrl}${instance.uid}"><g:message code="public.sdr.content.link03" /></a>
                </div>
            </div>
        </g:if>
        <g:elseif test="${instance.websiteUrl}">
            <div class="section">
                <h3><g:message code="public.website" /></h3>
                <div class="webSite">
                    <a class='external_icon' target="_blank"
                       href="${instance.websiteUrl}"><g:message code="public.sdr.content.link04" /></a>
                </div>
            </div>
        </g:elseif>

    <!-- attribution -->
        %{--<g:set var='attribs' value='${instance.getAttributionList()}'/>--}%
        %{--<g:if test="${attribs.size() > 0}">--}%
            %{--<div class="section" id="infoSourceList">--}%
                %{--<h4><g:message code="public.sdr.infosourcelist.title" /></h4>--}%
                %{--<ul>--}%
                    %{--<g:each var="a" in="${attribs}">--}%
                        %{--<li><a href="${a.url}" class="external" target="_blank">${a.name}</a></li>--}%
                    %{--</g:each>--}%
                %{--</ul>--}%
            %{--</div>--}%
        %{--</g:if>--}%
    </div>
</div>
</div>
<script type="text/javascript" src="https://www.google.com/jsapi"></script>
<script type="text/javascript">google.load('visualization', '1.0', {'packages':['corechart']});</script>
<script type="text/javascript">
     var CHARTS_CONFIG = {
         biocacheServicesUrl: "${grailsApplication.config.biocacheServicesUrl}",
         biocacheWebappUrl: "${grailsApplication.config.grails.serverURL}",
         collectionsUrl: "${grailsApplication.config.grails.serverURL}"
     };

    // configure the charts
      var facetChartOptions = {
          /* base url of the collectory */
          collectionsUrl: CHARTS_CONFIG.collectionsUrl,
          /* base url of the biocache ws*/
          biocacheServicesUrl: CHARTS_CONFIG.biocacheServicesUrl,
          /* base url of the biocache webapp*/
          biocacheWebappUrl: CHARTS_CONFIG.biocacheWebappUrl,
          /* a uid or list of uids to chart - either this or query must be present */
          instanceUid: "${instance.uid}",
          /* if using datahub */
          <g:if test="${grailsApplication.config.hub.queryContext}">
          isDataHub:true,
          dataHubFq:"fq=${grailsApplication.config.hub.queryContext}",
          </g:if>
          /* the list of charts to be drawn (these are specified in the one call because a single request can get the data for all of them) */
          charts: ['country','state','species_group','assertions','type_status',
              'biogeographic_region','state_conservation','occurrence_year']
      }
      var taxonomyChartOptions = {
          /* base url of the collectory */
          collectionsUrl: CHARTS_CONFIG.collectionsUrl,
          /* base url of the biocache ws*/
          biocacheServicesUrl: CHARTS_CONFIG.biocacheServicesUrl,
          /* base url of the biocache webapp*/
          biocacheWebappUrl: CHARTS_CONFIG.biocacheWebappUrl,
          /* support drill down into chart - default is false */
          drillDown: true,
          /* a uid or list of uids to chart - either this or query must be present */
          instanceUid: "${instance.uid}",
          //query: "notomys",
          //rank: "kingdom",
          /* if using datahub */
          <g:if test="${grailsApplication.config.hub.queryContext}">
          isDataHub:true,
          dataHubFq:"fq=${grailsApplication.config.hub.queryContext}",
          </g:if>
          /* threshold value to use for automagic rank selection - defaults to 55 */
          threshold: 25
      }
      var taxonomyTreeOptions = {
          /* base url of the collectory */
          collectionsUrl: CHARTS_CONFIG.collectionsUrl,
          /* base url of the biocache ws*/
          biocacheServicesUrl: CHARTS_CONFIG.biocacheServicesUrl,
          /* base url of the biocache webapp*/
          biocacheWebappUrl: CHARTS_CONFIG.biocacheWebappUrl,
          /* the id of the div to create the charts in - defaults is 'charts' */
          targetDivId: "tree",
          /* if using datahub */
          <g:if test="${grailsApplication.config.hub.queryContext}">
          isDataHub:true,
          dataHubFq:"fq=${grailsApplication.config.hub.queryContext}",
          </g:if>
          /* a uid or list of uids to chart - either this or query must be present */
          instanceUid: "${instance.uid}"
      }

      /************************************************************\
    *
    \************************************************************/
    var queryString = '';
    var decadeUrl = '';

    $('img#mapLegend').each(function(i, n) {
      // if legend doesn't load, then it must be a point map
      $(this).error(function() {
        $(this).attr('src',"${resource(dir: 'images/map', file: 'single-occurrences.png')}");
      });
    });
    /************************************************************\
    *
    \************************************************************/
    function onLoadCallback() {
      // stats
      if(loadLoggerStats){
          if (${instance.resourceType == 'website'}) {
              loadDownloadStats("${grailsApplication.config.loggerURL}", "${instance.uid}","${instance.name}", "2000");
          } else if (${instance.resourceType == 'records'}) {
              loadDownloadStats("${grailsApplication.config.loggerURL}", "${instance.uid}","${instance.name}", "1002");
          }
      }

      // species pages
      $.ajax({
          url: bieUrl + "/search.json?q=*&fq=uid:${instance.uid}",
          dataType: 'jsonp',
          success: function(data) {
              var pages = data.searchResults.totalRecords;
              if (pages) {
                  var $contrib = $('#pagesContributed');
                  $contrib.append($('<h2>Contribution to the Atlas</h2><p>This resource has contributed to <strong>' +
                      pages + '</strong> pages of taxa. ' +
                      '<a href="' + bieUrl + '/search?q=*&fq=uid:' + "${instance.uid}" + '">View a list</a></p>'));
              }
          }
      });
debugger;
      // records
      if (${instance.resourceType == 'records'}) {
          // summary biocache data
          $.ajax({
            url: CHARTS_CONFIG.biocacheServicesUrl + "/occurrences/search.json?pageSize=0&q=data_resource_uid:${instance.uid}",
            dataType: 'jsonp',
            timeout: 30000,
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
                    facetChartOptions.response = data;
                    // draw the charts
                    drawFacetCharts(data, facetChartOptions);
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

          // tree
          initTaxonTree(taxonomyTreeOptions);
      }
    }
    /************************************************************\
    *
    \************************************************************/
    google.load("visualization", "1.0", { packages:["corechart"] });
    google.setOnLoadCallback(onLoadCallback);

</script>
</body>
</html>
