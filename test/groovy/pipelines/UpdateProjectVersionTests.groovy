package pipelines

import com.lesfurets.jenkins.unit.BasePipelineTest
import helpers.Pipeline
import models.Gitflow
import org.junit.Before
import org.junit.Test

import static helpers.CustomAssertHelper.assertStringArray

class UpdateProjectVersionTests extends BasePipelineTest {
    private final String pipeline = "update_project_version.groovy"

    @Override
    @Before
    void setUp() throws Exception {
        scriptRoots += 'vars'
        super.setUp()
        Pipeline.setupLibrary(helper)
    }

    @Test
    void should_update_project_version_for_hotfix_branch_on_maven_builds() {
        //Arrange
        helper.registerAllowedMethod("sh", [Map.class], {c -> "1.0.0"})
        def script = Pipeline.getScript()

        //Act
        runScript(pipeline).call(
                gitflow: new Gitflow(
                        script: script,
                        branch: "hotfix/test",
                        is_pull_request: false
                ),
                buildType: 'maven'
        )

        //Assert
        assertStringArray([
                '   update_project_version.run()',
                '   update_project_version.call({gitflow=models.Gitflow@3b366632, buildType=maven})',
                '      update_project_version.stage(Update project version, groovy.lang.Closure)',
                '         update_project_version.sh({script=git describe --tags | sed -n -e "s/\\([0-9]\\)-.*/\\1/ p", returnStdout=true})',
                '         update_project_version.stage(Maven Version Update, groovy.lang.Closure)',
                '            update_project_version.sh(mvn versions:set -DnewVersion=1.0.1)',
                '            update_project_version.sh(git add pom.xml)',
                '            update_project_version.sh(git commit -m "[Automated commit: version bump]")'
        ] as String[], helper.callStack)
        assertJobStatusSuccess()
    }

    @Test
    void should_update_project_version_for_hotfix_branch_on_docker_in_maven_builds() {
        //Arrange
        helper.registerAllowedMethod("sh", [Map.class], {c -> "1.0.0"})
        def script = Pipeline.getScript()

        //Act
        runScript(pipeline).call(
                gitflow: new Gitflow(
                        script: script,
                        branch: "hotfix/test",
                        is_pull_request: false
                ),
                buildType: 'docker-in-maven'
        )

        //Assert
        assertStringArray([
                '   update_project_version.run()',
                '   update_project_version.call({gitflow=models.Gitflow@3b366632, buildType=docker-in-maven})',
                '      update_project_version.stage(Update project version, groovy.lang.Closure)',
                '         update_project_version.sh({script=git describe --tags | sed -n -e "s/\\([0-9]\\)-.*/\\1/ p", returnStdout=true})',
                '         update_project_version.stage(Maven Version Update, groovy.lang.Closure)',
                '            update_project_version.sh(mvn versions:set -DnewVersion=1.0.1)',
                '            update_project_version.sh(git add pom.xml)',
                '            update_project_version.sh(git commit -m "[Automated commit: version bump]")'
        ] as String[], helper.callStack)
        assertJobStatusSuccess()
    }

    @Test
    void should_update_project_version_for_hotfix_branch_on_gulp_builds() {
        //Arrange
        helper.registerAllowedMethod("sh", [Map.class], {c -> "1.0.0"})
        def script = Pipeline.getScript()

        //Act
        runScript(pipeline).call(
                gitflow: new Gitflow(
                        script: script,
                        branch: "hotfix/test",
                        is_pull_request: false
                ),
                buildType: 'gulp'
        )

        //Assert
        assertStringArray([
                '   update_project_version.run()',
                '   update_project_version.call({gitflow=models.Gitflow@3b366632, buildType=gulp})',
                '      update_project_version.stage(Update project version, groovy.lang.Closure)',
                '         update_project_version.sh({script=git describe --tags | sed -n -e "s/\\([0-9]\\)-.*/\\1/ p", returnStdout=true})',
                '         update_project_version.stage(Gulp Version Update, groovy.lang.Closure)',
                '            update_project_version.sh({script=sed -n "s/^.*appVersion.*\'\\(.*\\)\'.*$/\\1/ p" conf/config-release.js | tr -d \'\\n\', returnStdout=true})',
                '            update_project_version.sh(\n                    #!/bin/bash\n                    sed -i "s/appVersion: \'1.0.0\'/appVersion: \'1.0.1\'/g" conf/config-release.js\n                )',
                '            update_project_version.sh(git add conf/config-release.js)',
                '            update_project_version.sh(git commit -m "[Automated commit: version bump]")'
        ] as String[], helper.callStack)
        assertJobStatusSuccess()
    }

