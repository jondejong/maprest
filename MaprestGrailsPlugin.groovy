import grails.converters.JSON

import org.codehaus.groovy.grails.plugins.web.api.ControllersApi
import org.codehaus.groovy.grails.web.metaclass.RenderDynamicMethod

import com.jondejong.maprest.MaprestFormat

class MaprestGrailsPlugin {

    def version = "0.1.0"
    def grailsVersion = "2.0 > *"

    def loadAfter = ['controllers']
    def observe = ["controllers"]

    def title = "Maprest Plugin"
    def author = "Jon DeJong"
    def authorEmail = ""
    def description = 'Allows customization of REST responses using property maps.'
    def documentation = "https://github.com/jondejong/maprest"

    def license = "APACHE"
    def issueManagement = [ system: "Github", url: "https://github.com/jondejong/maprest/issues" ]
    def scm = [url: "https://github.com/jondejong/maprest"]

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
        def rootAttributes = parseRootAttributes(map)
        renderDynamicMethod.invoke(this, "render", [contentType: "application/xml"], {
            "${root}" (rootAttributes){
                mapAsXml(delegate, map)
            }
        })
    }

    def parseRootAttributes(Map map) {
        def attributes = [:]
        def attributeKeys = []

        map.keySet().each { String key ->
            if (key.startsWith('@')) {
                String attribute = key[1..key.size() - 1]
                attributes.put(attribute, map.get(key))
                attributeKeys.add(key)
            }
        }

        attributeKeys.each {map.remove(it)}

        return attributes
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
}
