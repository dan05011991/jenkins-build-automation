package pipelines

import com.lesfurets.jenkins.unit.BasePipelineTest
import helpers.Constants
import helpers.CustomAssertHelper
import helpers.Pipeline
import org.junit.Before
import org.junit.Test

import static com.lesfurets.jenkins.unit.MethodCall.callArgsToString
import static helpers.CustomAssertHelper.assertStringArray
import static org.junit.Assert.*

class BuilderTests extends BasePipelineTest {

    private final String pipeline = "builder.groovy"

    @Override
    @Before
    void setUp() {
        scriptRoots += 'vars'
        super.setUp()
        Pipeline.setupLibrary(helper)

        // Mocking out external pipelines
        helper.registerAllowedMethod('update_project_version', [Map.class], { echo 'update_project_version pipeline called' })
        helper.registerAllowedMethod('integration_test', [Map.class], { echo 'Integration test pipeline called' })
        helper.registerAllowedMethod('package_candidate', [Map.class], { echo 'package_candidate pipeline called' })
        helper.registerAllowedMethod('release_candidate', [Map.class], { echo 'release_candidate pipeline called' })

        // Common functions
        helper.registerAllowedMethod('pwd', [], { echo '/tmp/example' })
        helper.registerAllowedMethod('cleanWs', [], { echo 'Workspace cleaned' })
        helper.registerAllowedMethod('deleteDir', [], { echo 'Deleted Directory' })
        helper.registerAllowedMethod('git', [java.util.LinkedHashMap], { echo 'Git checkout' })

        helper.registerAllowedMethod('parallel', [Map.class], { echo 'Parallel job' })
        helper.registerAllowedMethod('withCredentials', [List.class, Closure.class], { echo 'Parallel job' })

        binding.setVariable("scm", [ userRemoteConfigs: [[ url: ["test"]]]])
    }

    @Test
    void should_follow_integration_route_for_feature_branches() {
        //Arrange
        binding.setVariable("BRANCH_NAME", "feature/test")

        //Act
        runScript(pipeline).call(
                buildType: 'maven',
                deploymentRepo: 'example_url',
                imageName: 'example_image_name',
                test: 'test.dockerfile',
                testMounts: '-v test:test',
                projectKey: 'example_key'
        )

        //Assert
        assertStringArray([
                '   builder.run()',
                '   builder.call({buildType=maven, deploymentRepo=example_url, imageName=example_image_name, test=test.dockerfile, testMounts=-v test:test, projectKey=example_key})',
                '      builder.node(groovy.lang.Closure)',
                '         builder.disableConcurrentBuilds()',
                '         builder.logRotator({daysToKeepStr=7, numToKeepStr=5})',
                '         builder.buildDiscarder(null)',
                '         builder.properties([null, null])',
                '         builder.sh(git config --global user.email "jenkins@bnp.com")',
                '         builder.sh(git config --global user.name "Jenkins Admin")',
                '         builder.stage(Clean, groovy.lang.Closure)',
                '            builder.cleanWs()',
                '               builder.echo(Workspace cleaned)',
                '            builder.echo(Source branch: feature/test)',
                '            builder.echo(Target branch: null)',
                '            builder.echo(Source Url: [test])',
                '            builder.echo(Is Pull Request?: false)',
                '         builder.stage(Pipeline setup, groovy.lang.Closure)',
                '            builder.parallel({Checkout Project=groovy.lang.Closure, Create pipeline scripts=groovy.lang.Closure, Developer Docker login=groovy.lang.Closure, Release Docker login=groovy.lang.Closure})',
                '               builder.echo(Parallel job)',
                '         builder.update_project_version({projectKey=example_key, buildType=maven, gitflow=models.Gitflow@1b5bc39d})',
                '            builder.echo(update_project_version pipeline called)',
                '         builder.integration_test({imageName=example_image_name, buildType=maven, test=test.dockerfile, testMounts=-v test:test, gitflow=models.Gitflow@1b5bc39d, docker_helper=models.Docker@34abdee4})',
                '            builder.echo(Integration test pipeline called)'
        ] as String[], helper.callStack)
        assertJobStatusSuccess()
    }

