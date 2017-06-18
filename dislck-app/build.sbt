enablePlugins(JDKPackagerPlugin)

name := "dislck"

// macOS app packaging opts
mainClass in Compile := Some("ru.maizy.dislck.app.AppLauncher")
jdkPackagerType := "image"
jdkPackagerProperties := Map(
  "app.name" -> name.value,
  "app.version" -> version.value
)
jdkAppIcon := Some((resourceDirectory in Compile).value / "icons" / "dnd.icns")

(packageBin in JDKPackager) := {
  // TODO: write info.plist options via jdkPackager
  val res = (packageBin in JDKPackager).value
  val infoPlist = baseDirectory.value /
    "target" / "universal" / "jdkpackager" / "bundles" / "dislck.app" / "Contents" / "Info.plist"
  val content = scala.io.Source.fromFile(infoPlist, "utf-8").getLines.mkString
  val newContent = content.replace("</dict>", "<key>LSUIElement</key>\n<string>true</string>\n</dict>")
  val writer = new java.io.PrintWriter(infoPlist)
  writer.write(newContent)
  writer.close()
  res
}

// scalastyle
(test in Test) := {
  org.scalastyle.sbt.ScalastylePlugin.scalastyle.in(Test).toTask("").value
  org.scalastyle.sbt.ScalastylePlugin.scalastyle.in(Compile).toTask("").value
  (test in Test).value
}
scalastyleFailOnError := true
