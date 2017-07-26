package au.org.ala.collectory

class UrlMappings {

    static mappings = {
        "/$controller/$action?/$id?(.$format)?" {
            constraints {
                // apply constraints here
            }
        }

        "/jenkins/consoleMessage/$jobName/$id/$start"(controller: 'jenkins', action: 'console')
        "/"(view: "/index")
        "500"(view: '/error')
    }
}
