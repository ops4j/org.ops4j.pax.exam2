Pax Exam 2 - Provisioning
================================

Here is an outline of a possible solution:

1) Use depends-maven-plugin to generate dependencies.properties, listing all transitive dependencies with version and scope in a format easily consumed without a runtime dependency on Maven APIs.

2) Add a goal to the new exam-maven-plugin to postprocess this properties file, allowing to include or exclude artifacts by scope or groupId or artifactId patterns. The result of this step should be a file with mvn: URLs.

3) Revive the scanFile() option to provision bundles listed in the file resulting from 2).



1) is already used to support the versionAsInProject() feature.

2) would be a new Mojo for the exam-maven-plugin available on the exam3-milestones branch.

3) is only supported by Pax Runner Container in 2.x, it's deprecated and more or less undocumented, and it's been removed in 3.x, but it's certainly feasible to reintroduce this option (or a similar one) and make it work for _all_ containers.

See also http://team.ops4j.org/wiki/display/paxscanner/File+Scanner

