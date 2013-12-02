Pax Exam
========

Thanks for looking into Pax Exam.
This is the official source repository of the OPS4J Pax Exam project for 
release lines 2.x and 3.x.
It's licensed under the Apache Software License 2.0 by the OPS4J community.

## Documentation

* <http://team.ops4j.org/wiki/display/PAXEXAM3/Documentation>

You may want to have a look at the Pax Exam 2 Learning project for more help and introductory content:

* <https://github.com/tonit/Learn-PaxExam>

Or check it out directly:

    git clone git://github.com/tonit/Learn-PaxExam.git


## Build

You'll need a machine with Java 6+ and Apache Maven 3 installed.

Checkout:

    git clone git://github.com/ops4j/org.ops4j.pax.exam2.git
    git checkout master

Branches:
* v2.x   : Maintenance for Pax Exam 2.x (2.6.0 and higher)
* v3.0.x : 3.0.x releases
* v3.1.x : 3.1.x releases
* master : ongoing development for 3.5.0 and higher 

Run Build:

    mvn clean install

Run build with integration tests

    mvn -Pdefault,itest clean install

## Releases

Releases go to Maven Central.

The current release of Pax Exam is 3.4.0.

## Issue Tracking

* <http://team.ops4j.org/browse/PAXEXAM>

## Continuous Integration Builds

We have a continuous integration build set up here:

* <http://ci.ops4j.org/jenkins/job/org.ops4j.pax.exam3>

Snapshot artifacts are being published to:

* <https://oss.sonatype.org/content/repositories/ops4j-snapshots>


The OPS4J Team.
