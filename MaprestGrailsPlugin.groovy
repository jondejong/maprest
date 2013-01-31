import com.jondejong.maprest.MaprestFormat
import grails.converters.JSON
import org.codehaus.groovy.grails.plugins.web.api.ControllersApi
import org.codehaus.groovy.grails.web.metaclass.RenderDynamicMethod

class MaprestGrailsPlugin {

    // the plugin version
    def version = "0.1.0"
    // the version or versions of Grails the plugin is designed for
    def grailsVersion = "2.2 > *"
    // the other plugins this plugin depends on
    def dependsOn = [:]
    // resources that are excluded from plugin packaging
    def pluginExcludes = [
            "grails-app/views/error.gsp"
    ]

    def loadAfter = ['controllers']
    def observe = ["controllers"]

    // TODO Fill in these fields
    def title = "Maprest Plugin" // Headline display name of the plugin
    def author = "Jon DeJong"
    def authorEmail = ""
    def description = '''\
Allows customization of REST responses using property maps.
'''

    // URL to the plugin's documentation
    def documentation = "https://github.com/jondejong/maprest"

    // Extra (optional) plugin metadata

    // License: one of 'APACHE', 'GPL2', 'GPL3'
    def license = "APACHE"

    // Details of company behind the plugin (if there is one)
//    def organization = [ name: "My Company", url: "http://www.my-company.com/" ]

    // Any additional developers beyond the author specified above.
//    def developers = [ [ name: "Joe Bloggs", email: "joe@bloggs.net" ]]

    // Location of the plugin's issue tracker.
//    def issueManagement = [ system: "JIRA", url: "http://jira.grails.org/browse/GPMYPLUGIN" ]

    // Online location of the plugin's browseable source code.
    def scm = [url: "https://github.com/jondejong/maprest"]

//    def watchedResources = 'file:./grails-app/controllers'

    def doWithDynamicMethods = { ctx ->
        addMethodsToControllers(application)
    }

    def onChange = { event ->
        addMethodsToControllers(application)
    }

    def addMethodsToControllers(application) {
        for (controllerClass in application.controllerClasses) {
            controllerClass.metaClass.getXmlFormat = { MaprestFormat.Format.XML }
            controllerClass.metaClass.getJsonFormat = { MaprestFormat.Format.JSON }
            controllerClass.metaClass.renderMaprest = { Object o, MaprestFormat.Format f, String root = null ->

                def map = o.transformToMap()

                if (f == MaprestFormat.Format.XML) {
                    if (!root) {
                        root = 'root'
                    }
                    renderXml(map, root)
                } else if (f == MaprestFormat.Format.JSON) {
                    if (root) {
                        map = ["${root}": map]
                    }
                    renderJson map
                }
            }
        }
    }

    def renderXml(map, root) {
        def renderDynamicMethod = new RenderDynamicMethod()

        renderDynamicMethod.invoke(this, "render", [contentType: "application/xml"], {
            "${root}" {
                mapAsXml(delegate, map)
            }
        })
    }

    def renderJson(map) {
        def scrubbedMap = scrubObjectForJson(map)
        def renderDynamicMethod = new RenderDynamicMethod()

        def api = new ControllersApi()
        api.render(this, scrubbedMap as JSON)
    }


    def scrubObjectForJson(map) {
        def newMap = [:]
        if (map instanceof Collection) {
            def newCollection = []
            map.each {
                newCollection.add scrubObjectForJson(it)
            }
            return newCollection
        } else {
            map.keySet().each {
                def val = map.get it
                def key = it
                if (it.toString().startsWith('@')) {
                    key = key[1..key.size() - 1]
                }
                if (val instanceof Map || val instanceof Collection) {
                    val = scrubObjectForJson(val)
                }
                newMap.put(key, val)
            }
        }
        return newMap
    }

    protected mapAsXml(builder, map) {
        for (entry in map) {
            if (entry.value instanceof Map) {
                def attributes = [:]
                def attributeKeys = []

                def element = (Map) entry.value

                element.keySet().each { String key ->
                    if (key.startsWith('@')) {
                        String attribute = key[1..key.size() - 1]
                        attributes.put(attribute, element.get(key))
                        attributeKeys.add(key)
                    }
                }
                attributeKeys.each {
                    element.remove(it)
                }
                builder."${entry.key}"(attributes) {
                    mapAsXml(builder, entry.value)
                }

            } else if (entry.value instanceof Collection) {
                builder."${entry.key}" {
                    for (childMap in entry.value) {
                        def attributes = [:]
                        def attributeKeys = []
                        def elementName
                        if (childMap?.containsKey('elementName')) {
                            elementName = childMap.elementName
                            childMap.remove 'elementName'
                        } else {
                            elementName = "${entry.key[0..-2]}"
                        }
                        childMap.keySet().each { key ->
                            if (key.startsWith('@')) {
                                String attribute = key[1..key.size() - 1]
                                attributes.put(attribute, childMap.get(key))
                                attributeKeys.add(key)
                            }
                        }

                        attributeKeys.each { childMap.remove(it) }

                        "${elementName}"(attributes) {
                            mapAsXml(builder, childMap)
                        }
                    }
                }
            } else {
                builder."${entry.key}"(entry.value)
            }
        }
    }

    def doWithApplicationContext = { applicationContext ->
        // TODO Implement post initialization spring config (optional)
    }

    def onConfigChange = { event ->
        // TODO Implement code that is executed when the project configuration changes.
        // The event is the same as for 'onChange'.
    }

    def onShutdown = { event ->
        // TODO Implement code that is executed when the application shuts down (optional)
    }

    def doWithWebDescriptor = { xml ->
        // TODO Implement additions to web.xml (optional), this event occurs before
    }

    def doWithSpring = {
        // TODO Implement runtime spring config (optional)
    }
}
