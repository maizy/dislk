# diSlck

_TBA_


## Requirements

* macOS 10.12
* [JRE 1.8](http://www.oracle.com/technetwork/java/javase/downloads/jre8-downloads-2133155.html)


## Build

Required Oracle JDK 1.8.x

```
sbt dislckApp/jdkPackager:packageBin

```

App will be in `dislck-app/target/universal/jdkpackager/bundles/dislck.app`


## Development

In Intelij IDEA you should manually add 
[macro paradise compiler plugin](https://search.maven.org/remotecontent?filepath=org/scalamacros/paradise_2.12.2/2.1.0/paradise_2.12.2-2.1.0.jar)
to `Prefereces` -> `Scala Compiler` -> `slackClient` -> `Compiler plugins`.


## License

[Apache v2](LICENSE.txt)

## etc

[python prototype](https://github.com/maizy/dislck/tree/python-prototype)
