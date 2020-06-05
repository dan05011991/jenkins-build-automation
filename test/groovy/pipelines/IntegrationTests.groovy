package pipelines

import com.lesfurets.jenkins.unit.BasePipelineTest
import models.Gitflow
import org.junit.Before
import org.junit.Test

import static com.lesfurets.jenkins.unit.global.lib.LibraryConfiguration.library
import static com.lesfurets.jenkins.unit.global.lib.ProjectSource.projectSource
import static helpers.CustomAssertHelper.assertStringArray

class IntegrationTests extends BasePipelineTest {

    private final String pipeline = "integration_test.groovy"

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
    void should_execute_pipeline_successfully_and_follow_maven_route() throws Exception {
        //Arrange
        binding.setVariable("BRANCH_NAME", "feature/test")
        binding.setVariable("scm", [userRemoteConfigs: [[url: ["test"]]]])

        //Act
        runScript(pipeline).call(
                gitflow: new Gitflow(
                        script: this,
                        branch: "develop",
                        is_pull_request: false
                ),
                buildType: 'maven',
                imageName: 'example_image_name',
                test: 'test.dockerfile'
        )

        //Assert
        assertStringArray([
                '   integration_test.run()',
                '   integration_test.call({gitflow=models.Gitflow@4628b1d3, buildType=maven, imageName=example_image_name, test=test.dockerfile})',
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

        //Act
        runScript(pipeline).call(
                gitflow: new Gitflow(
                        script: this,
                        branch: "develop",
                        is_pull_request: false
                ),
                buildType: 'gulp',
                imageName: 'example_image_name',
                test: 'test.dockerfile'
        )

        //Assert
        assertStringArray([
                '   integration_test.run()',
                '   integration_test.call({gitflow=models.Gitflow@4628b1d3, buildType=gulp, imageName=example_image_name, test=test.dockerfile})',
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

        //Act
        runScript(pipeline).call(
                gitflow: new Gitflow(
                        script: this,
                        branch: "develop",
                        is_pull_request: false
                ),
                buildType: 'webpack',
                imageName: 'example_image_name',
                test: 'test.dockerfile'
        )

        //Assert
        assertStringArray([
                '   integration_test.run()',
                '   integration_test.call({gitflow=models.Gitflow@4628b1d3, buildType=webpack, imageName=example_image_name, test=test.dockerfile})',
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
    void should_execute_pipeline_successfully_and_follow_master_route() throws Exception {
        //Arrange
        binding.setVariable("BRANCH_NAME", "feature/test")
        binding.setVariable("scm", [userRemoteConfigs: [[url: ["test"]]]])
        helper.registerAllowedMethod("sh", [Map.class], {c -> "EXAMPLE_TAG"})
        helper.registerAllowedMethod("withDockerRegistry", [Map.class, Closure.class], {c -> "Not required"})

        //Act
        runScript(pipeline).call(
                gitflow: new Gitflow(
                        script: this,
                        branch: "master",
                        is_pull_request: false
                ),
                buildType: 'gulp',
                imageName: 'example_image_name',
                test: 'test.dockerfile'
        )

        //Assert
        assertStringArray([
                '   integration_test.run()',
                '   integration_test.call({gitflow=models.Gitflow@263f04ca, buildType=gulp, imageName=example_image_name, test=test.dockerfile})',
                '      integration_test.stage(Gulp Build, groovy.lang.Closure)',
                '         integration_test.sh(docker build -t 8027a88d-134d-410a-8c35-40a7e9798fbb -f test.dockerfile .)',
                '         integration_test.sh(docker run --name 8027a88d-134d-410a-8c35-40a7e9798fbb 8027a88d-134d-410a-8c35-40a7e9798fbb ./node_modules/gulp/bin/gulp test)',
                '         integration_test.sh(docker cp $(docker ps -aqf "name=8027a88d-134d-410a-8c35-40a7e9798fbb"):/usr/webapp/tests/junit .)',
                '         integration_test.junit(junit/**/*.xml)',
                '         integration_test.sh(docker rm -f 8027a88d-134d-410a-8c35-40a7e9798fbb)',
                '         integration_test.sh(docker rmi 8027a88d-134d-410a-8c35-40a7e9798fbb)',
                '      integration_test.stage(Re-tag Docker Image, groovy.lang.Closure)',
                '         integration_test.sh({script=git describe --tags | sed -n -e "s/\\([0-9]\\)-.*/\\1/ p", returnStdout=true})',
                '         integration_test.sh({script=docker pull example_image_name:EXAMPLE_TAG})'
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

        //Act
        runScript(pipeline).call(
                gitflow: new Gitflow(
                        script: [
                                sh: {
                                    return "test"
                                }
                        ],
                        branch: "develop",
                        is_pull_request: true
                ),
                buildType: 'maven',
                imageName: 'example_image_name',
                test: 'test.dockerfile'
        )

        //Assert
        assertStringArray([
                '   integration_test.run()',
                '   integration_test.call({gitflow=models.Gitflow@8ab78bc, buildType=maven, imageName=example_image_name, test=test.dockerfile})',
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
