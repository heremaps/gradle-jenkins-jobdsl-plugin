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

package com.here.demo.jobdsl

import com.here.gradle.plugins.jobdsl.util.PipelineJobBuilder
import javaposse.jobdsl.dsl.DslFactory

/**
 * Demonstrates how to use {@link PipelineJobBuilder} to properties to simplify the configuration of job templates.
 */
class DemoPipelineJobBuilder extends PipelineJobBuilder {

    String shellCommand
    Integer timeout

    DemoPipelineJobBuilder(DslFactory dslFactory) {
        super(dslFactory)

        addDsl {
            steps {
                shell(shellCommand)
            }
            wrappers {
                timeout {
                    absolute(timeout)
                }
            }
        }
    }

}
