# diSlk

_TBA_


## Requirements

* macOS 10.12

## Build

Required Oracle JDK 1.8.x

```
sbt dislkApp/jdkPackager:packageBin

```

App will be in `dislk-app/target/universal/jdkpackager/bundles/dislk.app`.
Final app contains embedded JRE.


## Setup

**Temp**, will be configured in UI in the next versions.

Add ~/.config/dislk.json:

```
{
    "personal_token": "abcd-1234-5678-901234-1234abcdef",
    "autoset_status": {
        "text": "In DND mode until %1$tH:%1$tM",
        "emoji": ":zzz:"
    }
}
```

Token can be requested [here](https://api.slack.com/custom-integrations/legacy-tokens).

## Development

In Intelij IDEA you should manually add
[macro paradise compiler plugin](https://search.maven.org/remotecontent?filepath=org/scalamacros/paradise_2.12.2/2.1.0/paradise_2.12.2-2.1.0.jar)
to `Prefereces` -> `Scala Compiler` -> `slackClient`/`dislckApp` -> `Compiler plugins`.


## License

[Apache v2](LICENSE.txt)

## etc

[python prototype](https://github.com/maizy/dislk/tree/python-prototype)
