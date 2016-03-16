package com.here.gradle.plugins.jobdsl

import groovyx.net.http.ContentType
import groovyx.net.http.HttpResponseDecorator
import groovyx.net.http.RESTClient
import hudson.util.VersionNumber
import javaposse.jobdsl.dsl.AbstractJobManagement
import javaposse.jobdsl.dsl.ConfigFile
import javaposse.jobdsl.dsl.ConfigFileType
import javaposse.jobdsl.dsl.ConfigurationMissingException
import javaposse.jobdsl.dsl.DslScriptException
import javaposse.jobdsl.dsl.Item
import javaposse.jobdsl.dsl.JobConfigurationNotFoundException
import javaposse.jobdsl.dsl.NameNotProvidedException
import javaposse.jobdsl.dsl.UserContent
import javaposse.jobdsl.dsl.helpers.ExtensibleContext
import org.apache.http.HttpException
import org.apache.http.HttpRequest
import org.apache.http.HttpRequestInterceptor
import org.apache.http.protocol.HttpContext
import org.custommonkey.xmlunit.XMLUnit

class RestJobManagement extends AbstractJobManagement {

    ItemFilter filter
    String jenkinsUrl
    RESTClient restClient
    List<Map> plugins

    RestJobManagement(ItemFilter filter, String jenkinsUrl, String jenkinsUser, String jenkinsPassword) {
        super(System.out)

        this.filter = filter
        this.jenkinsUrl = jenkinsUrl

        restClient = new RESTClient(jenkinsUrl)
        restClient.handler.failure = { it }

        if (jenkinsUser != null && jenkinsPassword != null) {
            restClient.client.addRequestInterceptor([
                    process: { HttpRequest request, HttpContext context ->
                        request.addHeader(
                                'Authorization',
                                'Basic ' + "${jenkinsUser}:${jenkinsPassword}".toString().bytes.encodeBase64().toString()
                        )
                    }] as HttpRequestInterceptor)
        }

        requestPlugins()
    }

    void process(HttpRequest request, HttpContext context) throws HttpException, IOException {
        request.addHeader(
                'Authorization',
                'Basic ' + "${jenkinsUser}:${jenkinsPassword}".toString().bytes.encodeBase64().toString()
        )
    }


    @Override
    String getConfig(String jobName) throws JobConfigurationNotFoundException {
        throw new UnsupportedOperationException()
    }

    @Override
    boolean createOrUpdateConfig(Item item, boolean ignoreExisting) throws NameNotProvidedException {
        if (filter.matches(item.name)) {
            String existingXml = requestExistingItemXml(item)
            if (!existingXml) {
                return createItem(item)
            } else if (!ignoreExisting) {
                if (isXmlDifferent(existingXml, item.xml)) {
                    return updateItem(item)
                } else {
                    println "${item.name} (${getItemType(item)}): UP-TO-DATE"
                }
            }
        } else {
            println "${item.name} (${getItemType(item)}): IGNORE (name does not match filter expression)"
            return true
        }
    }

    @Override
    void createOrUpdateView(String viewName, String config, boolean ignoreExisting) throws NameNotProvidedException, ConfigurationMissingException {
        if (filter.matches(viewName)) {
            String existingXml = requestExistingViewXml(viewName)
            if (!existingXml) {
                createView(viewName, config)
            } else if (!ignoreExisting) {
                if (isXmlDifferent(existingXml, config)) {
                    updateView(viewName, config)
                } else {
                    println "${viewName} (View): UP-TO-DATE"
                }
            }
        } else {
            println "${viewName} (View): IGNORE (name does not match filter expression)"
        }
    }

    @Override
    String createOrUpdateConfigFile(ConfigFile configFile, boolean ignoreExisting) {
        throw new UnsupportedOperationException()
    }

    @Override
    void createOrUpdateUserContent(UserContent userContent, boolean ignoreExisting) {
        throw new UnsupportedOperationException()
    }

    @Override
    void renameJobMatching(String previousNames, String destination) throws IOException {
        throw new UnsupportedOperationException()
    }

    @Override
    void queueJob(String jobName) throws NameNotProvidedException {
        throw new UnsupportedOperationException()
    }

    @Override
    InputStream streamFileInWorkspace(String filePath) throws IOException, InterruptedException {
        throw new UnsupportedOperationException()
    }

    @Override
    String readFileInWorkspace(String filePath) throws IOException, InterruptedException {
        throw new UnsupportedOperationException()
    }

    @Override
    String readFileInWorkspace(String jobName, String filePath) throws IOException, InterruptedException {
        throw new UnsupportedOperationException()
    }

    @Override
    Map<String, String> getParameters() {
        return [:]
    }

    @Override
    void logPluginDeprecationWarning(String pluginShortName, String minimumVersion) {
        if (!isMinimumPluginVersionInstalled(pluginShortName, minimumVersion)) {
            logDeprecationWarning("Support for ${pluginShortName} versions older than ${minimumVersion}");
        }
    }

    @Override
    void requirePlugin(String pluginShortName, boolean failIfMissing) {
        def plugin = findPlugin(pluginShortName)
        if (plugin == null) {
            def message = "Required plugin ${pluginShortName} not installed."
            println message
            if (failIfMissing) {
                throw new DslScriptException(message)
            }
        }
    }