    @Test
    void should_follow_integration_route_for_bugfix_branches() {
        //Arrange
        binding.setVariable("BRANCH_NAME", "bugfix/test")

        //Act
        runScript(pipeline).call(
                buildType: 'maven',
                deploymentRepo: 'example_url',
                imageName: 'example_image_name',
                test: 'test.dockerfile',
                testMounts: '-v test:test',
                projectKey: 'example_key'
        )

        //Assert
        assertStringArray([
                '   builder.run()',
                '   builder.call({buildType=maven, deploymentRepo=example_url, imageName=example_image_name, test=test.dockerfile, testMounts=-v test:test, projectKey=example_key})',
                '      builder.node(groovy.lang.Closure)',
                '         builder.disableConcurrentBuilds()',
                '         builder.logRotator({daysToKeepStr=7, numToKeepStr=5})',
                '         builder.buildDiscarder(null)',
                '         builder.properties([null, null])',
                '         builder.sh(git config --global user.email "jenkins@bnp.com")',
                '         builder.sh(git config --global user.name "Jenkins Admin")',
                '         builder.stage(Clean, groovy.lang.Closure)',
                '            builder.cleanWs()',
                '               builder.echo(Workspace cleaned)',
                '            builder.echo(Source branch: bugfix/test)',
                '            builder.echo(Target branch: null)',
                '            builder.echo(Source Url: [test])',
                '            builder.echo(Is Pull Request?: false)',
                '         builder.stage(Pipeline setup, groovy.lang.Closure)',
                '            builder.parallel({Checkout Project=groovy.lang.Closure, Create pipeline scripts=groovy.lang.Closure, Developer Docker login=groovy.lang.Closure, Release Docker login=groovy.lang.Closure})',
                '               builder.echo(Parallel job)',
                '         builder.update_project_version({projectKey=example_key, buildType=maven, gitflow=models.Gitflow@1b5bc39d})',
                '            builder.echo(update_project_version pipeline called)',
                '         builder.integration_test({imageName=example_image_name, buildType=maven, test=test.dockerfile, testMounts=-v test:test, gitflow=models.Gitflow@1b5bc39d, docker_helper=models.Docker@34abdee4})',
                '            builder.echo(Integration test pipeline called)'
        ] as String[], helper.callStack)
        assertJobStatusSuccess()
    }

    @Test
    void should_follow_release_route() {
        //Arrange
        binding.setVariable("BRANCH_NAME", "release/test")
        helper.registerAllowedMethod("sh", [Map.class], {c -> "this is not a bump"})

        //Act
        runScript(pipeline).call(
                buildType: 'maven',
                deploymentRepo: 'example_url',
                imageName: 'example_image_name',
                test: 'test.dockerfile',
                testMounts: '-v test:test',
                projectKey: 'example_key'
        )

        //Assert
        assertStringArray([
                '   builder.run()',
                '   builder.call({buildType=maven, deploymentRepo=example_url, imageName=example_image_name, test=test.dockerfile, testMounts=-v test:test, projectKey=example_key})',
                '      builder.node(groovy.lang.Closure)',
                '         builder.disableConcurrentBuilds()',
                '         builder.logRotator({daysToKeepStr=7, numToKeepStr=5})',
                '         builder.buildDiscarder(null)',
                '         builder.properties([null, null])',
                '         builder.sh(git config --global user.email "jenkins@bnp.com")',
                '         builder.sh(git config --global user.name "Jenkins Admin")',
                '         builder.stage(Clean, groovy.lang.Closure)',
                '            builder.cleanWs()',
                '               builder.echo(Workspace cleaned)',
                '            builder.echo(Source branch: release/test)',
                '            builder.echo(Target branch: null)',
                '            builder.echo(Source Url: [test])',
                '            builder.echo(Is Pull Request?: false)',
                '         builder.stage(Pipeline setup, groovy.lang.Closure)',
                '            builder.parallel({Checkout Project=groovy.lang.Closure, Create pipeline scripts=groovy.lang.Closure, Developer Docker login=groovy.lang.Closure, Release Docker login=groovy.lang.Closure})',
                '               builder.echo(Parallel job)',
                '         builder.sh({script=git log -1, returnStdout=true})',
                '         builder.sh({script=git log -1, returnStdout=true})',
                '         builder.update_project_version({projectKey=example_key, buildType=maven, gitflow=models.Gitflow@7e0aadd0})',
                '            builder.echo(update_project_version pipeline called)',
                '         builder.sh({script=git log -1, returnStdout=true})',
                '         builder.sh({script=git log -1, returnStdout=true})',
                '         builder.package_candidate({buildType=maven, imageName=example_image_name, test=test.dockerfile, testMounts=-v test:test, docker_helper=models.Docker@21362712})',
                '            builder.echo(package_candidate pipeline called)'
        ] as String[], helper.callStack)
        assertJobStatusSuccess()
    }

