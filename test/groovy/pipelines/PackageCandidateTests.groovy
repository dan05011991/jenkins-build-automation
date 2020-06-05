package pipelines

import com.lesfurets.jenkins.unit.BasePipelineTest
import models.Gitflow
import org.junit.Before
import org.junit.Test

import static com.lesfurets.jenkins.unit.global.lib.LibraryConfiguration.library
import static com.lesfurets.jenkins.unit.global.lib.ProjectSource.projectSource
import static helpers.CustomAssertHelper.assertStringArray

class PackageCandidateTests extends BasePipelineTest {

    private final String pipeline = "package_candidate.groovy"

    @Override
    @Before
    void setUp() throws Exception {
        scriptRoots += 'vars'
        super.setUp()

        def library = library().name('commons')
                .defaultVersion('<notNeeded>')
                .allowOverride(true)
                .implicit(true)
                .targetPath('<notNeeded>')
                .retriever(projectSource())
                .build()
        helper.registerSharedLibrary(library)

        // Common functions
        helper.registerAllowedMethod('pwd', [], { echo '/tmp/example' })
        helper.registerAllowedMethod('cleanWs', [], { echo 'Workspace cleaned' })
        helper.registerAllowedMethod('deleteDir', [], { echo 'Deleted Directory' })
        helper.registerAllowedMethod('git', [java.util.LinkedHashMap], { echo 'Git checkout' })
        helper.registerAllowedMethod('parallel', [Map.class], { echo 'Parallel job' })

        helper.clearCallStack()
    }

    @Test
    void should_execute_pipeline_successfully_and_follow_maven_route() {
        //Arrange
        binding.setVariable("BRANCH_NAME", "feature/test")
        binding.setVariable("scm", [userRemoteConfigs: [[url: ["test"]]]])
        helper.registerAllowedMethod("sh", [Map.class], {c -> "1.0.0"})
        helper.registerAllowedMethod("withCredentials", [List.class, Closure.class], {c -> "Not required"})
        helper.registerAllowedMethod("sshagent", [Map.class, Closure.class], {c -> "Not required"})
        def script = [
                sh: {
                    return "1.0.1"
                },
                string: {
                    return ""
                },
                build: {
                    return [
                            number: '12345'
                    ]
                },
                specific: {
                    return ""
                },
                copyArtifacts: {
                    return ""
                }
        ]
        //Act
        runScript(pipeline).call(
                gitflow: new Gitflow(
                        script: script,
                        branch: "hotfix/test",
                        is_pull_request: false
                ),
                projectKey: 'example_project',
                buildType: 'maven',
                imageName: 'example_image_name',
                test: 'test.dockerfile'
        )

        //Assert
        assertStringArray([
                '   package_candidate.run()',
                '   package_candidate.call({gitflow=models.Gitflow@2e8ab815, projectKey=example_project, buildType=maven, imageName=example_image_name, test=test.dockerfile})',
                '      package_candidate.stage(Update project version, groovy.lang.Closure)',
                '         package_candidate.sh({script=git describe --tags | sed -n -e "s/\\([0-9]\\)-.*/\\1/ p", returnStdout=true})',
                '         package_candidate.stage(Maven Version Update, groovy.lang.Closure)',
                '            package_candidate.sh(mvn versions:set -DnewVersion=1.0.1)',
                '            package_candidate.sh(git add pom.xml)',
                '            package_candidate.sh(git commit -m "[Automated commit: version bump]")',
                '      package_candidate.stage(Docker Candidate Build, groovy.lang.Closure)',
                '         package_candidate.sh(docker build -t example_image_name:1.0.1-release-candidate .)',
                '      package_candidate.stage(Prepare project for next iteration, groovy.lang.Closure)',
                '         package_candidate.sh(git tag -a 1.0.1 -m "Release 1.0.1")',
                '         package_candidate.sh(mvn versions:set -DnewVersion=1.0.1-SNAPSHOT)',
                '         package_candidate.sh(mvn release:update-versions -B)',
                '         package_candidate.sh(git add pom.xml)',
                '         package_candidate.sh(git commit -m "[Automated commit: version bump]")',
                '      package_candidate.stage(Push Updates, groovy.lang.Closure)',
                '         package_candidate.withCredentials([{$class=UsernamePasswordMultiBinding, credentialsId=dockerhub, usernameVariable=USERNAME, passwordVariable=PASSWORD}], groovy.lang.Closure)',
                '         package_candidate.sshagent({credentials=[ssh]}, groovy.lang.Closure)'
        ] as String[], helper.callStack)
        assertJobStatusSuccess()
    }

