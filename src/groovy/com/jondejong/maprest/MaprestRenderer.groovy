package com.jondejong.maprest

import grails.converters.JSON

import org.codehaus.groovy.grails.plugins.web.api.ControllersApi
import org.codehaus.groovy.grails.web.metaclass.RenderDynamicMethod

class MaprestRenderer {

    protected RenderDynamicMethod renderDynamicMethod = new RenderDynamicMethod()
    protected ControllersApi api = new ControllersApi()

    def renderMaprest(Object o, MaprestFormat f, String root = null) {

        Map map = o.transformToMap()

        switch (f) {

            case MaprestFormat.XML:
                if (!root) {
                    root = 'root'
                }
                renderXml(map, root)
                break

            case MaprestFormat.JSON:
                if (root) {
                    map = ["${root}": map]
                }
                renderJson map
        }
    }

    def renderXml(Map map, root) {
        def rootAttributes = parseRootAttributes(map)
        renderDynamicMethod.invoke this, "render", [contentType: "application/xml"], {
            "$root" (rootAttributes) {
                mapAsXml(delegate, map)
            }
        }
    }

    protected Map parseRootAttributes(Map map) {
        def attributes = [:]
        def attributeKeys = []

        map.each { String key, value ->
            if (key.startsWith('@')) {
                String attribute = key[1..key.size() - 1]
                attributes[attribute] = value
                attributeKeys << key
            }
        }

        attributeKeys.each { map.remove(it) }

        return attributes
    }

    def renderJson(map) {
        def scrubbedMap = scrubObjectForJson(map)
        api.render(this, scrubbedMap as JSON)
    }

    def scrubObjectForJson(map) {
        if (map instanceof Collection) {
            return map.collect { scrubObjectForJson(it) }
        }

        def newMap = [:]
        map.each { key, val ->
            if (key.toString().startsWith('@')) {
                key = key[1..key.size() - 1]
            }
            if (val instanceof Map || val instanceof Collection) {
                val = scrubObjectForJson(val)
            }
            newMap[key] = val
        }
        return newMap
    }

    protected mapAsXml(builder, Map map) {
        for (Map.Entry entry in map) {
            if (entry.value instanceof Map) {
                def attributes = [:]
                def attributeKeys = []

                def element = entry.value

                element.keySet().each { String key ->
                    if (key.startsWith('@')) {
                        String attribute = key[1..key.size() - 1]
                        attributes[attribute] = element.get(key)
                        attributeKeys.add(key)
                    }
                }
                attributeKeys.each {
                    element.remove(it)
                }
                builder."${entry.key}"(attributes) {
                    mapAsXml(builder, entry.value)
                }
            }
            else if (entry.value instanceof Collection) {
                builder."${entry.key}" {
                    for (childMap in entry.value) {
                        def attributes = [:]
                        def attributeKeys = []
                        def elementName
                        if (childMap?.containsKey('elementName')) {
                            elementName = childMap.elementName
                            childMap.remove 'elementName'
                        }
                        else {
                            elementName = "${entry.key[0..-2]}"
                        }
                        childMap.keySet().each { key ->
                            if (key.startsWith('@')) {
                                String attribute = key[1..key.size() - 1]
                                attributes[attribute] = childMap.get(key)
                                attributeKeys.add(key)
                            }
                        }

                        attributeKeys.each { childMap.remove(it) }

                        "${elementName}"(attributes) {
                            mapAsXml(builder, childMap)
                        }
                    }
                }
            }
            else {
                builder."${entry.key}"(entry.value)
            }
        }
    }
}
