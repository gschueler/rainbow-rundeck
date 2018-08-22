import org.gradle.api.*
import org.gradle.api.tasks.*
import org.yaml.snakeyaml.Yaml

/**
 * Defines a config and task to help build plugin.yaml files
 */
class RDZipPluginPlugin implements Plugin<Project> {
    void apply(Project project) {
        project.extensions.create("rdZipPlugin", RDZipPluginPluginExtension)
        project.task("validatePluginYaml", type: RDZipPluginValidateYamlTask)
    }
}

class RDZipPluginValidateYamlTask extends DefaultTask {

    public static final List<String> VALID_PLUGIN_PROVIDERTYPES = ['ui', 'script']
    public static final List<String> REQUIRED_PROVIDER_FIELDS = ['name', 'service', 'plugin-type']
    public static final List<String> REQUIRED_PLUGIN_YAML_FIELDS = ['name', 'rundeckPluginVersion', 'providers']

    File getPluginYamlFile() {
        new File(zipContentsDir, project.rdZipPlugin.pluginYaml)
    }

    File getZipContentsDir() {
        project.file("${project.buildDir}/${project.rdZipPlugin.buildContents}")
    }

    def invalid(msg, src) {
        throw new Exception("$msg: $src")

    }

    def requireField(map, field, src) {
        if (!map[field]) {
            throw new Exception("Required field `$field` not found in: $src")
        }
    }

    @TaskAction
    def validate() {
        if (!pluginYamlFile || !pluginYamlFile.exists()) {
            throw new Exception("Plugin yaml file does not exist: $pluginYamlFile")
        }
        //validate Yaml
        def yaml = new Yaml()
        pluginYamlFile.withInputStream {

            def pluginYaml = yaml.loadAs(it, Map)
//            println("loaded: $pluginYaml")

            //validate required fields
            REQUIRED_PLUGIN_YAML_FIELDS.each { field ->
                requireField(pluginYaml, field, pluginYamlFile)
            }
            if (pluginYaml.providers.size() < 1) {
                throw new Exception("One or more entries in `providers` is required: $pluginYamlFile")
            }
            pluginYaml.providers.eachWithIndex { provider, n ->
                REQUIRED_PROVIDER_FIELDS.each { field ->
                    requireField(provider, field,  "$pluginYamlFile: providers[$n]")
                }
                if (!(provider['plugin-type'] in VALID_PLUGIN_PROVIDERTYPES)) {
                    invalid("plugin-type not valid: `${provider['plugin-type']}`", "$pluginYamlFile: providers[$n]")
                }
            }
        }
    }
}

class RDZipPluginPluginExtension {
    def String srcBase = 'src/main/rdplugin'
    def String pluginYaml = 'plugin.yaml'
    def String buildContents = 'zip-contents'
}