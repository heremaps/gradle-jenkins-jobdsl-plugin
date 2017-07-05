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
import javaposse.jobdsl.dsl.ExtensibleContext
import javaposse.jobdsl.dsl.Folder
import javaposse.jobdsl.dsl.Item
import javaposse.jobdsl.dsl.JobConfigurationNotFoundException
import javaposse.jobdsl.dsl.NameNotProvidedException
import javaposse.jobdsl.dsl.UserContent
import org.apache.http.HttpRequest
import org.apache.http.HttpRequestInterceptor
import org.apache.http.HttpStatus
import org.apache.http.protocol.HttpContext
import org.custommonkey.xmlunit.XMLUnit

/**
 * Implementation of {@link javaposse.jobdsl.dsl.JobManagement} that performs all actions using the REST API of Jenkins.
 * Only methods required by the plugin are implemented, all others throw an {@link UnsupportedOperationException}.
 */
@SuppressWarnings('MethodCount') // High method count required because of super class.
class RestJobManagement extends AbstractJobManagement implements DeferredJobManagement {

    public static final String STATUS_COULD_NOT_CREATE = 'COULD NOT CREATE'
    public static final String STATUS_COULD_NOT_UPDATE = 'COULD NOT UPDATE'
    public static final String STATUS_CREATED = 'CREATED'
    public static final String STATUS_IGNORE = 'IGNORE'
    public static final String STATUS_UP_TO_DATE = 'UP-TO-DATE'
    public static final String STATUS_UPDATED = 'UPDATED'
    public static final String STATUS_WOULD_BE_CREATED = 'WOULD BE CREATED'
    public static final String STATUS_WOULD_BE_UPDATED = 'WOULD BE UPDATED'

    private static final HEADER_ACCEPT_XML = [Accept: 'application/xml']

    static class ItemRequest {
        Item item
        boolean ignoreExisting
    }

    static class ViewRequest {
        String viewName
        String config
        boolean ignoreExisting
    }

    static String getItemType(Item item) {
        return item.getClass().simpleName
    }

    private static boolean isXmlDifferent(String control, String test) {
        XMLUnit.setIgnoreComments(true)
        XMLUnit.setIgnoreWhitespace(true)
        return !XMLUnit.compareXML(control, test).similar()
    }

    boolean disablePluginChecks
    boolean dryRun
    ItemFilter filter
    String jenkinsUrl
    VersionNumber jenkinsVersion
    RESTClient restClient
    List<Map> plugins
    Set<String> deprecatedPlugins
    Set<String> missingPlugins
    Set<String> outdatedPlugins
    Map<String, Integer> statusCounter
    List<ItemRequest> itemRequests
    List<ViewRequest> viewRequests

    @SuppressWarnings('ParameterCount')
    RestJobManagement(ItemFilter filter, boolean disablePluginChecks, boolean dryRun, String jenkinsUrl,
                      String jenkinsUser, String jenkinsApiToken) {
        super(System.out)

        this.disablePluginChecks = disablePluginChecks
        this.dryRun = dryRun
        this.filter = filter
        this.jenkinsUrl = jenkinsUrl

        if (!this.jenkinsUrl.endsWith('/')) {
            this.jenkinsUrl += '/'
        }

        deprecatedPlugins = [] as SortedSet
        missingPlugins = [] as SortedSet
        outdatedPlugins = [] as SortedSet
        statusCounter = [:]

        itemRequests = []
        viewRequests = []

        restClient = new RESTClient(jenkinsUrl)
        restClient.handler.failure = { it }

        if (jenkinsUser != null && jenkinsApiToken != null) {
            restClient.client.addRequestInterceptor([
                    process: { HttpRequest request, HttpContext context ->
                        request.addHeader(
                                'Authorization',
                                'Basic ' + "${jenkinsUser}:${jenkinsApiToken}".toString().bytes.encodeBase64()
                                        .toString()
                        )
                    }] as HttpRequestInterceptor)
        }

        HttpResponseDecorator resp = restClient.get(path: 'crumbIssuer/api/xml')
        if (resp.status == HttpStatus.SC_OK) {
            restClient.headers[resp.data.crumbRequestField] = resp.data.crumb
        }

        jenkinsVersion = requestJenkinsVersion()
        println "Remote Jenkins is version ${jenkinsVersion}"

        if (!disablePluginChecks) {
            requestPlugins()
        }
    }

    @Override
    String getConfig(String jobName) throws JobConfigurationNotFoundException {
        throw new UnsupportedOperationException()
    }

    @Override
    boolean createOrUpdateConfig(Item item, boolean ignoreExisting) throws NameNotProvidedException {
        itemRequests += new ItemRequest(item: item, ignoreExisting: ignoreExisting)
        return true
    }

