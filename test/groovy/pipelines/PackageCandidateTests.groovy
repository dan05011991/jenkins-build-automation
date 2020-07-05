package pipelines

import com.lesfurets.jenkins.unit.BasePipelineTest
import helpers.Pipeline
import models.Docker
import models.Gitflow
import org.junit.Before
import org.junit.Test

import static helpers.CustomAssertHelper.assertStringArray

class PackageCandidateTests extends BasePipelineTest {

    private final String pipeline = "package_candidate.groovy"

    @Override
    @Before
    void setUp() throws Exception {
        scriptRoots += 'vars'
        super.setUp()
        Pipeline.setupLibrary(helper)
    }

    @Test
    void should_execute_pipeline_successfully_and_follow_docker_in_maven_route() {
        //Arrange
        helper.registerAllowedMethod("sh", [Map.class], { c -> "1.0.0" })
        helper.registerAllowedMethod("sshagent", [Map.class, Closure.class], { c -> "Not required" })
        def script = [
                sh           : {
                    return "1.0.1"
                },
                string       : {
                    return ""
                },
                build        : {
                    return [
                            number: '12345'
                    ]
                },
                specific     : {
                    return ""
                },
                copyArtifacts: {
                    return ""
                }
        ]
        def gitflow = new Gitflow(
                script: script,
                source: "hotfix/test",
                is_pull_request: false
        )
        def docker = new Docker(
                script: script,
                gitflow: gitflow
        )

        //Act
        runScript(pipeline).call(
                gitflow: gitflow,
                docker_helper: docker,
                buildType: 'docker-in-maven',
                imageName: 'example_image_name',
                test: 'test.dockerfile',
                testMounts: '-v test:test'
        )

        //Assert
        assertStringArray([
                '   package_candidate.run()',
                '   package_candidate.call({gitflow=models.Gitflow@56b78e55, docker_helper=models.Docker@585811a4, buildType=docker-in-maven, imageName=example_image_name, test=test.dockerfile, testMounts=-v test:test})',
                '      package_candidate.sh({script=mvn help:evaluate -Dexpression=project.version -q -DforceStdout, returnStdout=true})',
                '      package_candidate.stage(Docker Candidate Build, groovy.lang.Closure)',
                '         package_candidate.sh(docker build -t d723f72c-786a-47f8-a218-0128e63fce24 -f test.dockerfile .)',
                '         package_candidate.sh(docker run --name d723f72c-786a-47f8-a218-0128e63fce24 -v test:test d723f72c-786a-47f8-a218-0128e63fce24)',
                '         package_candidate.sh(docker rm -f d723f72c-786a-47f8-a218-0128e63fce24)',
                '         package_candidate.sh(docker rmi d723f72c-786a-47f8-a218-0128e63fce24)',
                '      package_candidate.stage(Prepare project for next iteration, groovy.lang.Closure)',
                '         package_candidate.sh(git tag -a 1.0.0 -m "Release 1.0.0")',
                '         package_candidate.sh(mvn versions:set -DnewVersion=1.0.0-SNAPSHOT)',
                '         package_candidate.sh(mvn release:update-versions -B)',
                '         package_candidate.sh(git add pom.xml)',
                '         package_candidate.sh(git commit -m "[Automated commit: version bump]")',
                '      package_candidate.stage(Push Updates, groovy.lang.Closure)',
                '         package_candidate.sh(docker push index.docker.io/example_image_name:1.0.0-release-candidate)',
                '         package_candidate.sshagent({credentials=[ssh]}, groovy.lang.Closure)'
        ] as String[], helper.callStack)
        assertJobStatusSuccess()
    }

    @Test
    void should_execute_pipeline_successfully_and_follow_maven_route() {
        //Arrange
        helper.registerAllowedMethod("sh", [Map.class], { c -> "1.0.0" })
        helper.registerAllowedMethod("sshagent", [Map.class, Closure.class], { c -> "Not required" })
        def script = [
                sh           : {
                    return "1.0.1"
                },
                string       : {
                    return ""
                },
                build        : {
                    return [
                            number: '12345'
                    ]
                },
                specific     : {
                    return ""
                },
                copyArtifacts: {
                    return ""
                }
        ]
        def gitflow = new Gitflow(
                script: script,
                source: "hotfix/test",
                is_pull_request: false
        )
        def docker = new Docker(
                script: script,
                gitflow: gitflow
        )

        //Act
        runScript(pipeline).call(
                gitflow: gitflow,
                docker_helper: docker,
                buildType: 'maven',
                imageName: 'example_image_name',
                test: 'test.dockerfile',
                testMounts: '-v test:test'
        )

        //Assert
        assertStringArray([
                '   package_candidate.run()',
                '   package_candidate.call({gitflow=models.Gitflow@1a2e2935, docker_helper=models.Docker@1b9ea3e3, buildType=maven, imageName=example_image_name, test=test.dockerfile, testMounts=-v test:test})',
                '      package_candidate.sh({script=mvn help:evaluate -Dexpression=project.version -q -DforceStdout, returnStdout=true})',
                '      package_candidate.stage(Docker Candidate Build, groovy.lang.Closure)',
                '         package_candidate.sh(docker build -t index.docker.io/example_image_name:1.0.0-release-candidate .)',
                '      package_candidate.stage(Prepare project for next iteration, groovy.lang.Closure)',
                '         package_candidate.sh(git tag -a 1.0.0 -m "Release 1.0.0")',
                '         package_candidate.sh(mvn versions:set -DnewVersion=1.0.0-SNAPSHOT)',
                '         package_candidate.sh(mvn release:update-versions -B)',
                '         package_candidate.sh(git add pom.xml)',
                '         package_candidate.sh(git commit -m "[Automated commit: version bump]")',
                '      package_candidate.stage(Push Updates, groovy.lang.Closure)',
                '         package_candidate.sh(docker push index.docker.io/example_image_name:1.0.0-release-candidate)',
                '         package_candidate.sshagent({credentials=[ssh]}, groovy.lang.Closure)',
        ] as String[], helper.callStack)
        assertJobStatusSuccess()
    }

