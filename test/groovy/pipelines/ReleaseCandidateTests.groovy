package pipelines

import com.lesfurets.jenkins.unit.BasePipelineTest
import helpers.Pipeline
import models.Docker
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

        def gitflow = new Gitflow(
                script: this,
                branch: "master",
                is_pull_request: false
        )
        def docker_helper = new Docker(
                script: this,
                gitflow: gitflow
        )
        //Act
        runScript(pipeline).call(
                buildType: 'gulp',
                imageName: 'example_image_name',
                test: 'test.dockerfile',
                docker_helper: docker_helper
        )


        //Assert
        assertStringArray([
                '   release_candidate.run()',
                '   release_candidate.call({buildType=gulp, imageName=example_image_name, test=test.dockerfile, docker_helper=models.Docker@410954b})',
                '      release_candidate.stage(Re-tag Docker Image, groovy.lang.Closure)',
                '         release_candidate.sh({script=git describe --tags | sed -n -e "s/\\([0-9]\\)-.*/\\1/ p", returnStdout=true})',
                '         release_candidate.sh(docker pull hub.docker.com/example_image_name:EXAMPLE_TAG-release-candidate)',
                '         release_candidate.sh(docker tag hub.docker.com/example_image_name:EXAMPLE_TAG-release-candidate hub.docker.com/example_image_name:EXAMPLE_TAG)',
                '         release_candidate.sh(docker push hub.docker.com/example_image_name:EXAMPLE_TAG)'
        ] as String[], helper.callStack)
        assertJobStatusSuccess()
    }
}
