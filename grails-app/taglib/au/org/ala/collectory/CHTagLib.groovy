package au.org.ala.collectory

import grails.converters.JSON
import org.apache.commons.lang.StringUtils
import org.ocpsoft.prettytime.PrettyTime
import org.springframework.web.servlet.support.RequestContextUtils

import java.text.SimpleDateFormat

class CHTagLib {

    static namespace = "ch"
    def grailsApplication

    /**
     * Outputs value/body if one is not blank otherwise outputs the otherwise value if present.
     * Can be used as:
     *  <valueOrOtherwise>bod</> => outputs bod if bod is groovy true
     *  <valueOrOtherwise value='val'></> => outputs val if val is groovy true
     *  <valueOrOtherwise value='val'>bod</> => outputs bod if val is groovy true
     * In any of the above:
     *  <valueOrOtherwise value='val' otherwise='not defined'>bod</> => outputs not defined if nothing is groovy true
     *
     * @attrs value the value to test (and output if true and there is no body)
     * @attrs otherwise the text to output if tests all fail
     * @body the value to test (if value is not provided) and output if test is true
     */
    def valueOrOtherwise = { attrs, body ->
        // determine what text to show
        def value = attrs.value
        def bod = body()
        def text = ''
        if (body() && body() != "") {
            text = body()
        } else if (attrs.value) {
            text = attrs.value
        }
        // determine whether to show it
        if (attrs.value) {
            out << text
        } else if (!attrs.containsKey('value') && body()) { // only test body if value was not present (cf was null, blank or false)
            out << text
        } else if (attrs.otherwise) {
            out << attrs.otherwise
        }
    }

    /**
     * Writes a para with date last updated.
     *
     * @param date
     */
    def lastUpdated = {attrs ->
        if (attrs.date) {
            out << "<p class='lastUpdated'>" + g.message(code: "metadata.last.updated", args: []) + " ${attrs.date}</p>"
        }
    }

    /**
     * Formats free text so:
     *  line feeds are honoured
     *  and urls are linked
     *  and bold (+xyz+)and italic (_xyz_) are rendered
     *  and lists are supported using wiki markup
     *
     * @param attrs.noLink suppresses links
     * @param attrs.noList suppresses lists
     * @param body the text to format
     * @param pClass the class to use for paras
     * @param body pass the text in as a attr rather than as body (won't be encoded)
     */
    def formattedText = {attrs, body ->
        def text = attrs.body ?: body().toString()
        if (text) {

            if (text.indexOf('<') >= 0 && text.indexOf('>') >= 0) {
                // assume this is already marked up as html
                out << text
            }
            else {

                // italic
                def italicMarkup = /(\b)_([^\r\n_]*)_(\b)/  // word boundary _ thing to be italised _ word boundary
                text = text.replaceAll(italicMarkup) {match, s1, s2, s3 ->
                    s1 + '<em>' + s2 + '</em>' + s3         // word boundary <em> thing to be italised </em> word boundary
                }

                // in-line links
                if (!attrs.noLink) {
                    def urlMatch = /[^\[](http:\S*)\b/   // word boundary + http: + non-whitespace + word boundary
                    text = text.replaceAll(urlMatch) {s1, s2 ->
                        if (s2.indexOf('ala.org.au') > 0)
                            " <a href='${s2}'>${s2}</a>"
                        else
                            " <a rel='nofollow' class='external' target='_blank' href='${s2}'>${s2}</a>"
                    }
                }

                // wiki-like links
                if (!attrs.noLink) {
                    def urlMatch = /\[(http:\S*)\b ([^\]]*)\]/   // [http: + text to next word boundary + space + all text ubtil next ]
                    text = text.replaceAll(urlMatch) {s1, s2, s3 ->
                        if (s2.indexOf('ala.org.au') > 0)
                            "<a href='${s2}'>${s3}</a>"
                        else
                            "<a rel='nofollow' class='external' target='_blank' href='${s2}'>${s3}</a>"
                    }
                }

                // bold
                def regex = /\+([^\r\n+]*)\+/
                text = text.replaceAll(regex) {match, group -> '<b>' + group + '</b>'}

                // lists
                if (!attrs.noList) {
                    def lines = text.tokenize("\r\n")
                    def inList = false
                    def newText = ""
                    // for each line
                    lines.each {
                        if (it[0] == '*') {
                            // replace list markup
                            def item = "<li>" + it.substring(1,it.length()) + "</li>"
                            if (inList) {
                                it = item
                            } else {
                                inList = true
                                it = "<ul class='simple'>" + item
                            }
                        } else {
                            if (it) { // skip blank content
                                def para = (attrs.pClass) ? "<p class='${attrs.pClass}'>" : "<p>"
                                it = para + it + "</p>"
                            }
                            if (inList) {
                                inList = false
                                it = "</ul>" + it
                            }
                        }
                        newText += it
                    }
                    if (inList) { newText = newText + "</ul>"}
                    text = newText
                }

                out << text
            }
        }
    }