    @Test
    void should_update_project_version_for_hotfix_branch_on_webpack_builds() {
        //Arrange
        helper.registerAllowedMethod("sh", [Map.class], {c -> "1.0.0"})
        def script = Pipeline.getScript()

        //Act
        runScript(pipeline).call(
                gitflow: new Gitflow(
                        script: script,
                        branch: "hotfix/test",
                        is_pull_request: false
                ),
                buildType: 'webpack'
        )

        //Assert
        assertStringArray([
                '   update_project_version.run()',
                '   update_project_version.call({gitflow=models.Gitflow@3b366632, buildType=webpack})',
                '      update_project_version.stage(Update project version, groovy.lang.Closure)',
                '         update_project_version.sh({script=git describe --tags | sed -n -e "s/\\([0-9]\\)-.*/\\1/ p", returnStdout=true})',
                '         update_project_version.stage(Webpack Version Update, groovy.lang.Closure)',
                '            update_project_version.sh({script=sed -n "s/^.*version.*\\"\\(.*\\)\\".*$/\\1/ p" package.json | tr -d \'\\n\', returnStdout=true})',
                '            update_project_version.sh(\n                    #!/bin/bash\n                    sed -i \'s/"version": "1.0.0"/"version": "1.0.1"/g\' package.json\n                )',
                '            update_project_version.sh(git add package.json)',
                '            update_project_version.sh(git commit -m "[Automated commit: version bump]")'
        ] as String[], helper.callStack)
        assertJobStatusSuccess()
    }

    @Test
    void should_update_project_version_for_release_branch_on_maven_builds() {
        //Arrange
        helper.registerAllowedMethod("sh", [Map.class], {c -> "1.0.0"})
        def script = Pipeline.getScript()

        //Act
        runScript(pipeline).call(
                gitflow: new Gitflow(
                        script: script,
                        branch: "release/test",
                        is_pull_request: false
                ),
                buildType: 'maven'
        )

        //Assert
        assertStringArray([
                '   update_project_version.run()',
                '   update_project_version.call({gitflow=models.Gitflow@3b366632, buildType=maven})',
                '      update_project_version.stage(Update project version, groovy.lang.Closure)',
                '         update_project_version.sh({script=git describe --tags | sed -n -e "s/\\([0-9]\\)-.*/\\1/ p", returnStdout=true})',
                '         update_project_version.stage(Maven Version Update, groovy.lang.Closure)',
                '            update_project_version.sh(mvn versions:set -DnewVersion=1.0.1)',
                '            update_project_version.sh(git add pom.xml)',
                '            update_project_version.sh(git commit -m "[Automated commit: version bump]")'
        ] as String[], helper.callStack)
        assertJobStatusSuccess()
    }

    @Test
    void should_update_project_version_for_release_branch_on_docker_in_maven_builds() {
        //Arrange
        helper.registerAllowedMethod("sh", [Map.class], {c -> "1.0.0"})
        def script = Pipeline.getScript()

        //Act
        runScript(pipeline).call(
                gitflow: new Gitflow(
                        script: script,
                        branch: "release/test",
                        is_pull_request: false
                ),
                buildType: 'docker-in-maven'
        )

        //Assert
        assertStringArray([
                '   update_project_version.run()',
                '   update_project_version.call({gitflow=models.Gitflow@3b366632, buildType=docker-in-maven})',
                '      update_project_version.stage(Update project version, groovy.lang.Closure)',
                '         update_project_version.sh({script=git describe --tags | sed -n -e "s/\\([0-9]\\)-.*/\\1/ p", returnStdout=true})',
                '         update_project_version.stage(Maven Version Update, groovy.lang.Closure)',
                '            update_project_version.sh(mvn versions:set -DnewVersion=1.0.1)',
                '            update_project_version.sh(git add pom.xml)',
                '            update_project_version.sh(git commit -m "[Automated commit: version bump]")'
        ] as String[], helper.callStack)
        assertJobStatusSuccess()
    }

