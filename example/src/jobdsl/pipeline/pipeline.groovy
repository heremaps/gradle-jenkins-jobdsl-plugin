import com.here.demo.jobdsl.DemoPipelineJobBuilder
import com.here.gradle.plugins.jobdsl.util.PipelineBuilder

folder('demo')
folder('demo/pipeline')
folder('demo/pipeline/master')
folder('demo/pipeline/staging')

// Configure pipeline builder
def pipelineBuilder = new PipelineBuilder()
pipelineBuilder.baseFolders(['demo', 'pipeline', 'master'])
pipelineBuilder.defaultConfiguration([timeout: 10])
pipelineBuilder.commonDsl {
    wrappers {
        timestamps()
    }
}

// Add jobs to pipeline
def firstJob = new DemoPipelineJobBuilder(this)
firstJob.name = 'First job'
firstJob.shellCommand = 'ls'
firstJob.freeStyleJob {
    publishers {
        archiveArtifacts('*')
    }
}

def secondJob = new DemoPipelineJobBuilder(this)
secondJob.name = 'Second job'
secondJob.shellCommand = 'ls -a'
secondJob.freeStyleJob {
    publishers {
        archiveArtifacts('*')
    }
}

def thirdJob = new DemoPipelineJobBuilder(this)
thirdJob.name = 'Third job'
thirdJob.shellCommand = 'ls -al'
thirdJob.freeStyleJob {
    publishers {
        archiveArtifacts('*')
    }
}

pipelineBuilder.addJob(firstJob)
pipelineBuilder.addJob(secondJob)
pipelineBuilder.addJob(thirdJob)

// Build pipeline
pipelineBuilder.build()

// Reconfigure pipeline
pipelineBuilder.baseFolders(['demo', 'pipeline', 'staging'])
pipelineBuilder.defaultConfiguration([timeout: 20])

// Build pipeline again
pipelineBuilder.build()