    /**
     * Outputs value with some decoration if value is not blank.
     * Handles up to 3 values but stops when any value is blank.
     *
     * TODO: this is a pretty shit tag - should be refactored
     *
     * @attrs value the value to test and output
     * @attrs tagName the tag to enclose the value in - defaults to p if not specified
     * @attrs prefix output before value
     * @attrs postfix output after value
     * @attrs join separator between multiple values
     */
    def ifNotBlank = {attrs ->
        def tagName = (attrs.tagName == null) ? 'p' : attrs.tagName
        def startTag = tagName ? "<${tagName}>" : ""
        def endTag = tagName ? "</${tagName}>" : ""
        if (attrs.value) {
            out << startTag
            out << (attrs.prefix) ? attrs.prefix : ""
            out << attrs.value.encodeAsHTML()
            out << (attrs.postfix) ? attrs.postfix : ""

            // allow other content
            if (attrs.value2) {
                out << (attrs.join) ? attrs.join : ""
                out << (attrs.prefix) ? attrs.prefix : ""
                out << attrs.value2
                out << (attrs.postfix) ? attrs.postfix : ""

                if (attrs.value3) {
                    out << (attrs.join) ? attrs.join : ""
                    out << (attrs.prefix) ? attrs.prefix : ""
                    out << attrs.value3
                    out << (attrs.postfix) ? attrs.postfix : ""
                }
            }

            out << endTag
        }
    }

    /**
     * Adds site context to page title.
     */
    def pageTitle = { attrs, body ->
        out << "${body()} | Murray-Darling Basin Authority"
    }

    /**
     * Show lsid as a link
     *
     * @attrs guid only shown if it is an lsid
     * @attrs target for the link
     */
    def guid = { attrs ->
        if(attrs.guid){
            def lsid = attrs.guid
            def target = attrs.target
            if (target) {
                target = " target='" + target + "'"
            } else {
                target = ""
            }
            if (lsid =~ 'lsid') {  // contains
                def authority = lsid[9..lsid.indexOf(':',10)-1]
                out << "<a${target} rel='nofollow' class='external' href='http://${authority}/${lsid.encodeAsHTML()}'>${lsid.encodeAsHTML()}</a>"
            } else {
                out << attrs.guid
            }
        }
    }

    /**
     * A little bit of email scrambling for dumb scrappers.
     *
     * Uses email attribute as email if present else uses the body.
     * If no attribute and the body is not an email address then nothing is shown.
     *
     * @attrs email the address to decorate
     * @body the text to use as the link text
     */
    def emailLink = { attrs, body ->
        def strEncodedAtSign = "(SPAM_MAIL@ALA.ORG.AU)"
        String email = attrs.email
        if (!email)
            email = body().toString()
        int index = email.indexOf('@')
        if (index > 0) {
            email = email.replaceAll("@", strEncodedAtSign)
            out << "<a href='#' class='link under' onclick=\"return sendEmail('${email}')\">${body()}</a>"
        }
    }

