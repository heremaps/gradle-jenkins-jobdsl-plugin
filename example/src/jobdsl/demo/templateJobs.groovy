import com.here.demo.jobdsl.TemplateJobBuilder

[1,2,3].each { index ->
    new TemplateJobBuilder(
            dslFactory: this,
            name: "Demo/TemplateJob${index}"
    ).buildFreeStyleJob {
        steps {
            shell("echo TemplateJob ${index}")
        }
    }
}
