package au.org.ala.collectory

class CamelCaseTagLib {
    static defaultEncodeAs = [taglib:'html']
    //static encodeAsForTags = [tagName: [taglib:'html'], otherTagName: [taglib:'none']]

    def formatService

    def formatFieldName = { attr, body ->
        def fieldName = attr['value']
        out << formatService.formatFieldName(fieldName)
    }

    def prettyCamel = { attr, body ->
        def value = attr['value']
        out << formatService.prettyCamel(value)
    }
}
