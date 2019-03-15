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
