package au.org.ala.collectory

class MessagesController {

    def messageSources

    static defaultAction = "i18n"

    /**
     * Export raw i18n message properties as TEXT for use by JavaScript i18n library
     *
     * @param id - messageSource file name
     * @return
     */
    def i18n(String id) {
        Locale locale = org.springframework.web.servlet.support.RequestContextUtils.getLocale(request)

        if (id && !id.startsWith("messages_")) {

            Map props = messageSources.listMessageCodes(locale?:request.locale)
            response.setHeader("Content-type", "text/plain; charset=UTF-8")
            def messages = props.collect{ "${it.key}=${it.value}" }

            render ( text: messages.sort().join("\n") )
        } else {
            render (text: '')
        }
    }
}