    @Test
    void should_execute_pipeline_successfully_and_follow_gulp_route() {
        //Arrange
        binding.setVariable("BRANCH_NAME", "feature/test")
        binding.setVariable("scm", [userRemoteConfigs: [[url: ["test"]]]])
        helper.registerAllowedMethod("sh", [Map.class], {c -> "1.0.0"})
        helper.registerAllowedMethod("withCredentials", [List.class, Closure.class], {c -> "Not required"})
        helper.registerAllowedMethod("sshagent", [Map.class, Closure.class], {c -> "Not required"})
        def script = [
                sh: {
                    return "1.0.1"
                },
                string: {
                    return ""
                },
                build: {
                    return [
                            number: '12345'
                    ]
                },
                specific: {
                    return ""
                },
                copyArtifacts: {
                    return ""
                }
        ]
        //Act
        runScript(pipeline).call(
                gitflow: new Gitflow(
                        script: script,
                        branch: "hotfix/test",
                        is_pull_request: false
                ),
                projectKey: 'example_project',
                buildType: 'gulp',
                imageName: 'example_image_name',
                test: 'test.dockerfile'
        )

        //Assert
        assertStringArray([
                '   package_candidate.run()',
                '   package_candidate.call({gitflow=models.Gitflow@cc6460c, projectKey=example_project, buildType=gulp, imageName=example_image_name, test=test.dockerfile})',
                '      package_candidate.stage(Update project version, groovy.lang.Closure)',
                '         package_candidate.sh({script=git describe --tags | sed -n -e "s/\\([0-9]\\)-.*/\\1/ p", returnStdout=true})',
                '         package_candidate.stage(Gulp Version Update, groovy.lang.Closure)',
                '            package_candidate.sh({script=sed -n "s/^.*appVersion.*\'\\(.*\\)\'.*$/\\1/ p" conf/config-release.js | tr -d \'\\n\', returnStdout=true})',
                '            package_candidate.sh(\n                    #!/bin/bash\n                    sed -i "s/appVersion: \'1.0.0\'/appVersion: \'1.0.1\'/g" conf/config-release.js\n                )',
                '            package_candidate.sh(git add conf/config-release.js)',
                '            package_candidate.sh(git commit -m "[Automated commit: version bump]")',
                '      package_candidate.stage(Docker Candidate Build, groovy.lang.Closure)',
                '         package_candidate.sh(docker build -t example_image_name:1.0.1-release-candidate .)',
                '      package_candidate.stage(Prepare project for next iteration, groovy.lang.Closure)',
                '         package_candidate.sh(git tag -a 1.0.1 -m "Release 1.0.1")',
                '      package_candidate.stage(Push Updates, groovy.lang.Closure)',
                '         package_candidate.withCredentials([{$class=UsernamePasswordMultiBinding, credentialsId=dockerhub, usernameVariable=USERNAME, passwordVariable=PASSWORD}], groovy.lang.Closure)',
                '         package_candidate.sshagent({credentials=[ssh]}, groovy.lang.Closure)'
        ] as String[], helper.callStack)
        assertJobStatusSuccess()
    }

    @Test
    void should_execute_pipeline_successfully_and_follow_webpack_route() {
        //Arrange
        binding.setVariable("BRANCH_NAME", "feature/test")
        binding.setVariable("scm", [userRemoteConfigs: [[url: ["test"]]]])
        helper.registerAllowedMethod("sh", [Map.class], {c -> "1.0.0"})
        helper.registerAllowedMethod("withCredentials", [List.class, Closure.class], {c -> "Not required"})
        helper.registerAllowedMethod("sshagent", [Map.class, Closure.class], {c -> "Not required"})
        def script = [
                sh: {
                    return "1.0.1"
                },
                string: {
                    return ""
                },
                build: {
                    return [
                            number: '12345'
                    ]
                },
                specific: {
                    return ""
                },
                copyArtifacts: {
                    return ""
                }
        ]
        //Act
        runScript(pipeline).call(
                gitflow: new Gitflow(
                        script: script,
                        branch: "hotfix/test",
                        is_pull_request: false
                ),
                projectKey: 'example_project',
                buildType: 'webpack',
                imageName: 'example_image_name',
                test: 'test.dockerfile'
        )

        //Assert
        assertStringArray([
                '   package_candidate.run()',
                '   package_candidate.call({gitflow=models.Gitflow@cc6460c, projectKey=example_project, buildType=webpack, imageName=example_image_name, test=test.dockerfile})',
                '      package_candidate.stage(Update project version, groovy.lang.Closure)',
                '         package_candidate.sh({script=git describe --tags | sed -n -e "s/\\([0-9]\\)-.*/\\1/ p", returnStdout=true})',
                '         package_candidate.stage(Webpack Version Update, groovy.lang.Closure)',
                '            package_candidate.sh({script=sed -n "s/^.*version.*\\"\\(.*\\)\\".*$/\\1/ p" package.json | tr -d \'\\n\', returnStdout=true})',
                '            package_candidate.sh(\n                    #!/bin/bash\n                    sed -i \'s/"version": "1.0.0"/"version": "1.0.1"/g\' package.json\n                )',
                '            package_candidate.sh(git add package.json)',
                '            package_candidate.sh(git commit -m "[Automated commit: version bump]")',
                '      package_candidate.stage(Docker Candidate Build, groovy.lang.Closure)',
                '         package_candidate.sh(docker build -t example_image_name:1.0.1-release-candidate .)',
                '      package_candidate.stage(Prepare project for next iteration, groovy.lang.Closure)',
                '         package_candidate.sh(git tag -a 1.0.1 -m "Release 1.0.1")',
                '      package_candidate.stage(Push Updates, groovy.lang.Closure)',
                '         package_candidate.withCredentials([{$class=UsernamePasswordMultiBinding, credentialsId=dockerhub, usernameVariable=USERNAME, passwordVariable=PASSWORD}], groovy.lang.Closure)',
                '         package_candidate.sshagent({credentials=[ssh]}, groovy.lang.Closure)'
        ] as String[], helper.callStack)
        assertJobStatusSuccess()
    }
}
