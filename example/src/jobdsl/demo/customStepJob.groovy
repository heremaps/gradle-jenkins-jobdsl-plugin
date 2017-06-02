import com.here.demo.jobdsl.CustomStep
import com.here.demo.jobdsl.TemplateJobBuilder

new TemplateJobBuilder(
        dslFactory: this,
        name: "Demo/CustomStepJob"
).buildFreeStyleJob {
    steps {
        CustomStep.echo(delegate, 'Hello custom build step!')
    }
}
