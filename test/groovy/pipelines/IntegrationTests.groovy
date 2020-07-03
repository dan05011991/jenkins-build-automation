package pipelines

import com.lesfurets.jenkins.unit.BasePipelineTest
import helpers.Pipeline
import models.Docker
import models.Gitflow
import org.junit.Before
import org.junit.Test

import static helpers.CustomAssertHelper.assertStringArray

class IntegrationTests extends BasePipelineTest {

    private final String pipeline = "integration_test.groovy"

    @Override
    @Before
    void setUp() throws Exception {
        scriptRoots += 'vars'
        super.setUp()
        Pipeline.setupLibrary(helper)

        // Common functions
        helper.registerAllowedMethod('pwd', [], { echo '/tmp/example' })
        helper.registerAllowedMethod('cleanWs', [], { echo 'Workspace cleaned' })
        helper.registerAllowedMethod('deleteDir', [], { echo 'Deleted Directory' })
        helper.registerAllowedMethod('git', [java.util.LinkedHashMap], { echo 'Git checkout' })
        helper.registerAllowedMethod('parallel', [Map.class], { echo 'Parallel job' })
    }

    @Test
    void should_execute_pipeline_successfully_and_follow_docker_in_maven_route() throws Exception {
        //Arrange
        binding.setVariable("BRANCH_NAME", "feature/test")
        binding.setVariable("scm", [userRemoteConfigs: [[url: ["test"]]]])
        def script = [
                sh: {
                    return "1.0.1"
                }
        ]
        def gitflow = new Gitflow(
                script: script,
                branch: "develop",
                is_pull_request: false
        )
        def docker_helper = new Docker(
                script: script,
                gitflow: gitflow
        )

        //Act
        runScript(pipeline).call(
                gitflow: gitflow,
                buildType: 'docker-in-maven',
                imageName: 'example_image_name',
                test: 'test.dockerfile',
                testMounts: '-v test:test',
                docker_helper: docker_helper
        )

        //Assert
        assertStringArray([
                '   integration_test.run()',
                '   integration_test.call({gitflow=models.Gitflow@00000000, buildType=docker-in-maven, imageName=example_image_name, test=test.dockerfile, testMounts=-v test:test, docker_helper=models.Docker@00000000})',
                '      integration_test.stage(Docker In Maven Build, groovy.lang.Closure)',
                '         integration_test.sh(docker build -t efe28da0-24ed-4253-a351-467f7587cb71 -f test.dockerfile .)',
                '         integration_test.sh(docker run --name 12345678-1234-1234-1234-123456789012 -v test:test 12345678-1234-1234-1234-123456789012)',
                '         integration_test.sh(docker cp $(docker ps -aqf "name=efe28da0-24ed-4253-a351-467f7587cb71"):/usr/webapp/target/surefire-reports .)',
                '         integration_test.junit(surefire-reports/**/*.xml)',
                '         integration_test.sh(docker rm -f efe28da0-24ed-4253-a351-467f7587cb71)',
                '         integration_test.sh(docker rmi efe28da0-24ed-4253-a351-467f7587cb71)'
        ] as String[], helper.callStack)
        assertJobStatusSuccess()
    }

    @Test
    void should_execute_pipeline_successfully_and_follow_maven_route() throws Exception {
        //Arrange
        binding.setVariable("BRANCH_NAME", "feature/test")
        binding.setVariable("scm", [userRemoteConfigs: [[url: ["test"]]]])
        def script = [
                sh: {
                    return "1.0.1"
                }
        ]
        def gitflow = new Gitflow(
                script: script,
                branch: "develop",
                is_pull_request: false
        )
        def docker_helper = new Docker(
                script: script,
                gitflow: gitflow
        )

        //Act
        runScript(pipeline).call(
                gitflow: gitflow,
                buildType: 'maven',
                imageName: 'example_image_name',
                test: 'test.dockerfile',
                docker_helper: docker_helper
        )

        //Assert
        assertStringArray([
                '   integration_test.run()',
                '   integration_test.call({gitflow=models.Gitflow@00000000, buildType=maven, imageName=example_image_name, test=test.dockerfile, docker_helper=models.Docker@00000000})',
                '      integration_test.stage(Maven Build, groovy.lang.Closure)',
                '         integration_test.sh(docker build -t efe28da0-24ed-4253-a351-467f7587cb71 -f test.dockerfile .)',
                '         integration_test.sh(docker run --name efe28da0-24ed-4253-a351-467f7587cb71 efe28da0-24ed-4253-a351-467f7587cb71 mvn surefire-report:report)',
                '         integration_test.sh(docker cp $(docker ps -aqf "name=efe28da0-24ed-4253-a351-467f7587cb71"):/usr/webapp/target/surefire-reports .)',
                '         integration_test.junit(surefire-reports/**/*.xml)',
                '         integration_test.sh(docker rm -f efe28da0-24ed-4253-a351-467f7587cb71)',
                '         integration_test.sh(docker rmi efe28da0-24ed-4253-a351-467f7587cb71)'
        ] as String[], helper.callStack)
        assertJobStatusSuccess()
    }

