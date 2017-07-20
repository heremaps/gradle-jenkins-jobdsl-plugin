import com.here.demo.jobdsl.TemplateJobBuilder
import javaposse.jobdsl.dsl.DslFactory

1.upto(3) { index ->
    def job = new TemplateJobBuilder(this as DslFactory)
    job.name = "TemplateJob${index}"
    job.folders = ['Demo']
    job.freeStyleJob {
        steps {
            shell("echo TemplateJob ${index}")
        }
    }
    job.build()
}
