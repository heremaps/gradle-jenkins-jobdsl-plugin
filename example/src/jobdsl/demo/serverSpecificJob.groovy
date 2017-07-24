import com.here.demo.jobdsl.CustomStep
import com.here.demo.jobdsl.TemplateJobBuilder
import com.here.gradle.plugins.jobdsl.util.DslConfig
import javaposse.jobdsl.dsl.DslFactory

def job = new TemplateJobBuilder(this as DslFactory)
job.name = 'ServerSpecificJob'
job.folders = ['Demo']
job.freeStyleJob {
    steps {
        CustomStep.echo(delegate, DslConfig.get('serverName'))
    }
}
job.build()