    @Override
    void requireMinimumPluginVersion(String pluginShortName, String version, boolean failIfMissing) {
        if (!isMinimumPluginVersionInstalled(pluginShortName, version)) {
            def plugin = findPlugin(pluginShortName)
            def message

            if (plugin == null) {
                message = "Version ${version} or later of plugin ${pluginShortName} needs to be installed."
            } else {
                message = "Plugin ${pluginShortName} needs to be updated to version ${version} or later."
            }
            println message

            if (failIfMissing) {
                throw new DslScriptException(message)
            }
        }
    }

    @Override
    void requireMinimumCoreVersion(String version) {
        throw new UnsupportedOperationException()
    }

    @Override
    VersionNumber getPluginVersion(String pluginShortName) {
        def plugin = findPlugin(pluginShortName)
        return plugin == null ?: new VersionNumber(plugin.version)
    }

    @Override
    boolean isMinimumPluginVersionInstalled(String pluginShortName, String version) {
        def actualVersionNumber = getPluginVersion(pluginShortName)
        if (actualVersionNumber == null) {
            return false
        }
        def minimumVersionNumber = new VersionNumber(version)
        return !actualVersionNumber.isOlderThan(minimumVersionNumber)
    }

    @Override
    VersionNumber getJenkinsVersion() {
        throw new UnsupportedOperationException()
    }

    @Override
    Integer getVSphereCloudHash(String name) {
        throw new UnsupportedOperationException()
    }

    @Override
    String getConfigFileId(ConfigFileType type, String name) {
        throw new UnsupportedOperationException()
    }

    @Override
    Set<String> getPermissions(String authorizationMatrixPropertyClassName) {
        throw new UnsupportedOperationException()
    }

    @Override
    Node callExtension(String name, Item item, Class<? extends ExtensibleContext> contextType, Object... args) throws Throwable {
        return null
    }

    void requestPlugins() {
        HttpResponseDecorator response = restClient.get(
                path: '/pluginManager/api/json',
                query: [depth: 2],
                contentType: ContentType.JSON
        )

        if (response.status != 200) {
            throw new DslScriptException("Could not load list of plugins from Jenkins server '${jenkinsUrl}': ${response.statusLine}")
        }

        plugins = response.data.plugins
    }

    Map findPlugin(String pluginShortName) {
        return plugins.find { it.shortName == pluginShortName }
    }

    String requestExistingItemXml(Item item) {
        HttpResponseDecorator response = restClient.get(
                path: getItemConfigPath(item),
                contentType: ContentType.TEXT,
                headers: [Accept: 'application/xml']
        )

        if (response?.data) {
            println "${item.name} (${getItemType(item)}): EXISTS"
            return "${response.data}".toString()
        } else {
            println "${item.name} (${getItemType(item)}): NEW"
            return null
        }
    }

    String requestExistingViewXml(String viewName) {
        HttpResponseDecorator response = restClient.get(
                path: "view/${viewName}/config.xml",
                contentType: ContentType.TEXT,
                headers: [Accept: 'application/xml']
        )

        if (response?.data) {
            println "${viewName} (View): EXISTS"
            return "${response.data}".toString()
        } else {
            println "${viewName} (View): NEW"
            return null
        }
    }

    boolean createItem(Item item) {
        HttpResponseDecorator response = restClient.post(
                path: getItemCreatePath(item),
                query: [name: getItemNameWithoutFolders(item)],
                body: item.xml,
                requestContentType: 'application/xml'
        )

        if (response.status == 200) {
            println "${item.name} (${getItemType(item)}): CREATED"
            return true
        } else {
            println "${item.name} (${getItemType(item)}): COULD NOT CREATE - ${response.dump()}"
            if (response.status == 404) {
                println "If the item is contained in a folder probably the folder does not exist"
            }
            return false
        }
    }

    boolean createView(String viewName, String config) {
        HttpResponseDecorator response = restClient.post(
                path: "createView",
                query: [name: viewName],
                body: config,
                requestContentType: 'application/xml'
        )

        if (response.status == 200) {
            println "${viewName} (View): CREATED"
            return true
        } else {
            println "${viewName} (View): COULD NOT CREATE - ${response.dump()}"
            return false
        }
    }

    boolean updateItem(Item item) {
        HttpResponseDecorator response = restClient.post(
                path: getItemConfigPath(item),
                body: item.xml,
                requestContentType: 'application/xml'
        )

        if (response.status == 200) {
            println "${item.name} (${getItemType(item)}): UPDATED"
            return true
        } else {
            println "${item.name} (${getItemType(item)}): COULD NOT UPDATE - ${response.dump()}"
            return false
        }
    }

    boolean updateView(String viewName, String config) {
        HttpResponseDecorator response = restClient.post(
                path: "view/${viewName}/config.xml",
                body: config,
                requestContentType: 'application/xml'
        )

        if (response.status == 200) {
            println "${viewName} (View): UPDATED"
            return true
        } else {
            println "${viewName} (View): COULD NOT UPDATE - ${response.dump()}"
            return false
        }
    }

    String getItemConfigPath(Item item) {
        return "job/${item.name.replaceAll('/', '/job/')}/config.xml"
    }

    String getItemCreatePath(Item item) {
        def names = item.name.split('/')
        return (names.length > 1 ? "job/${names[0..-2].join('/job/')}/" : '') + 'createItem'
    }

    String getItemNameWithoutFolders(Item item) {
        int lastIndex = item.name.lastIndexOf('/')
        return item.name.substring(lastIndex + 1)
    }

    String getItemType(Item item) {
        return item.getClass().simpleName
    }

    private boolean isXmlDifferent(String control, String test) {
        XMLUnit.setIgnoreComments(true)
        XMLUnit.setIgnoreWhitespace(true)
        return !XMLUnit.compareXML(control, test).similar()
    }

}
