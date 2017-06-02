import com.here.demo.jobdsl.EchoJobBuilder

[1, 2, 3].each { index ->
    new EchoJobBuilder(
            dslFactory: this,
            name: "Demo/EchoJob${index}",
            echo: "EchoJob ${index}"
    ).build()
}
