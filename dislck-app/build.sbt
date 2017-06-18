enablePlugins(JDKPackagerPlugin)

name := "dislck"

mainClass in Compile := Some("ru.maizy.dislck.app.AppLauncher")
jdkPackagerBasename := "diSlck"
jdkPackagerType := "image"

// scalastyle
(test in Test) := {
  org.scalastyle.sbt.ScalastylePlugin.scalastyle.in(Test).toTask("").value
  org.scalastyle.sbt.ScalastylePlugin.scalastyle.in(Compile).toTask("").value
  (test in Test).value
}
scalastyleFailOnError := true