    @Test
    void should_update_project_version_for_release_branch_on_gulp_builds() {
        //Arrange
        helper.registerAllowedMethod("sh", [Map.class], {c -> "1.0.0"})
        def script = Pipeline.getScript()

        //Act
        runScript(pipeline).call(
                gitflow: new Gitflow(
                        script: script,
                        branch: "release/test",
                        is_pull_request: false
                ),
                buildType: 'gulp'
        )

        //Assert
        assertStringArray([
                '   update_project_version.run()',
                '   update_project_version.call({gitflow=models.Gitflow@3b366632, buildType=gulp})',
                '      update_project_version.stage(Update project version, groovy.lang.Closure)',
                '         update_project_version.sh({script=git describe --tags | sed -n -e "s/\\([0-9]\\)-.*/\\1/ p", returnStdout=true})',
                '         update_project_version.stage(Gulp Version Update, groovy.lang.Closure)',
                '            update_project_version.sh({script=sed -n "s/^.*appVersion.*\'\\(.*\\)\'.*$/\\1/ p" conf/config-release.js | tr -d \'\\n\', returnStdout=true})',
                '            update_project_version.sh(\n                    #!/bin/bash\n                    sed -i "s/appVersion: \'1.0.0\'/appVersion: \'1.0.1\'/g" conf/config-release.js\n                )',
                '            update_project_version.sh(git add conf/config-release.js)',
                '            update_project_version.sh(git commit -m "[Automated commit: version bump]")'
        ] as String[], helper.callStack)
        assertJobStatusSuccess()
    }

    @Test
    void should_update_project_version_for_release_branch_on_webpack_builds() {
        //Arrange
        helper.registerAllowedMethod("sh", [Map.class], {c -> "1.0.0"})
        def script = Pipeline.getScript()

        //Act
        runScript(pipeline).call(
                gitflow: new Gitflow(
                        script: script,
                        branch: "release/test",
                        is_pull_request: false
                ),
                buildType: 'webpack'
        )

        //Assert
        assertStringArray([
                '   update_project_version.run()',
                '   update_project_version.call({gitflow=models.Gitflow@3b366632, buildType=webpack})',
                '      update_project_version.stage(Update project version, groovy.lang.Closure)',
                '         update_project_version.sh({script=git describe --tags | sed -n -e "s/\\([0-9]\\)-.*/\\1/ p", returnStdout=true})',
                '         update_project_version.stage(Webpack Version Update, groovy.lang.Closure)',
                '            update_project_version.sh({script=sed -n "s/^.*version.*\\"\\(.*\\)\\".*$/\\1/ p" package.json | tr -d \'\\n\', returnStdout=true})',
                '            update_project_version.sh(\n                    #!/bin/bash\n                    sed -i \'s/"version": "1.0.0"/"version": "1.0.1"/g\' package.json\n                )',
                '            update_project_version.sh(git add package.json)',
                '            update_project_version.sh(git commit -m "[Automated commit: version bump]")'
        ] as String[], helper.callStack)
        assertJobStatusSuccess()
    }

    @Test
    void should_update_project_version_for_feature_branch_on_maven_builds() {
        //Arrange
        helper.registerAllowedMethod("sh", [Map.class], {c -> "1.0.0"})
        def script = Pipeline.getScript()

        //Act
        runScript(pipeline).call(
                gitflow: new Gitflow(
                        script: script,
                        branch: "feature/test-123_456",
                        is_pull_request: false
                ),
                buildType: 'maven'
        )

        //Assert
        assertStringArray([
                '   update_project_version.run()',
                '   update_project_version.call({gitflow=models.Gitflow@3b366632, buildType=maven})',
                '      update_project_version.stage(Update project version, groovy.lang.Closure)',
                '         update_project_version.sh({script=git describe --tags | sed -n -e "s/\\([0-9]\\)-.*/\\1/ p", returnStdout=true})',
                '         update_project_version.stage(Maven Version Update, groovy.lang.Closure)',
                '            update_project_version.sh(mvn versions:set -DnewVersion=feature_test-123-456)',
                '            update_project_version.sh(git add pom.xml)',
                '            update_project_version.sh(git commit -m "[Automated commit: version bump]")'
        ] as String[], helper.callStack)
        assertJobStatusSuccess()
    }

