import com.here.demo.jobdsl.CustomStep
import com.here.demo.jobdsl.TemplateJobBuilder
import javaposse.jobdsl.dsl.DslFactory

def job = new TemplateJobBuilder(this as DslFactory)
job.name = 'CustomStepJob'
job.folders = ['Demo']
job.freeStyleJob {
    steps {
        CustomStep.echo(delegate, 'Hello custom build step!')
    }
}
job.build()
