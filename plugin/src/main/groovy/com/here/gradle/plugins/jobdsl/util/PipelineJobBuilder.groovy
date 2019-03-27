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

package com.here.gradle.plugins.jobdsl.util

import javaposse.jobdsl.dsl.DslFactory

/**
 * Implementation of {@link JobBuilder} that can be used with a {@link PipelineBuilder}. This class adds support for
 * {@link PipelineBuilder#baseFolders} by overriding the required methods.
 */
class PipelineJobBuilder extends JobBuilder {

    PipelineBuilder pipelineBuilder

    PipelineJobBuilder(DslFactory dslFactory) {
        super(dslFactory)
    }

    List<String> allFolders() {
        return pipelineBuilder?.baseFolders ? [*pipelineBuilder.baseFolders, *folders] : [*folders]
    }

    @Override
    String fullJobName() {
        return (allFolders() + name).join('/')
    }

}
