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

import com.here.demo.jobdsl.DemoPipelineJobBuilder
import com.here.gradle.plugins.jobdsl.util.PipelineBuilder

folder('Demo/pipeline')
folder('Demo/pipeline/master')
folder('Demo/pipeline/staging')

// Configure pipeline builder
def pipelineBuilder = new PipelineBuilder()
pipelineBuilder.with {
    baseFolders(['Demo', 'pipeline', 'master'])
    defaultConfiguration([timeout: 10])
    commonDsl {
        wrappers {
            timestamps()
        }
    }
}

// Add jobs to pipeline
def firstJob = new DemoPipelineJobBuilder(this)
firstJob.with {
    name = 'First job'
    shellCommand = 'ls'
    freeStyleJob {
        publishers {
            archiveArtifacts('*')
        }
    }
}

def secondJob = new DemoPipelineJobBuilder(this)
secondJob.with {
    name = 'Second job'
    shellCommand = 'ls -a'
    freeStyleJob {
        publishers {
            archiveArtifacts('*')
        }
    }
}

def thirdJob = new DemoPipelineJobBuilder(this)
thirdJob.with {
    name = 'Third job'
    shellCommand = 'ls -al'

    // Apply a value to timeout, this will not be overridden by the default configuration of the pipeline builder.
    timeout = 25

    freeStyleJob {
        publishers {
            archiveArtifacts('*')
        }
    }
}

pipelineBuilder.with {
    addJob(firstJob)
    addJob(secondJob)
    addJob(thirdJob)

    // Build pipeline
    build()

    // Reconfigure pipeline
    baseFolders(['Demo', 'pipeline', 'staging'])
    defaultConfiguration([timeout: 20])

    // Build pipeline again
    build()
}