    @Test
    void should_update_project_version_for_feature_branch_on_docker_in_maven_builds() {
        //Arrange
        helper.registerAllowedMethod("sh", [Map.class], {c -> "1.0.0"})
        def script = Pipeline.getScript()

        //Act
        runScript(pipeline).call(
                gitflow: new Gitflow(
                        script: script,
                        branch: "feature/test-123_456",
                        is_pull_request: false
                ),
                buildType: 'docker-in-maven'
        )

        //Assert
        assertStringArray([
                '   update_project_version.run()',
                '   update_project_version.call({gitflow=models.Gitflow@3b366632, buildType=docker-in-maven})',
                '      update_project_version.stage(Update project version, groovy.lang.Closure)',
                '         update_project_version.sh({script=git describe --tags | sed -n -e "s/\\([0-9]\\)-.*/\\1/ p", returnStdout=true})',
                '         update_project_version.stage(Maven Version Update, groovy.lang.Closure)',
                '            update_project_version.sh(mvn versions:set -DnewVersion=feature_test-123-456)',
                '            update_project_version.sh(git add pom.xml)',
                '            update_project_version.sh(git commit -m "[Automated commit: version bump]")'
        ] as String[], helper.callStack)
        assertJobStatusSuccess()
    }

    @Test
    void should_update_project_version_for_feature_branch_on_gulp_builds() {
        //Arrange
        helper.registerAllowedMethod("sh", [Map.class], {c -> "1.0.0"})
        def script = Pipeline.getScript()

        //Act
        runScript(pipeline).call(
                gitflow: new Gitflow(
                        script: script,
                        branch: "feature/test-123_456",
                        is_pull_request: false
                ),
                buildType: 'gulp'
        )

        //Assert
        assertStringArray([
                '   update_project_version.run()',
                '   update_project_version.call({gitflow=models.Gitflow@3b366632, buildType=gulp})',
                '      update_project_version.stage(Update project version, groovy.lang.Closure)',
                '         update_project_version.sh({script=git describe --tags | sed -n -e "s/\\([0-9]\\)-.*/\\1/ p", returnStdout=true})',
                '         update_project_version.stage(Gulp Version Update, groovy.lang.Closure)',
                '            update_project_version.sh({script=sed -n "s/^.*appVersion.*\'\\(.*\\)\'.*$/\\1/ p" conf/config-release.js | tr -d \'\\n\', returnStdout=true})',
                '            update_project_version.sh(\n                    #!/bin/bash\n                    sed -i "s/appVersion: \'1.0.0\'/appVersion: \'feature_test-123-456\'/g" conf/config-release.js\n                )',
                '            update_project_version.sh(git add conf/config-release.js)',
                '            update_project_version.sh(git commit -m "[Automated commit: version bump]")'
        ] as String[], helper.callStack)
        assertJobStatusSuccess()
    }

    @Test
    void should_update_project_version_for_feature_branch_on_webpack_builds() {
        //Arrange
        helper.registerAllowedMethod("sh", [Map.class], {c -> "1.0.0"})
        def script = Pipeline.getScript()

        //Act
        runScript(pipeline).call(
                gitflow: new Gitflow(
                        script: script,
                        branch: "feature/test-123_456",
                        is_pull_request: false
                ),
                buildType: 'webpack'
        )

        //Assert
        assertStringArray([
                '   update_project_version.run()',
                '   update_project_version.call({gitflow=models.Gitflow@3b366632, buildType=webpack})',
                '      update_project_version.stage(Update project version, groovy.lang.Closure)',
                '         update_project_version.sh({script=git describe --tags | sed -n -e "s/\\([0-9]\\)-.*/\\1/ p", returnStdout=true})',
                '         update_project_version.stage(Webpack Version Update, groovy.lang.Closure)',
                '            update_project_version.sh({script=sed -n "s/^.*version.*\\"\\(.*\\)\\".*$/\\1/ p" package.json | tr -d \'\\n\', returnStdout=true})',
                '            update_project_version.sh(\n                    #!/bin/bash\n                    sed -i \'s/"version": "1.0.0"/"version": "feature_test-123-456"/g\' package.json\n                )',
                '            update_project_version.sh(git add package.json)',
                '            update_project_version.sh(git commit -m "[Automated commit: version bump]")'
        ] as String[], helper.callStack)
        assertJobStatusSuccess()
    }
}