    @Test
    void should_follow_hotfix_route() {
        //Arrange
        binding.setVariable("BRANCH_NAME", "hotfix/test")
        helper.registerAllowedMethod("sh", [Map.class], {c -> "this is not a bump"})

        //Act
        runScript(pipeline).call(
                buildType: 'maven',
                deploymentRepo: 'example_url',
                imageName: 'example_image_name',
                test: 'test.dockerfile',
                testMounts: '-v test:test',
                projectKey: 'example_key'
        )

        //Assert
        assertStringArray([
                '   builder.run()',
                '   builder.call({buildType=maven, deploymentRepo=example_url, imageName=example_image_name, test=test.dockerfile, testMounts=-v test:test, projectKey=example_key})',
                '      builder.node(groovy.lang.Closure)',
                '         builder.disableConcurrentBuilds()',
                '         builder.logRotator({daysToKeepStr=7, numToKeepStr=5})',
                '         builder.buildDiscarder(null)',
                '         builder.properties([null, null])',
                '         builder.sh(git config --global user.email \"jenkins@bnp.com\")',
                '         builder.sh(git config --global user.name \"Jenkins Admin\")',
                '         builder.stage(Clean, groovy.lang.Closure)',
                '            builder.cleanWs()',
                '               builder.echo(Workspace cleaned)',
                '            builder.echo(Source branch: hotfix/test)',
                '            builder.echo(Target branch: null)',
                '            builder.echo(Source Url: [test])',
                '            builder.echo(Is Pull Request?: false)',
                '         builder.stage(Pipeline setup, groovy.lang.Closure)',
                '            builder.parallel({Checkout Project=groovy.lang.Closure, Create pipeline scripts=groovy.lang.Closure, Developer Docker login=groovy.lang.Closure, Release Docker login=groovy.lang.Closure})',
                '               builder.echo(Parallel job)',
                '         builder.sh({script=git log -1, returnStdout=true})',
                '         builder.sh({script=git log -1, returnStdout=true})',
                '         builder.sh({script=git fetch --prune})',
                '         builder.sh({script=\n                if [ \"\$(git diff origin/master 2> /dev/null)\" ]; then \n                    echo \"yes\"; \n                fi\n            , returnStdout=true})',
                '         builder.sh({script=git log -1, returnStdout=true})',
                '         builder.sh({script=git fetch --prune})',
                '         builder.sh({script=\n                if [ \"\$(git diff origin/master 2> /dev/null)\" ]; then \n                    echo \"yes\"; \n                fi\n            , returnStdout=true})',
                '         builder.integration_test({imageName=example_image_name, buildType=maven, test=test.dockerfile, testMounts=-v test:test, gitflow=models.Gitflow@2970a5bc, docker_helper=models.Docker@50305a})',
                '            builder.echo(Integration test pipeline called)',
                '         builder.sh({script=git log -1, returnStdout=true})',
                '         builder.sh({script=git fetch --prune})',
                '         builder.sh({script=\n                if [ \"\$(git diff origin/master 2> /dev/null)\" ]; then \n                    echo \"yes\"; \n                fi\n            , returnStdout=true})'
        ] as String[], helper.callStack)
        assertJobStatusSuccess()
    }

    @Test
    void should_follow_master_route() {
        //Arrange
        binding.setVariable("BRANCH_NAME", "master")
        helper.registerAllowedMethod("sh", [Map.class], {c -> "this is not a bump"})

        //Act
        runScript(pipeline).call(
                buildType: 'maven',
                deploymentRepo: 'example_url',
                imageName: 'example_image_name',
                test: 'test.dockerfile',
                testMounts: '-v test:test',
                projectKey: 'example_key'
        )

        //Assert
        assertStringArray([
                '   builder.run()',
                '   builder.call({buildType=maven, deploymentRepo=example_url, imageName=example_image_name, test=test.dockerfile, testMounts=-v test:test, projectKey=example_key})',
                '      builder.node(groovy.lang.Closure)',
                '         builder.disableConcurrentBuilds()',
                '         builder.logRotator({daysToKeepStr=7, numToKeepStr=5})',
                '         builder.buildDiscarder(null)',
                '         builder.properties([null, null])',
                '         builder.sh(git config --global user.email "jenkins@bnp.com")',
                '         builder.sh(git config --global user.name "Jenkins Admin")',
                '         builder.stage(Clean, groovy.lang.Closure)',
                '            builder.cleanWs()',
                '               builder.echo(Workspace cleaned)',
                '            builder.echo(Source branch: master)',
                '            builder.echo(Target branch: null)',
                '            builder.echo(Source Url: [test])',
                '            builder.echo(Is Pull Request?: false)',
                '         builder.stage(Pipeline setup, groovy.lang.Closure)',
                '            builder.parallel({Checkout Project=groovy.lang.Closure, Create pipeline scripts=groovy.lang.Closure, Developer Docker login=groovy.lang.Closure, Release Docker login=groovy.lang.Closure})',
                '               builder.echo(Parallel job)',
                '         builder.integration_test({imageName=example_image_name, buildType=maven, test=test.dockerfile, testMounts=-v test:test, gitflow=models.Gitflow@00000000, docker_helper=models.Docker@00000000})',
                '            builder.echo(Integration test pipeline called)',
                '         builder.release_candidate({imageName=example_image_name, docker_helper=models.Docker@21362712})',
                '            builder.echo(release_candidate pipeline called)'
        ] as String[], helper.callStack)
        assertJobStatusSuccess()
    }

