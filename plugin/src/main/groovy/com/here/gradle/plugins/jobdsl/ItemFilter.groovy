/*
 * Copyright (C) 2016-2019 HERE Europe B.V.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 * License-Filename: LICENSE
 */

package com.here.gradle.plugins.jobdsl

/**
 * Utility class to check if item names match a regular expression.
 */
class ItemFilter {

    private final String pattern

    ItemFilter(String pattern) {
        this.pattern = pattern
    }

    /**
     * Check if the itemName matches the filter pattern. If the pattern is null or empty this always returns true.
     *
     * @param itemName Name of the Jenkins job or view.
     * @return True if filter is null or empty or itemName matches filter pattern.
     */
    boolean matches(String itemName) {
        pattern ? itemName ==~ pattern : true
    }

}
