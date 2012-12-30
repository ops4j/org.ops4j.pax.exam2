Pax Exam 2
================================

Thanks for looking into Pax Exam 2.
This is the official source repository of the OPS4J Pax Exam 2 project.
Its licensed under the Apache Software License 2.0 by the OPS4J community.

## Documentation

* <http://team.ops4j.org/wiki/display/paxexam/Documentation>

You may want to have a look at the Pax Exam 2 Learning project for more help and introductionary content:

* <https://github.com/tonit/Learn-PaxExam>

Or check it out directly:

    git clone git://github.com/tonit/Learn-PaxExam.git


## Build

You'll need a machine with Java 6+ and Apache Maven 3 installed.

Checkout:

    git clone git://github.com/ops4j/org.ops4j.pax.exam2.git
    git checkout v2.x

Branches:
   v2.x   : Maintenance for Pax Exam 2.x (2.6.0 and higher)
   v3.0.x : 3.0.x releases
   master : ongoing development for 3.1.0 and higher 

Run Build:

    mvn clean install

Run build with integration tests

    mvn -Pdefault,itest clean install

## Releases

Releases go to Maven Central.

The current release of Pax Exam is 2.6.0.

## Issue Tracking

* <http://team.ops4j.org/browse/PAXEXAM>

## Continuous Integration Builds

We have a continuous integration build set up here:

* <http://ci.ops4j.org/hudson/job/org.ops4j.pax.exam2>

Snapshot artifacts are being published to:

* <https://oss.sonatype.org/content/repositories/ops4j-snapshots>


The OPS4J Team.
