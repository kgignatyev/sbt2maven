sbt2maven
=========

utility to publish sbt built artifacts to maven repo without changing SBT build file

Rational: some project provide SBT only build and it makes hard for Maven managed projects to use locally built artifacts.
This utility places SBT locally generated artifacts into local Maven repository.


mvn install
creates executable jar file that can be invoked as
java -jar ~/.m2/repository/org/kgi/sbt/mavenhelper/sbt-2-maven/1.0-SNAPSHOT/sbt-2-maven-1.0-SNAPSHOT.jar  in SBT project


1. build project with <b>'sbt +publish-local'</b>
2. run this utility in the project's directory and it will publish all the artifacts to local maven repository