repositories.remote << 'http://scala-tools.org/repo-releases' # for scala lib
repositories.remote << 'http://download.oracle.com/maven' # for Berkeley DB
repositories.remote << 'http://mirrors.ibiblio.org/pub/mirrors/maven2/' # for ant

def jgoodies_artifact(name, version)
  artifact = artifact("com.jgoodies:#{name}:jar:#{version}").from(
    file("target/jgoodies-#{name}-#{version}/jgoodies-#{name}-#{version}/jgoodies-#{name}-#{version}.jar" =>
      unzip("target/jgoodies-#{name}-#{version}" =>
        download("target/jgoodies-#{name}-#{version}.zip" => "http://www.jgoodies.com/download/libraries/#{name}/jgoodies-#{name}-#{version.gsub(/\./, '_')}.zip"))))
  artifact.install
  artifact
end


MOCKITO = 'org.mockito:mockito-all:jar:1.9.0'
FEST_ASSERT = 'org.easytesting:fest-assert:jar:1.4'
SCALA_LIB = 'org.scala-lang:scala-library:jar:2.9.1-1'
JSR166Y = 'org.codehaus.jsr166-mirror:jsr166y:jar:1.7.0'
JAVOLUTION = 'org.javolution:javolutoin:jar:5.5.1'
download artifact(JAVOLUTION) =>
  'http://download.java.net/maven/2/javolution/javolution/5.5.1/javolution-5.5.1.jar'
GUAVA = transitive('com.google.guava:guava:jar:11.0.2')
COMMONS_MATH = 'org.apache.commons:commons-math:jar:2.2'
COMMONS_POOL = 'commons-pool:commons-pool:jar:1.6'
GUICE = transitive('com.google.inject:guice:jar:3.0')
GUICE_ASSISTED = 'com.google.inject.extensions:guice-assistedinject:jar:3.0'
COMMONS_JEXL = transitive('org.apache.commons:commons-jexl:jar:2.1.1')
JGOODIES_BINDING = jgoodies_artifact("binding", "2.6.0")
JGOODIES_FORMS = jgoodies_artifact("forms", "1.5.0")
JGOODIES_VALIDATION = jgoodies_artifact("validation", "2.4.0")
JGOODIES_COMMON = jgoodies_artifact("common", "1.3.0")
BSAF = 'org.jdesktop.bsaf:bsaf:jar:1.9.2'
JFREE_CHART = transitive('jfree:jfreechart:jar:1.0.13')
SLF4J = 'org.slf4j:slf4j-api:jar:1.6.1'
LOGBACK = transitive('ch.qos.logback:logback-classic:jar:1.0.0')
SIMPLE_XML = 'org.simpleframework:simple-xml:jar:2.6.2'
BERKELEY_DB = 'com.sleepycat:je:jar:5.0.34'

KDTREE = 'org.asoem:kdtree:jar:1.0.0'
MACWIDGETS = 'com.explodingpixels:macwidgets:jar:0.9.5-SNAPSHOT'

define 'greyfish' do
  project.version = '1.0-SNAPSHOT'
  project.group = 'asoem'

  package :jar

  compile.with SCALA_LIB, JSR166Y, JAVOLUTION, GUAVA, COMMONS_MATH, COMMONS_POOL, GUICE, GUICE_ASSISTED, COMMONS_JEXL,
  SLF4J, LOGBACK, SIMPLE_XML, BERKELEY_DB, JGOODIES_BINDING, JGOODIES_FORMS, JGOODIES_VALIDATION, BSAF, JFREE_CHART, KDTREE, MACWIDGETS, JGOODIES_COMMON

  test.with MOCKITO, FEST_ASSERT
end
