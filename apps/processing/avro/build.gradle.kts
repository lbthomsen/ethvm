import com.commercehub.gradle.plugin.avro.DefaultAvroExtension
import com.commercehub.gradle.plugin.avro.GenerateAvroProtocolTask
import com.commercehub.gradle.plugin.avro.GenerateAvroSchemaTask
import org.gradle.api.tasks.bundling.Jar

plugins {
  `java-library`
  `maven-publish`
  kotlin("jvm")
  id("com.commercehub.gradle.plugin.avro") version "0.15.1"
}

val test by tasks.getting(Test::class) { useJUnitPlatform {} }

dependencies {

  compile("org.apache.avro:avro:1.8.2")

}

avro {

}

tasks {

  val generateProtocol by creating(GenerateAvroProtocolTask::class) {
    source("src/main/avro")
    include("**/*.avdl")
    setOutputDir(File("build/generated-main-avro-avpr"))
  }

  val generateSchema by creating(GenerateAvroSchemaTask::class) {
    dependsOn("generateProtocol")
    source("src/main/avro", "build/generated-main-avro-avpr")
    include("**/*.avpr")
    setOutputDir(File("build/generated-main-avro-avsc"))
  }

}

val sourcesJar by tasks.creating(Jar::class) {
  dependsOn(JavaPlugin.CLASSES_TASK_NAME)
  classifier = "sources"
  from(project.java.sourceSets["main"].allSource)
}

val javadocJar by tasks.creating(Jar::class) {
  dependsOn(JavaPlugin.JAVADOC_TASK_NAME)
  classifier = "javadoc"
  from(tasks["javadoc"])
}

artifacts {
  add("archives", sourcesJar)
  add("archives", javadocJar)
}

publishing {

  publications {

    val mavenJava by creating(MavenPublication::class) {
      from(components["java"])
      artifact(sourcesJar)
    }

  }

}