    /**
     * Text appropriate to the type of contribution.
     *
     * @attr resourceType the typeof resource
     * @attr status the state of progress in integration the resource
     * @attr tag the tag to enclose the text
     */
    def dataResourceContribution = { attrs ->
        def startTag = attrs.tag ? "<${attrs.tag}>" : ""
        def endTag = attrs.tag ? "</${attrs.tag}>" : ""
        if (attrs.resourceType == 'website' && attrs.status == 'dataAvailable') {
            out << startTag + "This website provides content for Atlas species pages." + endTag
        }
        else if (attrs.resourceType == 'website' && attrs.status == 'linksAvailable') {
            out << startTag + "Links to this website appear on appropriate Atlas species pages." + endTag
        }
        else if (attrs.resourceType == 'document' && attrs.status == 'dataAvailable') {
            out << startTag + "Documents from this source provide content for Atlas species pages." + endTag
        }
        else if (attrs.resourceType == 'document' && attrs.status == 'linksAvailable') {
            out << startTag + "Links to this document appear on appropriate Atlas species pages." + endTag
        }
    }


    def lastChecked = { attrs ->
        if (attrs.date) {
            out << """<span id="updated">This resource was last checked for updated data on
                <b>${new SimpleDateFormat("dd MMM yyyy").format(attrs.date)}</b>.</span>"""
        }
    }

    def dataCurrency = { attrs ->
        if (attrs.date) {
            out << """<span id="currency">The most recent data was published on
                <b>${new SimpleDateFormat("dd MMM yyyy").format(attrs.date)}</b>.</span>"""
        }
    }

    /**
     * Builds the link to bio-cache records for the passed entity.
     *
     * @param entity the entity to search for
     * @param collection (same as entity - for backwards compat)
     * @param onlyIf a boolean switch
     * @body the body of the link
     */
    def recordsLink = {attrs, body ->
        def pg = attrs.entity ? attrs.entity : attrs.collection
        if (pg) {
            if (attrs.containsKey('onlyIf') && !attrs.onlyIf) {
                out << body()
            }
            else {
                out << "<a class='recordsLink' href='"
                out << buildRecordsUrl(pg.uid)
                out << "'>" << body() << "</a>"
            }
        }
    }

    def downloadPublicArchive = { attrs ->
        if (attrs.uid && attrs.available) {
            def url = grailsApplication.config.resource.publicArchive.url.template.replaceAll('@UID@',attrs.uid)
            out << "<p><a class='downloadLink' href='${url}'>Download darwin core archive of all records</a></p>"
        }
    }

    def createNewRecordsAlertsLink = { attrs ->
        createAlertsLink(attrs, '/webservice/createBiocacheNewRecordsAlert')
    }

    def createNewAnnotationsAlertsLink = { attrs ->
        createAlertsLink(attrs, '/webservice/createBiocacheNewAnnotationsAlert')
    }

    def createAlertsLink(attrs, urlPath) {

        if(!grailsApplication.config.disableAlertLinks.toBoolean()){
            def link = grailsApplication.config.alertUrl + urlPath
            link += '?webserviceQuery=/occurrences/search?q=' + attrs.query
            link += '&uiQuery=/occurrences/search?q=' + attrs.query
            link += '&queryDisplayName=' + attrs.displayName
            link += '&baseUrlForWS=' + grailsApplication.config.biocacheServicesUrl
            link += '&baseUrlForUI=' + grailsApplication.config.biocacheUiURL
            link += '&resourceName=' + grailsApplication.config.alertResourceName
            out << "<a href=\"" + link +"\" class='btn' alt='"+attrs.altText+"'><i class='icon icon-bell'></i> "+ attrs.linkText + "</a>"
        }
    }


    /**
     * List of content types.
     *
     * @attr types json list of string
     */
    def contentTypes = { attrs ->
        if (attrs.types) {
            def list = JSON.parse(attrs.types as String).collect {it.toString()}
            out << '<p>Includes: ' + list.join(', ') + '.</p>'
        }
    }