    @Test
    void should_follow_develop_route() {
        //Arrange
        binding.setVariable("BRANCH_NAME", "develop")
        helper.registerAllowedMethod("sh", [Map.class], {c -> "this is not a bump"})

        //Act
        runScript(pipeline).call(
                buildType: 'maven',
                deploymentRepo: 'example_url',
                imageName: 'example_image_name',
                test: 'test.dockerfile',
                testMounts: '-v test:test',
                projectKey: 'example_key'
        )

        //Assert
        assertStringArray([
                '   builder.run()',
                '   builder.call({buildType=maven, deploymentRepo=example_url, imageName=example_image_name, test=test.dockerfile, testMounts=-v test:test, projectKey=example_key})',
                '      builder.node(groovy.lang.Closure)',
                '         builder.disableConcurrentBuilds()',
                '         builder.logRotator({daysToKeepStr=7, numToKeepStr=5})',
                '         builder.buildDiscarder(null)',
                '         builder.properties([null, null])',
                '         builder.sh(git config --global user.email "jenkins@bnp.com")',
                '         builder.sh(git config --global user.name "Jenkins Admin")',
                '         builder.stage(Clean, groovy.lang.Closure)',
                '            builder.cleanWs()',
                '               builder.echo(Workspace cleaned)',
                '            builder.echo(Source branch: develop)',
                '            builder.echo(Target branch: null)',
                '            builder.echo(Source Url: [test])',
                '            builder.echo(Is Pull Request?: false)',
                '         builder.stage(Pipeline setup, groovy.lang.Closure)',
                '            builder.parallel({Checkout Project=groovy.lang.Closure, Create pipeline scripts=groovy.lang.Closure, Developer Docker login=groovy.lang.Closure, Release Docker login=groovy.lang.Closure})',
                '               builder.echo(Parallel job)',
                '         builder.integration_test({imageName=example_image_name, buildType=maven, test=test.dockerfile, testMounts=-v test:test, gitflow=models.Gitflow@00000000, docker_helper=models.Docker@00000000})',
                '            builder.echo(Integration test pipeline called)',
        ] as String[], helper.callStack)
        assertJobStatusSuccess()
    }

    @Test
    void should_follow_pull_request_route_for_feature_branch() {
        //Arrange
        binding.setVariable("BRANCH_NAME", "PR-13")
        binding.setVariable("CHANGE_BRANCH", "feature/test")
        binding.setVariable("CHANGE_TARGET", "develop")
        helper.registerAllowedMethod("sh", [Map.class], {c -> "this is not a bump"})

        //Act
        runScript(pipeline).call(
                buildType: 'maven',
                deploymentRepo: 'example_url',
                imageName: 'example_image_name',
                test: 'test.dockerfile',
                testMounts: '-v test:test',
                projectKey: 'example_key'
        )

        //Assert
        assertStringArray([
                '   builder.run()',
                '   builder.call({buildType=maven, deploymentRepo=example_url, imageName=example_image_name, test=test.dockerfile, testMounts=-v test:test, projectKey=example_key})',
                '      builder.node(groovy.lang.Closure)',
                '         builder.disableConcurrentBuilds()',
                '         builder.logRotator({daysToKeepStr=7, numToKeepStr=5})',
                '         builder.buildDiscarder(null)',
                '         builder.properties([null, null])',
                '         builder.sh(git config --global user.email "jenkins@bnp.com")',
                '         builder.sh(git config --global user.name "Jenkins Admin")',
                '         builder.stage(Clean, groovy.lang.Closure)',
                '            builder.cleanWs()',
                '               builder.echo(Workspace cleaned)',
                '            builder.echo(Source branch: feature/test)',
                '            builder.echo(Target branch: develop)',
                '            builder.echo(Source Url: [test])',
                '            builder.echo(Is Pull Request?: true)',
                '         builder.stage(Pipeline setup, groovy.lang.Closure)',
                '            builder.parallel({Checkout Project=groovy.lang.Closure, Create pipeline scripts=groovy.lang.Closure, Developer Docker login=groovy.lang.Closure, Release Docker login=groovy.lang.Closure})',
                '               builder.echo(Parallel job)',
                '         builder.update_project_version({projectKey=example_key, buildType=maven, gitflow=models.Gitflow@2970a5bc})',
                '            builder.echo(update_project_version pipeline called)',
                '         builder.integration_test({imageName=example_image_name, buildType=maven, test=test.dockerfile, testMounts=-v test:test, gitflow=models.Gitflow@2970a5bc, docker_helper=models.Docker@6d511b5f})',
                '            builder.echo(Integration test pipeline called)'
        ] as String[], helper.callStack)
        assertJobStatusSuccess()
    }

