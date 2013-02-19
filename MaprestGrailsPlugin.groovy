import com.jondejong.maprest.MaprestFormat
import com.jondejong.maprest.MaprestRenderer

class MaprestGrailsPlugin {
    def version = "0.1.1"
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
        MaprestRenderer renderer = new MaprestRenderer()
        for (controllerClass in application.controllerClasses) {
            controllerClass.metaClass.getXmlFormat = { MaprestFormat.XML }
            controllerClass.metaClass.getJsonFormat = { MaprestFormat.JSON }
            controllerClass.metaClass.renderMaprest = { Object o, MaprestFormat f, String root = null ->
                renderer.renderMaprest o, f, root
            }
        }
    }
}
