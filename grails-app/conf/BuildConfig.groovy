grails.project.class.dir = "target/classes"
grails.project.test.class.dir = "target/test-classes"
grails.project.test.reports.dir = "target/test-reports"

grails.project.fork = [
    // configure settings for compilation JVM, note that if you alter the Groovy version forked compilation is required
    //  compile: [maxMemory: 256, minMemory: 64, debug: false, maxPerm: 256, daemon:true],

    // configure settings for the test-app JVM, uses the daemon by default
    test: [maxMemory: 768, minMemory: 64, debug: false, maxPerm: 256, daemon:true],
    // configure settings for the run-app JVM
    run: [maxMemory: 768, minMemory: 64, debug: false, maxPerm: 256, forkReserve:false],
    // configure settings for the run-war JVM
    war: [maxMemory: 768, minMemory: 64, debug: false, maxPerm: 256, forkReserve:false],
    // configure settings for the Console UI JVM
    console: [maxMemory: 768, minMemory: 64, debug: false, maxPerm: 256]
]

grails.project.dependency.resolver = "maven" // or ivy
grails.project.dependency.resolution = {
    // inherit Grails' default dependencies
    inherits("global") {
        // uncomment to disable ehcache
        // excludes 'ehcache'
    }
    log "debug" // log level of Ivy resolver, either 'error', 'warn', 'info', 'debug' or 'verbose'
    repositories {
        mavenLocal()
        mavenRepo ("http://nexus.ala.org.au/content/groups/public/") {
            updatePolicy 'always'
        }
    }
    dependencies {
        // specify dependencies here under either 'build', 'compile', 'runtime', 'test' or 'provided' scopes eg.
        compile "commons-httpclient:commons-httpclient:3.1"
    }

    plugins {
        build ":tomcat:7.0.54"
        build(":release:3.0.1",
              ":rest-client-builder:2.1.1") {
            export = false
        }
        compile ":cache-ehcache:1.0.0"
        compile ":cache:1.1.1"
        compile ":ala-ws-plugin:1.0"
        runtime ":jquery:1.8.3"

        runtime ':resources:1.2.14'
        if (Environment.current == Environment.PRODUCTION) {
            runtime ":zipped-resources:1.0.1"
            runtime ":cached-resources:1.1"
            compile ":cache-headers:1.1.7"
            runtime ":yui-minify-resources:0.1.5"
        }
    }
}