    @Test
    void should_follow_pull_request_route_for_bugfix_on_hotfix_branch() {
        //Arrange
        binding.setVariable("BRANCH_NAME", "PR-13")
        binding.setVariable("CHANGE_BRANCH", "bugfix/test")
        binding.setVariable("CHANGE_TARGET", "hotfix/test")
        helper.registerAllowedMethod("sh", [Map.class], {c -> "this is not a bump"})

        //Act
        runScript(pipeline).call(
                buildType: 'maven',
                deploymentRepo: 'example_url',
                imageName: 'example_image_name',
                test: 'test.dockerfile',
                testMounts: '-v test:test',
                projectKey: 'example_key'
        )

        //Assert
        assertStringArray([
                '   builder.run()',
                '   builder.call({buildType=maven, deploymentRepo=example_url, imageName=example_image_name, test=test.dockerfile, testMounts=-v test:test, projectKey=example_key})',
                '      builder.node(groovy.lang.Closure)',
                '         builder.disableConcurrentBuilds()',
                '         builder.logRotator({daysToKeepStr=7, numToKeepStr=5})',
                '         builder.buildDiscarder(null)',
                '         builder.properties([null, null])',
                '         builder.sh(git config --global user.email "jenkins@bnp.com")',
                '         builder.sh(git config --global user.name "Jenkins Admin")',
                '         builder.stage(Clean, groovy.lang.Closure)',
                '            builder.cleanWs()',
                '               builder.echo(Workspace cleaned)',
                '            builder.echo(Source branch: bugfix/test)',
                '            builder.echo(Target branch: hotfix/test)',
                '            builder.echo(Source Url: [test])',
                '            builder.echo(Is Pull Request?: true)',
                '         builder.stage(Pipeline setup, groovy.lang.Closure)',
                '            builder.parallel({Checkout Project=groovy.lang.Closure, Create pipeline scripts=groovy.lang.Closure, Developer Docker login=groovy.lang.Closure, Release Docker login=groovy.lang.Closure})',
                '               builder.echo(Parallel job)',
                '         builder.update_project_version({projectKey=example_key, buildType=maven, gitflow=models.Gitflow@2970a5bc})',
                '            builder.echo(update_project_version pipeline called)',
                '         builder.integration_test({imageName=example_image_name, buildType=maven, test=test.dockerfile, testMounts=-v test:test, gitflow=models.Gitflow@2970a5bc, docker_helper=models.Docker@6d511b5f})',
                '            builder.echo(Integration test pipeline called)'
        ] as String[], helper.callStack)
        assertJobStatusSuccess()
    }

    @Test
    void should_follow_pull_request_route_for_bugfix_on_release_branch() {
        //Arrange
        binding.setVariable("BRANCH_NAME", "PR-13")
        binding.setVariable("CHANGE_BRANCH", "bugfix/test")
        binding.setVariable("CHANGE_TARGET", "release/test")
        helper.registerAllowedMethod("sh", [Map.class], {c -> "this is not a bump"})

        //Act
        runScript(pipeline).call(
                buildType: 'maven',
                deploymentRepo: 'example_url',
                imageName: 'example_image_name',
                test: 'test.dockerfile',
                testMounts: '-v test:test',
                projectKey: 'example_key'
        )

        //Assert
        assertStringArray([
                '   builder.run()',
                '   builder.call({buildType=maven, deploymentRepo=example_url, imageName=example_image_name, test=test.dockerfile, testMounts=-v test:test, projectKey=example_key})',
                '      builder.node(groovy.lang.Closure)',
                '         builder.disableConcurrentBuilds()',
                '         builder.logRotator({daysToKeepStr=7, numToKeepStr=5})',
                '         builder.buildDiscarder(null)',
                '         builder.properties([null, null])',
                '         builder.sh(git config --global user.email "jenkins@bnp.com")',
                '         builder.sh(git config --global user.name "Jenkins Admin")',
                '         builder.stage(Clean, groovy.lang.Closure)',
                '            builder.cleanWs()',
                '               builder.echo(Workspace cleaned)',
                '            builder.echo(Source branch: bugfix/test)',
                '            builder.echo(Target branch: release/test)',
                '            builder.echo(Source Url: [test])',
                '            builder.echo(Is Pull Request?: true)',
                '         builder.stage(Pipeline setup, groovy.lang.Closure)',
                '            builder.parallel({Checkout Project=groovy.lang.Closure, Create pipeline scripts=groovy.lang.Closure, Developer Docker login=groovy.lang.Closure, Release Docker login=groovy.lang.Closure})',
                '               builder.echo(Parallel job)',
                '         builder.update_project_version({projectKey=example_key, buildType=maven, gitflow=models.Gitflow@2970a5bc})',
                '            builder.echo(update_project_version pipeline called)',
                '         builder.integration_test({imageName=example_image_name, buildType=maven, test=test.dockerfile, testMounts=-v test:test, gitflow=models.Gitflow@2970a5bc, docker_helper=models.Docker@6d511b5f})',
                '            builder.echo(Integration test pipeline called)'
        ] as String[], helper.callStack)
        assertJobStatusSuccess()
    }

    @Test(expected = Exception.class)
    void should_reject_branches_for_violating_gitflow() {
        //Arrange
        String[] branches_to_test = [
                "featurea/something",
                "releasea/something",
                "bugfixa/something",
                "hotfixa/something",
                "something",
                "developa",
                "mastera"
        ]
        String errorMessage = "Invalid branch syntax. Must follow standard GitFlow process"

        for(String branch : branches_to_test) {
            helper.clearCallStack()

            binding.setVariable("BRANCH_NAME", branch)

            //Act
            try {
                runScript(pipeline).call(
                        buildType: 'maven',
                        deploymentRepo: 'example_url',
                        imageName: 'example_image_name',
                        test: 'test.dockerfile',
                        testMounts: '-v test:test',
                        projectKey: 'example_key'
                )
            }
            catch (Exception ex) {
                //Assert
                assertEquals(errorMessage, ex.getMessage())
                throw ex
            }

            fail("Pipeline should throw exception")
        }
    }