    @Test
    void should_execute_pipeline_successfully_and_follow_gulp_route() throws Exception {
        //Arrange
        binding.setVariable("BRANCH_NAME", "feature/test")
        binding.setVariable("scm", [userRemoteConfigs: [[url: ["test"]]]])
        def script = [
                sh: {
                    return '1.0.1'
                }
        ]
        def gitflow = new Gitflow(
                script: script,
                branch: "develop",
                is_pull_request: false
        )
        def docker_helper = new Docker(
                script: script,
                gitflow: gitflow
        )

        //Act
        runScript(pipeline).call(
                gitflow: gitflow,
                buildType: 'gulp',
                imageName: 'example_image_name',
                test: 'test.dockerfile',
                docker_helper: docker_helper
        )

        //Assert
        assertStringArray([
                '   integration_test.run()',
                '   integration_test.call({gitflow=models.Gitflow@00000000, buildType=gulp, imageName=example_image_name, test=test.dockerfile, docker_helper=models.Docker@00000000})',
                '      integration_test.stage(Gulp Build, groovy.lang.Closure)',
                '         integration_test.sh(docker build -t efe28da0-24ed-4253-a351-467f7587cb71 -f test.dockerfile .)',
                '         integration_test.sh(docker run --name efe28da0-24ed-4253-a351-467f7587cb71 efe28da0-24ed-4253-a351-467f7587cb71 ./node_modules/gulp/bin/gulp test)',
                '         integration_test.sh(docker cp $(docker ps -aqf "name=efe28da0-24ed-4253-a351-467f7587cb71"):/usr/webapp/tests/junit .)',
                '         integration_test.junit(junit/**/*.xml)',
                '         integration_test.sh(docker rm -f efe28da0-24ed-4253-a351-467f7587cb71)',
                '         integration_test.sh(docker rmi efe28da0-24ed-4253-a351-467f7587cb71)'
        ] as String[], helper.callStack)
        assertJobStatusSuccess()
    }

    @Test
    void should_execute_pipeline_successfully_and_follow_webpack_route() throws Exception {
        //Arrange
        binding.setVariable("BRANCH_NAME", "feature/test")
        binding.setVariable("scm", [userRemoteConfigs: [[url: ["test"]]]])
        def script = [
                sh: {
                    return "1.0.1"
                }
        ]
        def gitflow = new Gitflow(
                script: script,
                branch: "develop",
                is_pull_request: false
        )
        def docker_helper = new Docker(
                script: script,
                gitflow: gitflow
        )

        //Act
        runScript(pipeline).call(
                gitflow: gitflow,
                buildType: 'webpack',
                imageName: 'example_image_name',
                test: 'test.dockerfile',
                docker_helper: docker_helper
        )

        //Assert
        assertStringArray([
                '   integration_test.run()',
                '   integration_test.call({gitflow=models.Gitflow@00000000, buildType=webpack, imageName=example_image_name, test=test.dockerfile, docker_helper=models.Docker@00000000})',
                '      integration_test.stage(Webpack Build, groovy.lang.Closure)',
                '         integration_test.sh(docker build -t efe28da0-24ed-4253-a351-467f7587cb71 -f test.dockerfile .)',
                '         integration_test.sh(docker run --name efe28da0-24ed-4253-a351-467f7587cb71 efe28da0-24ed-4253-a351-467f7587cb71 npm test)',
                '         integration_test.sh(docker cp $(docker ps -aqf "name=efe28da0-24ed-4253-a351-467f7587cb71"):/usr/webapp/tests/junit .)',
                '         integration_test.junit(junit/**/*.xml)',
                '         integration_test.sh(docker rm -f efe28da0-24ed-4253-a351-467f7587cb71)',
                '         integration_test.sh(docker rmi efe28da0-24ed-4253-a351-467f7587cb71)'
        ] as String[], helper.callStack)
        assertJobStatusSuccess()
    }

    @Test
    void should_execute_pipeline_successfully_and_follow_pull_request_route() throws Exception {
        //Arrange
        binding.setVariable("BRANCH_NAME", "feature/test")
        binding.setVariable("scm", [userRemoteConfigs: [[url: ["test"]]]])
        helper.registerAllowedMethod("sh", [], {c -> "Not required"})
        helper.registerAllowedMethod("sh", [Map.class], {c -> "Not required"})
        def script = [
                sh: { Map<String, String> items ->
                    if(items['script'] == './get_parent_branch.sh') {
                        return 'test'
                    } else {
                        return '1.0.1'
                    }
                }
        ]
        def gitflow = new Gitflow(
                script: script,
                branch: "develop",
                is_pull_request: true
        )
        def docker_helper = new Docker(
                script: script,
                gitflow: gitflow
        )

        //Act
        runScript(pipeline).call(
                gitflow: gitflow,
                buildType: 'maven',
                imageName: 'example_image_name',
                test: 'test.dockerfile',
                docker_helper: docker_helper
        )

        //Assert
        assertStringArray([
                '   integration_test.run()',
                '   integration_test.call({gitflow=models.Gitflow@00000000, buildType=maven, imageName=example_image_name, test=test.dockerfile, docker_helper=models.Docker@00000000})',
                '      integration_test.echo(Lookahead merge from base branch test to develop)',
                '      integration_test.sh(\n                git checkout test\n                git pull origin test\n                git checkout develop\n                git merge test\n            )',
                '      integration_test.stage(Maven Build, groovy.lang.Closure)',
                '         integration_test.sh(docker build -t 7b43bb7f-b75b-484c-858b-96bf45bc9e32 -f test.dockerfile .)',
                '         integration_test.sh(docker run --name 7b43bb7f-b75b-484c-858b-96bf45bc9e32 7b43bb7f-b75b-484c-858b-96bf45bc9e32 mvn surefire-report:report)',
                '         integration_test.sh(docker cp $(docker ps -aqf "name=7b43bb7f-b75b-484c-858b-96bf45bc9e32"):/usr/webapp/target/surefire-reports .)',
                '         integration_test.junit(surefire-reports/**/*.xml)',
                '         integration_test.sh(docker rm -f 7b43bb7f-b75b-484c-858b-96bf45bc9e32)',
                '         integration_test.sh(docker rmi 7b43bb7f-b75b-484c-858b-96bf45bc9e32)'
        ] as String[], helper.callStack)
        assertJobStatusSuccess()
    }
}
