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
 * Created by Temi on 07/2016.
 */
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