    @Test
    void should_execute_pipeline_successfully_and_follow_gulp_route() {
        //Arrange
        helper.registerAllowedMethod("sh", [Map.class], { c -> "1.0.0" })
        helper.registerAllowedMethod("sshagent", [Map.class, Closure.class], { c -> "Not required" })
        def script = [
                sh           : {
                    return "1.0.1"
                },
                string       : {
                    return ""
                },
                build        : {
                    return [
                            number: '12345'
                    ]
                },
                specific     : {
                    return ""
                },
                copyArtifacts: {
                    return ""
                }
        ]
        def gitflow = new Gitflow(
                script: script,
                source: "hotfix/test",
                is_pull_request: false
        )
        def docker = new Docker(
                script: script,
                gitflow: gitflow
        )

        //Act
        runScript(pipeline).call(
                gitflow: gitflow,
                docker_helper: docker,
                buildType: 'gulp',
                imageName: 'example_image_name',
                test: 'test.dockerfile',
                testMounts: '-v test:test'
        )

        //Assert
        assertStringArray([
                '   package_candidate.run()',
                '   package_candidate.call({gitflow=models.Gitflow@1a2e2935, docker_helper=models.Docker@1b9ea3e3, buildType=gulp, imageName=example_image_name, test=test.dockerfile, testMounts=-v test:test})',
                '      package_candidate.sh({script=mvn help:evaluate -Dexpression=project.version -q -DforceStdout, returnStdout=true})',
                '      package_candidate.stage(Docker Candidate Build, groovy.lang.Closure)',
                '         package_candidate.sh(docker build -t index.docker.io/example_image_name:1.0.0-release-candidate .)',
                '      package_candidate.stage(Prepare project for next iteration, groovy.lang.Closure)',
                '         package_candidate.sh(git tag -a 1.0.0 -m "Release 1.0.0")',
                '      package_candidate.stage(Push Updates, groovy.lang.Closure)',
                '         package_candidate.sh(docker push index.docker.io/example_image_name:1.0.0-release-candidate)',
                '         package_candidate.sshagent({credentials=[ssh]}, groovy.lang.Closure)',
        ] as String[], helper.callStack)
        assertJobStatusSuccess()
    }

    @Test
    void should_execute_pipeline_successfully_and_follow_webpack_route() {
        //Arrange
        helper.registerAllowedMethod("sh", [Map.class], { c -> "1.0.0" })
        helper.registerAllowedMethod("sshagent", [Map.class, Closure.class], { c -> "Not required" })
        def script = [
                sh           : {
                    return "1.0.1"
                },
                string       : {
                    return ""
                },
                build        : {
                    return [
                            number: '12345'
                    ]
                },
                specific     : {
                    return ""
                },
                copyArtifacts: {
                    return ""
                }
        ]
        def gitflow = new Gitflow(
                script: script,
                source: "hotfix/test",
                is_pull_request: false
        )
        def docker = new Docker(
                script: script,
                gitflow: gitflow
        )

        //Act
        runScript(pipeline).call(
                gitflow: gitflow,
                docker_helper: docker,
                buildType: 'webpack',
                imageName: 'example_image_name',
                test: 'test.dockerfile',
                testMounts: '-v test:test'
        )

        //Assert
        assertStringArray([
                '   package_candidate.run()',
                '   package_candidate.call({gitflow=models.Gitflow@1a2e2935, docker_helper=models.Docker@1b9ea3e3, buildType=webpack, imageName=example_image_name, test=test.dockerfile, testMounts=-v test:test})',
                '      package_candidate.sh({script=mvn help:evaluate -Dexpression=project.version -q -DforceStdout, returnStdout=true})',
                '      package_candidate.stage(Docker Candidate Build, groovy.lang.Closure)',
                '         package_candidate.sh(docker build -t index.docker.io/example_image_name:1.0.0-release-candidate .)',
                '      package_candidate.stage(Prepare project for next iteration, groovy.lang.Closure)',
                '         package_candidate.sh(git tag -a 1.0.0 -m "Release 1.0.0")',
                '      package_candidate.stage(Push Updates, groovy.lang.Closure)',
                '         package_candidate.sh(docker push index.docker.io/example_image_name:1.0.0-release-candidate)',
                '         package_candidate.sshagent({credentials=[ssh]}, groovy.lang.Closure)',
        ] as String[], helper.callStack)
        assertJobStatusSuccess()
    }
}