    @Test
    void should_reject_branches_for_violating_gitflow_in_pull_request_scenario() {
        //Arrange
        List<Tuple> tests = new ArrayList<>()
        tests.add(new Tuple('hotfix/something', 'master'))
        tests.add(new Tuple('hotfix/something', 'develop'))
        tests.add(new Tuple('hotfix/something', 'release/test'))
        tests.add(new Tuple('hotfix/something', 'hotfix/test'))
        tests.add(new Tuple('hotfix/something', 'bugfix/test'))

        tests.add(new Tuple('release/something', 'master'))
        tests.add(new Tuple('release/something', 'develop'))
        tests.add(new Tuple('release/something', 'release/test'))
        tests.add(new Tuple('release/something', 'hotfix/test'))
        tests.add(new Tuple('release/something', 'bugfix/test'))

        tests.add(new Tuple('feature/something', 'master'))
        tests.add(new Tuple('feature/something', 'release/test'))
        tests.add(new Tuple('feature/something', 'hotfix/test'))
        tests.add(new Tuple('feature/something', 'bugfix/test'))

        tests.add(new Tuple('bugfix/something', 'master'))
        tests.add(new Tuple('bugfix/something', 'develop'))
        tests.add(new Tuple('bugfix/something', 'feature/test'))
        tests.add(new Tuple('bugfix/something', 'bugfix/test'))

        tests.add(new Tuple('master', 'develop'))
        tests.add(new Tuple('develop', 'master'))

        String errorMessage = "Invalid branch syntax. Must follow standard GitFlow process"

        for(String test : tests) {
            helper.clearCallStack()

            binding.setVariable("BRANCH_NAME", "PR-13")
            binding.setVariable("CHANGE_BRANCH", test[0])
            binding.setVariable("CHANGE_TARGET", test[1])

            //Act
            try {
                runScript(pipeline).call(
                        buildType: 'maven',
                        deploymentRepo: 'example_url',
                        imageName: 'example_image_name',
                        test: 'test.dockerfile',
                        testMounts: '-v test:test',
                        projectKey: 'example_key'
                )
            }
            catch (Exception ex) {
                //Assert
                assertEquals(errorMessage, ex.getMessage())
                continue
            }

            fail("Pipeline should throw exception")
        }
    }

    @Test
    void should_not_reject_branches() {
        //Arrange
        String[] branches_to_test = [
                "feature/something",
                "release/something",
                "bugfix/something",
                "hotfix/something",
                "develop",
                "master"
        ]
        helper.registerAllowedMethod("sh", [Map.class], {c -> "this is not a bump"})

        for (String branch : branches_to_test) {
            helper.clearCallStack()

            binding.setVariable("BRANCH_NAME", branch)

            //Act
            runScript(pipeline).call(
                    buildType: 'maven',
                    deploymentRepo: 'example_url',
                    imageName: 'example_image_name',
                    test: 'test.dockerfile',
                    testMounts: '-v test:test',
                    projectKey: 'example_key'
            )
        }

        //Assert
        assertJobStatusSuccess()
    }

    @Test
    void should_not_reject_branches_in_pull_request_scenario() {
        //Arrange
        List<Tuple> tests = new ArrayList<>()
        tests.add(new Tuple('feature/something', 'develop'))
        tests.add(new Tuple('bugfix/something', 'release/test'))
        tests.add(new Tuple('bugfix/something', 'hotfix/test'))
        helper.registerAllowedMethod("sh", [Map.class], {c -> "this is not a bump"})

        for (Tuple test : tests) {
            helper.clearCallStack()

            binding.setVariable("BRANCH_NAME", "PR-13")
            binding.setVariable("CHANGE_BRANCH", test[0])
            binding.setVariable("CHANGE_TARGET", test[1])

            //Act
            runScript(pipeline).call(
                    buildType: 'maven',
                    deploymentRepo: 'example_url',
                    imageName: 'example_image_name',
                    test: 'test.dockerfile',
                    testMounts: '-v test:test',
                    projectKey: 'example_key'
            )
        }

        //Assert
        assertJobStatusSuccess()
    }

    @Test
    void should_default_pull_request_flag_to_false() {
        //Arrange
        helper.registerAllowedMethod("sh", [Map.class], { c -> "this is not a bump" })
        binding.setVariable("BRANCH_NAME", "feature/test")

        //Act
        runScript(pipeline).call(
                buildType: 'maven',
                deploymentRepo: 'example_url',
                imageName: 'example_image_name',
                test: 'test.dockerfile',
                testMounts: '-v test:test',
                projectKey: 'example_key'
        )

        //Assert
        assertTrue("Pull request should be set to false", helper.callStack.findAll { call ->
            call.methodName == "echo"
        }.any { call ->
            callArgsToString(call).contains("Is Pull Request?: false")
        })
        assertJobStatusSuccess()
    }

    @Test
    void should_set_pull_request_flag_to_true() {
        //Arrange
        helper.registerAllowedMethod("sh", [Map.class], { c -> "this is not a bump" })
        binding.setVariable("BRANCH_NAME", "PR-12")
        binding.setVariable("CHANGE_BRANCH", "feature/dev")
        binding.setVariable("CHANGE_TARGET", "develop")

        //Act
        runScript(pipeline).call(
                buildType: 'maven',
                deploymentRepo: 'example_url',
                imageName: 'example_image_name',
                test: 'test.dockerfile',
                testMounts: '-v test:test',
                projectKey: 'example_key'
        )

        //Assert
        assertTrue("Pull request should be set to true", helper.callStack.findAll { call ->
            call.methodName == "echo"
        }.any { call ->
            callArgsToString(call).contains("Is Pull Request?: true")
        })
        assertJobStatusSuccess()
    }

