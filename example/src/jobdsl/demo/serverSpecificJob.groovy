import com.here.demo.jobdsl.CustomStep
import com.here.demo.jobdsl.TemplateJobBuilder
import com.here.gradle.plugins.jobdsl.util.DslConfig

new TemplateJobBuilder(
        dslFactory: this,
        name: 'Demo/ServerSpecificJob'
).buildFreeStyleJob {
    steps {
        CustomStep.echo(delegate, DslConfig.get('serverName'))
    }
}
