package pipelines

import com.lesfurets.jenkins.unit.BasePipelineTest
import helpers.Pipeline
import models.Gitflow
import org.junit.Before
import org.junit.Test

import static helpers.CustomAssertHelper.assertStringArray

class ReleaseCandidateTests extends BasePipelineTest {

    private final String pipeline = "release_candidate.groovy"

    @Override
    @Before
    void setUp() throws Exception {
        scriptRoots += 'vars'
        super.setUp()
        Pipeline.setupLibrary(helper)
    }

    @Test
    void should_execute_pipeline_successfully_and_follow_master_route() throws Exception {
        //Arrange
        binding.setVariable("BRANCH_NAME", "feature/test")
        binding.setVariable("scm", [userRemoteConfigs: [[url: ["test"]]]])
        helper.registerAllowedMethod("sh", [Map.class], {c -> "EXAMPLE_TAG"})
        helper.registerAllowedMethod("withDockerRegistry", [Map.class, Closure.class], { c -> "Not required"})

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
}