    @Test
    void should_set_pull_request_flag_to_false() {
        //Arrange
        helper.registerAllowedMethod("sh", [Map.class], { c -> "this is not a bump" })
        binding.setVariable("BRANCH_NAME", "feature/dev")

        //Act
        runScript(pipeline).call(
                buildType: 'maven',
                deploymentRepo: 'example_url',
                imageName: 'example_image_name',
                test: 'test.dockerfile',
                testMounts: '-v test:test',
                projectKey: 'example_key'
        )

        //Assert
        assertTrue("Pull request should be set to false", helper.callStack.findAll { call ->
            call.methodName == "echo"
        }.any { call ->
            callArgsToString(call).contains("Is Pull Request?: false")
        })
        assertJobStatusSuccess()
    }

    @Test
    void should_exit_pipeline_for_bump_commit_for_specified_branches() {
        //Arrange
        helper.registerAllowedMethod("sh", [Map.class], { c -> Constants.bumpCommit })
        binding.setVariable("BRANCH_NAME", "release/dev")

        //Act
        runScript(pipeline).call(
                buildType: 'maven',
                deploymentRepo: 'example_url',
                imageName: 'example_image_name',
                test: 'test.dockerfile',
                testMounts: '-v test:test',
                projectKey: 'example_key'
        )

        //Assert
        assertStringArray([
                '   builder.run()',
                '   builder.call({buildType=maven, deploymentRepo=example_url, imageName=example_image_name, test=test.dockerfile, testMounts=-v test:test, projectKey=example_key})',
                '      builder.node(groovy.lang.Closure)',
                '         builder.disableConcurrentBuilds()',
                '         builder.logRotator({daysToKeepStr=7, numToKeepStr=5})',
                '         builder.buildDiscarder(null)',
                '         builder.properties([null, null])',
                '         builder.sh(git config --global user.email "jenkins@bnp.com")',
                '         builder.sh(git config --global user.name "Jenkins Admin")',
                '         builder.stage(Clean, groovy.lang.Closure)',
                '            builder.cleanWs()',
                '               builder.echo(Workspace cleaned)',
                '            builder.echo(Source branch: release/dev)',
                '            builder.echo(Target branch: null)',
                '            builder.echo(Source Url: [test])',
                '            builder.echo(Is Pull Request?: false)',
                '         builder.stage(Pipeline setup, groovy.lang.Closure)',
                '            builder.parallel({Checkout Project=groovy.lang.Closure, Create pipeline scripts=groovy.lang.Closure, Developer Docker login=groovy.lang.Closure, Release Docker login=groovy.lang.Closure})',
                '               builder.echo(Parallel job)',
                '         builder.sh({script=git log -1, returnStdout=true})',
                '         builder.echo(This is a bump commit build - exiting early)'
        ] as String[], helper.callStack)
        assertJobStatusSuccess()
    }

    @Test
    void should_not_exit_pipeline_for_bump_commits_for_unspecified_branches() {
        //Arrange
        helper.registerAllowedMethod("sh", [Map.class], { c -> Constants.bumpCommit })
        binding.setVariable("BRANCH_NAME", "feature/dev")

        //Act
        runScript(pipeline).call(
                buildType: 'maven',
                deploymentRepo: 'example_url',
                imageName: 'example_image_name',
                test: 'test.dockerfile',
                testMounts: '-v test:test',
                projectKey: 'example_key'
        )

        //Assert
        assertFalse("Bump should not be active for this branch", helper.callStack.findAll { call ->
            call.methodName == "echo"
        }.any { call ->
            callArgsToString(call).contains("This is a bump commit build - exiting early")
        })
        assertJobStatusSuccess()
    }

    @Test
    void should_not_exit_pipeline_for_bump_commits_for_pull_requests_from_bugfix_to_hotfix_branch() {
        //Arrange
        helper.registerAllowedMethod("sh", [Map.class], { c -> Constants.bumpCommit })
        binding.setVariable("BRANCH_NAME", "PR-13")
        binding.setVariable("CHANGE_BRANCH", "bugfix/dev")
        binding.setVariable("CHANGE_TARGET", "hotfix/test")

        //Act
        runScript(pipeline).call(
                buildType: 'maven',
                deploymentRepo: 'example_url',
                imageName: 'example_image_name',
                test: 'test.dockerfile',
                testMounts: '-v test:test',
                projectKey: 'example_key'
        )

        //Assert
        assertFalse("Bump should not be active for this branch", helper.callStack.findAll { call ->
            call.methodName == "echo"
        }.any { call ->
            callArgsToString(call).contains("This is a bump commit build - exiting early")
        })
        assertJobStatusSuccess()
    }

