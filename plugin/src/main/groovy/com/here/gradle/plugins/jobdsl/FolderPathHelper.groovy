package com.here.gradle.plugins.jobdsl

class FolderPathHelper {

    /**
     * Generate the path to the item by injecting 'job/' between the parent folder names.
     *
     * @param name
     * @return
     */
    static String itemNameToPath(String name) {
        return "job/${name.replaceAll('/', '/job/')}"
    }

    /**
     * Generate the path to the view by injecting 'job/' between the parent folder names and 'view/' before the view
     * name.
     *
     * @param name
     * @return
     */
    static String viewNameToPath(String name) {
        return name.split('/').reverse().inject('view') { acc, value ->
            return acc.contains('/') ? "job/${value}/${acc}" : "${acc}/${value}"
        }
    }

    /**
     * Generate the path to the config.xml for an item.
     *
     * @param name
     * @return
     */
    static String itemConfigPath(String name) {
       return itemNameToPath(name) + '/config.xml'
    }

    /**
     * Generate the path to the config.xml for a view.
     *
     * @param name
     * @return
     */
    static String viewConfigPath(String name) {
        return viewNameToPath(name) + '/config.xml'
    }

    /**
     * Remove all folders from the name. This removes everything before and including the last '/' in the name.
     *
     * @param name
     * @return
     */
    static String removeFoldersFromName(String name) {
        int lastIndex = name.lastIndexOf('/')
        return name.drop(lastIndex + 1)
    }

    /**
     * Get the base path of the REST api for the provided name. For non-folder items this is the empty string, for
     * folder items this is the path of the folder.
     *
     * @param name
     * @return
     */
    static String restApiPathForName(String name) {
        def names = name.split('/')
        return (names.length > 1 ? "job/${names[0..-2].join('/job/')}/" : '')
    }

    /**
     * Get the path to the provided REST method for the name. For non-folder items this is only the name of the REST
     * method, for folder items this is the path to the folder plus the method name.
     *
     * @param name
     * @param restMethod
     * @return
     */
    static String restMethodPathForName(String name, String restMethod) {
        return restApiPathForName(name) + restMethod
    }

    /**
     * Get the path to the create REST method for an item name.
     *
     * @param name
     * @return
     */
    static String createItemPath(String name) {
        return restMethodPathForName(name, 'createItem')
    }

    /**
     * Get the path to the create REST method for a view name.
     *
     * @param name
     * @return
     */
    static String createViewPath(String name) {
        return restMethodPathForName(name, 'createView')
    }

}
