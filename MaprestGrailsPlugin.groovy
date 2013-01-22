import com.jondejong.maprest.MaprestFormat
import grails.converters.JSON
import org.codehaus.groovy.grails.web.metaclass.RenderDynamicMethod

class MaprestGrailsPlugin {

    // the plugin version
    def version = "0.0.1"
    // the version or versions of Grails the plugin is designed for
    def grailsVersion = "2.1 > *"
    // the other plugins this plugin depends on
    def dependsOn = [:]
    // resources that are excluded from plugin packaging
    def pluginExcludes = [
            "grails-app/views/error.gsp"
    ]

    def loadAfter = ['controllers']

    // TODO Fill in these fields
    def title = "Maprest Plugin" // Headline display name of the plugin
    def author = "Jon DeJong"
    def authorEmail = ""
    def description = '''\
Brief summary/description of the plugin.
'''

    // URL to the plugin's documentation
    def documentation = "http://grails.org/plugin/maprest"

    // Extra (optional) plugin metadata

    // License: one of 'APACHE', 'GPL2', 'GPL3'
//    def license = "APACHE"

    // Details of company behind the plugin (if there is one)
//    def organization = [ name: "My Company", url: "http://www.my-company.com/" ]

    // Any additional developers beyond the author specified above.
//    def developers = [ [ name: "Joe Bloggs", email: "joe@bloggs.net" ]]

    // Location of the plugin's issue tracker.
//    def issueManagement = [ system: "JIRA", url: "http://jira.grails.org/browse/GPMYPLUGIN" ]

    // Online location of the plugin's browseable source code.
//    def scm = [ url: "http://svn.codehaus.org/grails-plugins/" ]

    def doWithDynamicMethods = { ctx ->
        for (controllerClass in application.controllerClasses) {
            controllerClass.metaClass.getXmlFormat = { MaprestFormat.Format.XML }
            controllerClass.metaClass.getJsonFormat = { MaprestFormat.Format.JSON }
            controllerClass.metaClass.getClientAcceptedFormat = { MaprestFormat.Format.CLIENT_ACCEPTED }
            controllerClass.metaClass.renderMaprest = { Object o, String root, MaprestFormat.Format f ->

                def map = o.transformToMap()

                if (f == MaprestFormat.Format.XML) {
                    renderXml(map, root)
                } else if (f == MaprestFormat.Format.JSON) {
                    render map as JSON
                } else {
                    withFormat {
                        json {
                            render map as JSON
                        }
                        xml {
                            renderXml(map, root)
                        }
                    }
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

    protected mapAsXml(builder, map) {
        for (entry in map) {
            if (entry.value instanceof Map) {
                def attributes = [:]

                def thisEntry = (Map)entry.value

                thisEntry.keySet().each {String key->
                    if(key.startsWith('@')) {
                        String attribute = key[1..key.size() - 1]
                        attributes.put(attribute, thisEntry.get(key))
                        thisEntry.remove(key)
                    }
                }

                builder."${entry.key}" (attributes){
                    mapAsXml(builder, entry.value)
                }

            } else if (entry.value instanceof Collection) {
                builder."${entry.key}" {
                    for (childMap in entry.value) {
                        def elementName
                        if (childMap?.containsKey('elementName')) {
                            elementName = childMap.elementName
                            childMap.remove 'elementName'
                        } else {
                            elementName = "${entry.key[0..-2]}"
                        }
                        "${elementName}" {
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

    def onChange = { event ->
        // TODO Implement code that is executed when any artefact that this plugin is
        // watching is modified and reloaded. The event contains: event.source,
        // event.application, event.manager, event.ctx, and event.plugin.
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