    @Test
    void should_not_exit_pipeline_for_bump_commits_for_pull_requests_from_bugfix_to_release_branch() {
        //Arrange
        helper.registerAllowedMethod("sh", [Map.class], { c -> Constants.bumpCommit })
        binding.setVariable("BRANCH_NAME", "PR-13")
        binding.setVariable("CHANGE_BRANCH", "bugfix/dev")
        binding.setVariable("CHANGE_TARGET", "release/test")

        //Act
        runScript(pipeline).call(
                buildType: 'maven',
                deploymentRepo: 'example_url',
                imageName: 'example_image_name',
                test: 'test.dockerfile',
                testMounts: '-v test:test',
                projectKey: 'example_key'
        )

        //Assert
        assertFalse("Bump should not be active for this branch", helper.callStack.findAll { call ->
            call.methodName == "echo"
        }.any { call ->
            callArgsToString(call).contains("This is a bump commit build - exiting early")
        })
        assertJobStatusSuccess()
    }

    @Test
    void should_call_integration_test_for_integration_branches() {
        //Arrange
        String[] branches_to_test = [
                "feature/something",
                "bugfix/something",
                "develop",
                "master"
        ]
        helper.registerAllowedMethod("sh", [Map.class], { c -> "something" })

        for (String branch : branches_to_test) {
            helper.clearCallStack()

            binding.setVariable("BRANCH_NAME", branch)

            //Act
            runScript(pipeline).call(
                    buildType: 'maven',
                    deploymentRepo: 'example_url',
                    imageName: 'example_image_name',
                    test: 'test.dockerfile',
                    testMounts: '-v test:test',
                    projectKey: 'example_key'
            )

            //Assert
            assertEquals(1, helper.callStack.findAll { call -> call.methodName == "integration_test" }.size())
            assertEquals(0, helper.callStack.findAll { call -> call.methodName == "package_candidate" }.size())

            assertTrue("Should call integration tests for branch " + branch, helper.callStack.findAll { call ->
                call.methodName == "integration_test"
            }.any { call ->
                return CustomAssertHelper.assertEquals(
                        "{imageName=example_image_name, buildType=maven, test=test.dockerfile, testMounts=-v test:test, gitflow=models.Gitflow@00000000, docker_helper=models.Docker@00000000}",
                        callArgsToString(call))
            })
            assertJobStatusSuccess()
        }
    }

    @Test
    void should_call_package_candidate_for_package_branches() {
        //Arrange
        String[] branches_to_test = [
                "release/something",
                "hotfix/something"
        ]
        helper.registerAllowedMethod("sh", [Map.class], { c -> "yes" })

        for (String branch : branches_to_test) {
            helper.clearCallStack()

            binding.setVariable("BRANCH_NAME", branch)

            //Act
            runScript(pipeline).call(
                    buildType: 'maven',
                    deploymentRepo: 'example_url',
                    imageName: 'example_image_name',
                    test: 'test.dockerfile',
                    testMounts: '-v test:test',
                    projectKey: 'example_key'
            )

            //Assert
            assertEquals(0, helper.callStack.findAll { call ->
                call.methodName == "integration_test"
            }.size())

            assertTrue("Should call package candidate for branch " + branch, helper.callStack.findAll { call ->
                call.methodName == "package_candidate"
            }.any { call ->
                return CustomAssertHelper.assertEquals(
                        "{buildType=maven, imageName=example_image_name, test=test.dockerfile, testMounts=-v test:test, docker_helper=models.Docker@00000000}",
                        callArgsToString(call))
            })
            assertJobStatusSuccess()
        }
    }

    @Test
    void should_not_call_package_candidate_for_non_package_branches() {
        //Arrange
        String[] branches_to_test = [
                "feature/something",
                "bugfix/something",
                "develop",
                "master"
        ]
        helper.registerAllowedMethod("sh", [Map.class], { c -> "something" })

        for (String branch : branches_to_test) {
            helper.clearCallStack()

            binding.setVariable("BRANCH_NAME", branch)

            //Act
            runScript(pipeline).call(
                    buildType: 'maven',
                    deploymentRepo: 'example_url',
                    imageName: 'example_image_name',
                    test: 'test.dockerfile',
                    testMounts: '-v test:test',
                    projectKey: 'example_key'
            )

            //Assert
            assertEquals(0, helper.callStack.findAll { call ->
                call.methodName == "package_candidate"
            }.size())

            assertJobStatusSuccess()
        }
    }

    @Test
    void should_create_script_file() {
        //Arrange
        helper.registerAllowedMethod("sh", [Map.class], { c -> "something" })
        helper.registerAllowedMethod('writeFile', [Map.class], { echo 'Write file' })

        //Act
        runScript(pipeline).createScript("test.sh")

        //Assert
        assertStringArray([
                '   builder.run()',
                '   builder.createScript(test.sh)',
                '      builder.echo(Creating pipeline script test.sh)',
                '      builder.libraryResource(com/pipeline/scripts/test.sh)',
                '      builder.writeFile({file=test.sh, text=#!/usr/bin/env bash\n})',
                '         builder.echo(Write file)',
                '      builder.sh(chmod +x test.sh)'
        ] as String[], helper.callStack)
    }
}
