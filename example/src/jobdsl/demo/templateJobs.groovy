import com.here.demo.jobdsl.TemplateJobBuilder

1.upto(3) { index ->
    new TemplateJobBuilder(
            dslFactory: this,
            name: "Demo/TemplateJob${index}"
    ).buildFreeStyleJob {
        steps {
            shell("echo TemplateJob ${index}")
        }
    }
}