    @Override
    void createOrUpdateView(String viewName, String config, boolean ignoreExisting) throws NameNotProvidedException,
            ConfigurationMissingException {
        viewRequests += new ViewRequest(viewName: viewName, config: config, ignoreExisting: ignoreExisting)
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
            logDeprecationWarning("Support for ${pluginShortName} versions older than ${minimumVersion}")
            deprecatedPlugins.add(pluginShortName)
        }
    }

    @Override
    void requirePlugin(String pluginShortName, boolean failIfMissing) {
        if (disablePluginChecks) {
            return
        }
        def plugin = findPlugin(pluginShortName)
        if (plugin == null) {
            def message = "Required plugin ${pluginShortName} not installed."
            println message
            missingPlugins.add(pluginShortName)
            if (failIfMissing) {
                throw new DslScriptException(message)
            }
        }
    }

    @Override
    void requireMinimumPluginVersion(String pluginShortName, String version, boolean failIfMissing) {
        if (disablePluginChecks) {
            return
        } else if (!isMinimumPluginVersionInstalled(pluginShortName, version)) {
            def plugin = findPlugin(pluginShortName)
            def message

            if (plugin == null) {
                message = "Version ${version} or later of plugin ${pluginShortName} needs to be installed."
                missingPlugins.add(pluginShortName)
            } else {
                message = "Plugin ${pluginShortName} needs to be updated to version ${version} or later."
                outdatedPlugins.add(pluginShortName)
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

    VersionNumber getPluginVersion(String pluginShortName) {
        def plugin = findPlugin(pluginShortName)
        return plugin == null ? null : new VersionNumber(plugin.version)
    }

    @Override
    boolean isMinimumPluginVersionInstalled(String pluginShortName, String version) {
        if (disablePluginChecks) {
            return true
        }
        def actualVersionNumber = getPluginVersion(pluginShortName)
        if (actualVersionNumber == null) {
            return false
        }
        def minimumVersionNumber = new VersionNumber(version)
        return !actualVersionNumber.isOlderThan(minimumVersionNumber)
    }

    @Override
    boolean isMinimumCoreVersion(String version) {
        return !jenkinsVersion.isOlderThan(new VersionNumber(version))
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
    Node callExtension(String name, Item item, Class<? extends ExtensibleContext> contextType, Object... args) throws
            Throwable {
        return null
    }

    VersionNumber requestJenkinsVersion() {
        HttpResponseDecorator response = restClient.get(
                path: 'api/json',
                contentType: ContentType.JSON
        ) as HttpResponseDecorator

        def jenkinsHeader = response.getFirstHeader('X-Jenkins')

        if (!jenkinsHeader) {
            throw new DslScriptException("Could not get version from Jenkins server '${jenkinsUrl}': " +
                    "${response.statusLine}")
        }

        return new VersionNumber(jenkinsHeader.value)
    }

    void requestPlugins() {
        HttpResponseDecorator response = restClient.get(
                path: 'pluginManager/api/json',
                query: [depth: 2],
                contentType: ContentType.JSON
        ) as HttpResponseDecorator

        if (response.status != HttpStatus.SC_OK) {
            throw new DslScriptException("Could not load list of plugins from Jenkins server '${jenkinsUrl}': " +
                    "${response.statusLine}")
        }

        plugins = response.data.plugins
    }

    Map findPlugin(String pluginShortName) {
        return plugins.find { it.shortName == pluginShortName }
    }

    @Override
    @SuppressWarnings('Instanceof') // No other way to check if an item is a Folder.
    void applyChanges() {
        // Create folders first, to make sure they exist before trying to create items in them
        itemRequests.findAll { it.item instanceof Folder }.each { itemRequest ->
            performCreateOrUpdateConfig(itemRequest.item, itemRequest.ignoreExisting)
        }

        // Create all non-folder items
        itemRequests.findAll { !(it.item instanceof Folder) }.each { itemRequest ->
            performCreateOrUpdateConfig(itemRequest.item, itemRequest.ignoreExisting)
        }

        // Create all views
        viewRequests.each { viewRequest ->
            performCreateOrUpdateView(viewRequest.viewName, viewRequest.config, viewRequest.ignoreExisting)
        }
    }

    boolean performCreateOrUpdateConfig(Item item, boolean ignoreExisting) throws NameNotProvidedException {
        if (filter.matches(item.name)) {
            String existingXml = requestExistingItemXml(item)
            if (!existingXml) {
                return createItem(item)
            } else if (!ignoreExisting) {
                if (isXmlDifferent(existingXml, item.xml)) {
                    return updateItem(item)
                } else {
                    logItemStatus(item, STATUS_UP_TO_DATE)
                }
            }
        } else {
            logItemStatus(item, STATUS_IGNORE)
        }
        return true
    }

    void performCreateOrUpdateView(String viewName, String config, boolean ignoreExisting) throws
            NameNotProvidedException, ConfigurationMissingException {
        if (filter.matches(viewName)) {
            String existingXml = requestExistingViewXml(viewName)
            if (!existingXml) {
                createView(viewName, config)
            } else if (!ignoreExisting) {
                if (isXmlDifferent(existingXml, config)) {
                    updateView(viewName, config)
                } else {
                    logViewStatus(viewName, STATUS_UP_TO_DATE)
                }
            }
        } else {
            logViewStatus(viewName, STATUS_IGNORE)
        }
    }

    String requestExistingItemXml(Item item) {
        HttpResponseDecorator response = restClient.get(
                path: FolderPathHelper.itemConfigPath(item.name),
                contentType: ContentType.TEXT,
                headers: HEADER_ACCEPT_XML
        ) as HttpResponseDecorator

        if (response?.data) {
            return "${response.data}".toString()
        } else {
            return null
        }
    }

    String requestExistingViewXml(String viewName) {
        HttpResponseDecorator response = restClient.get(
                path: FolderPathHelper.viewConfigPath(viewName),
                contentType: ContentType.TEXT,
                headers: HEADER_ACCEPT_XML
        ) as HttpResponseDecorator

        if (response?.data) {
            return "${response.data}".toString()
        } else {
            return null
        }
    }

    boolean createItem(Item item) {
        if (dryRun) {
            logItemStatus(item, STATUS_WOULD_BE_CREATED)
            return true
        }

        HttpResponseDecorator response = restClient.post(
                path: FolderPathHelper.createItemPath(item.name),
                query: [name: FolderPathHelper.removeFoldersFromName(item.name)],
                body: item.xml,
                requestContentType: 'application/xml'
        ) as HttpResponseDecorator

        if (response.status == HttpStatus.SC_OK) {
            logItemStatus(item, STATUS_CREATED)
            return true
        } else {
            logItemStatus(item, STATUS_COULD_NOT_CREATE, response.dump())
            if (response.status == HttpStatus.SC_NOT_FOUND) {
                println 'If the item is contained in a folder probably the folder does not exist'
            }
            return false
        }
    }

    boolean createView(String viewName, String config) {
        if (dryRun) {
            logViewStatus(viewName, STATUS_WOULD_BE_CREATED)
            return true
        }

        HttpResponseDecorator response = restClient.post(
                path: FolderPathHelper.createViewPath(viewName),
                query: [name: FolderPathHelper.removeFoldersFromName(viewName)],
                body: config,
                requestContentType: 'application/xml'
        ) as HttpResponseDecorator

        if (response.status == HttpStatus.SC_OK) {
            logViewStatus(viewName, STATUS_CREATED)
            return true
        } else {
            logViewStatus(viewName, STATUS_COULD_NOT_CREATE, response.dump())
            return false
        }
    }

    boolean updateItem(Item item) {
        if (dryRun) {
            logItemStatus(item, STATUS_WOULD_BE_UPDATED)
            return true
        }

        HttpResponseDecorator response = restClient.post(
                path: FolderPathHelper.itemConfigPath(item.name),
                body: item.xml,
                requestContentType: 'application/xml'
        ) as HttpResponseDecorator

        if (response.status == HttpStatus.SC_OK) {
            logItemStatus(item, STATUS_UPDATED)
            return true
        } else {
            logItemStatus(item, STATUS_COULD_NOT_UPDATE, response.dump())
            return false
        }
    }

    boolean updateView(String viewName, String config) {
        if (dryRun) {
            logViewStatus(viewName, STATUS_WOULD_BE_UPDATED)
            return true
        }

        HttpResponseDecorator response = restClient.post(
                path: FolderPathHelper.viewConfigPath(viewName),
                body: config,
                requestContentType: 'application/xml'
        ) as HttpResponseDecorator

        if (response.status == HttpStatus.SC_OK) {
            logViewStatus(viewName, STATUS_UPDATED)
            return true
        } else {
            logViewStatus(viewName, STATUS_COULD_NOT_UPDATE, response.dump())
            return false
        }
    }

    private void logItemStatus(Item item, String status, String message = null) {
        countStatus(status)
        println "${item.name} (${getItemType(item)}): ${status}${message != null ? " - ${message}" : ''}"
    }

    private void logViewStatus(String viewName, String status, String message = null) {
        countStatus(status)
        println "${viewName} (View): ${status}${message != null ? " - ${message}" : ''}"
    }

    private void countStatus(String status) {
        statusCounter[status] = (statusCounter[status] ?: 0) + 1
    }

}
