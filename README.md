Pax Exam
========

Thanks for looking into Pax Exam.
This is the official source repository of the OPS4J Pax Exam project for 
release lines 2.x, 3.x and 4.x.
It's licensed under the Apache Software License 2.0 by the OPS4J community.

## Documentation

* <http://team.ops4j.org/wiki/display/PAXEXAM4/Documentation>

## Contributing

In OPS4J, everyone is invited to contribute. We don't require any paperwork or community reputation.
All we ask you is to move carefully and to clean up after yourself: 

* Describe your problem or enhancement request before submitting a solution.
* Submit a JIRA issue before creating a pull request. This is required for the release notes.
* For discussions, the mailing list is more suitable than JIRA.
* Any bugfix or new feature must be covered by regression tests.
* Respect the coding style and formatting conventions of existing sources. There is an Eclipse
formatter in `assets/EclipseJavaFormatter.xml`.
 
## Build

You'll need a machine with Java 7+ and Apache Maven 3 installed.

Checkout:

    git clone git://github.com/ops4j/org.ops4j.pax.exam2.git
    git checkout master

Branches:
* v3.x   : Maintenance for Pax Exam 3.x 
* master : ongoing development for 4.x 
* exam5-milestones : future development for 5.x

Run Build:

    mvn clean install

Run build with integration tests

    mvn -Pdefault,itest clean install

## Releases

Releases go to Maven Central.

The current release of Pax Exam is 4.9.0.

## Issue Tracking

* JIRA project [PAXEXAM](http://team.ops4j.org/browse/PAXEXAM)

## Mailing List

There is a Google Groups list for all OPS4J projects: `ops4j@googlegroups.com`.

## Continuous Integration Builds

We have a continuous integration build set up here:

* <http://ci.ops4j.org/jenkins/job/org.ops4j.pax.exam-4.x>

Snapshot artifacts are being published to:

* <https://oss.sonatype.org/content/repositories/ops4j-snapshots>


The OPS4J Team.