    def getInstitutionLink = {attrs->
        if(attrs.institutions){
            attrs.institutions.each{ institution ->
                switch (institution.uid[0..1]) {
                    case 'in':
                        String href = createLink(controller: 'collectory', action: 'showInstitution') + "/${institution.uid}"
                        out << "<a href='${href}'>${institution.name}</a>";
                        break
                }
            }
        }
    }

    private String buildRecordsUrl(uid) {
        // handle descendant institutions
        def uidStr = uid;
        if (uid.size() > 1 && uid[0..1] == 'in') {
            uidStr = Institution._get(uid)?.descendantUids()?.join(",") ?: uid
        }
        def queryStr
        // need to handle multiple uids differently
        if (uidStr.indexOf(',')) {
            def uids = uidStr.tokenize(',')
            queryStr = uids.collect({ fieldNameForSearch(it) + ":" + it}).join(' OR ')
        }
        else {
            queryStr = fieldNameForSearch(uidStr) + ":" + uidStr
        }
        return  grailsApplication.config.biocacheUiURL + "/occurrence/search?q=" + queryStr
    }

    /**
     * Tailors the descriptive noun for an institution based on it's type.
     */
    def institutionType = {attrs ->
        if (attrs.inst?.institutionType == "governmentDepartment") {
            out << 'department'
        }
        else {
            out << 'institution'
        }
    }

    /**
     * Show map of records based on UID
     *
     * - content is loaded directly by constructing the image url
     *
     * @attr uid of the instance
     */
    def recordsMapDirect = { attrs ->
        if (attrs.uid) {
            def urlBase = grailsApplication.config.biocacheServicesUrl + "/density/"
            def query = "?q=" + fieldNameForSearch(attrs.uid) + ":" + attrs.uid
            out <<
                    "<div class='recordsMap'>" +
                    " <img id='recordsMap' class='no-radius' src='${urlBase}map${query}' width='340' />" +
                    " <img id='mapLegend' src='${urlBase}legend${query}' width='128' />" +
                    "</div>" +
                    "<div class='learnMaps'><span class='asterisk-container'><a href='${grailsApplication.config.ala.baseURL}/faq/species-data/errors-in-maps/'>Learn more about Atlas maps</a>&nbsp;</span></div>"
        }
        else {
            out <<
                    "<div class='recordsMap'>" +
                    " <img id='recordsMap' class='no-radius' src='${resource(dir:'images/map',file:'mapping-data-not-available.png')}' width='340' />" +
                    " <img id='mapLegend' src='${resource(dir:'images/ala', file:'legend-not-available.png')}' width='128' />" +
                    "</div>" +
                    "<div class='learnMaps'><span class='asterisk-container'><a href='${grailsApplication.config.ala.baseURL}/faq/species-data/errors-in-maps/'>Learn more about Atlas maps</a>&nbsp;</span></div>"
        }
    }

    private String fieldNameForSearch(uid) {
        switch (uid[0..1]) {
            case 'co': return 'collection_uid'; break
            case 'in': return 'institution_uid'; break
            case 'dr': return 'data_resource_uid'; break
            case 'dp': return 'data_provider_uid'; break
            case 'dh': return 'data_hub_uid'; break
            default: return ""
        }
    }

    def display = {attrs, body ->

        def date = attrs.remove('date')
        def showTime = Boolean.valueOf(attrs.remove('showTime'))
        def capitalize = Boolean.valueOf(attrs.remove('capitalize'))

        if ('org.joda.time.DateTime'.equals(date?.class?.name)) {
            date = date.toDate()
        }

        if (!date) return

        def prettyTime = new PrettyTime(RequestContextUtils.getLocale(request))


        String result = prettyTime.format(date).trim()
        if (capitalize) result = StringUtils.capitalize(result)
        if (showTime) {
            def format = attrs.remove('format') ?:
                    message(code: 'default.date.format', default: 'hh:mm:ss a')
            result += ', ' + date.format(format)
        }

        out << result
    }
}
