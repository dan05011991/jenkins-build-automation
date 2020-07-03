package models

import com.lesfurets.jenkins.unit.BasePipelineTest
import helpers.Constants
import org.junit.Test

import static org.junit.Assert.*

class GitflowTests extends BasePipelineTest {

    @Test
    void should_return_true_when_branch_is_valid() throws Exception {
        //Arrange
        for(String good_branch : Constants.good_branches) {
            def gitflow = new Gitflow(branch: good_branch)

            //Act
            def result = gitflow.isValid()

            //Assert
            assertTrue(good_branch + " wasn't deemed valid", result)
        }
    }

    @Test
    void should_return_false_when_branch_is_invalid() throws Exception {
        //Arrange
        for(String bad_branch : Constants.bad_branches) {
            def gitflow = new Gitflow(branch: bad_branch)

            //Act
            def result = gitflow.isValid()

            //Assert
            assertFalse(bad_branch + " wasn't deemed invalid", result)
        }
    }

    @Test
    void should_test_when_branch_is_master_branch() {
        //Arrange
        def valid_test = new Gitflow(branch: "master")
        def invalid_test = new Gitflow(branch: "not_master")

        //Act / Assert
        assertTrue("Master branch should be identified", valid_test.isMasterBranch())
        assertFalse("Should not be master branch", invalid_test.isMasterBranch())
    }

    @Test
    void should_test_when_branch_is_develop_branch() {
        //Arrange
        def valid_test = new Gitflow(branch: "develop")
        def invalid_test = new Gitflow(branch: "not_develop")

        //Act / Assert
        assertTrue("Develop branch should be identified", valid_test.isDevelopBranch())
        assertFalse("Should not be develop branch", invalid_test.isDevelopBranch())
    }

    @Test
    void should_test_when_branch_is_hotfix_branch() {
        //Arrange
        def valid_test = new Gitflow(branch: "hotfix/test")
        def invalid_test = new Gitflow(branch: "feature/test")

        //Act / Assert
        assertTrue("Hotfix branch should be identified", valid_test.isHotfixBranch())
        assertFalse("Should not be hotfix branch", invalid_test.isHotfixBranch())
    }

    @Test
    void should_test_when_branch_is_release_branch() {
        //Arrange
        def valid_test = new Gitflow(branch: "release/test")
        def invalid_test = new Gitflow(branch: "feature/test")

        //Act / Assert
        assertTrue("Release branch should be identified", valid_test.isReleaseBranch())
        assertFalse("Should not be release branch", invalid_test.isReleaseBranch())
    }

    @Test
    void should_test_when_branch_is_bugfix_branch() {
        //Arrange
        def valid_test = new Gitflow(branch: "bugfix/test")
        def invalid_test = new Gitflow(branch: "feature/test")

        //Act / Assert
        assertTrue("Bugfix branch should be identified", valid_test.isBugfixBranch())
        assertFalse("Should not be bugfix branch", invalid_test.isBugfixBranch())
    }

    @Test
    void should_test_when_branch_is_feature_branch() {
        //Arrange
        def valid_test = new Gitflow(branch: "feature/test")
        def invalid_test = new Gitflow(branch: "bugfix/test")

        //Act / Assert
        assertTrue("Feature branch should be identified", valid_test.isFeatureBranch())
        assertFalse("Should not be feature branch", invalid_test.isFeatureBranch())
    }

    @Test
    void should_be_package_branch() {
        //Arrange
        def valid_branches = [
                "release/test",
                "hotfix/test"
        ]

        for(String branch : valid_branches) {
            def gitflow = new Gitflow(branch: branch)

            //Act / Assert
            assertTrue(branch + " should be package branch", gitflow.isPackageBranch())
        }
    }

    @Test
    void should_not_be_package_branch() {
        //Arrange
        def invalid_branches = [
                "master",
                "develop",
                "feature/test",
                "bugfix/test"
        ]

        for(String branch : invalid_branches) {
            def gitflow = new Gitflow(branch: branch)

            //Act / Assert
            assertFalse(branch + " should be package branch", gitflow.isPackageBranch())
        }
    }

    @Test
    void should_be_integration_branch() {
        //Arrange
        def valid_branches = [
                "feature/test",
                "bugfix/test"
        ]

        for(String branch : valid_branches) {
            def gitflow = new Gitflow(branch: branch)

            //Act / Assert
            assertTrue(branch + " should be integration branch", gitflow.isIntegrationBranch())
        }
    }

    @Test
    void should_not_be_integration_branch() {
        //Arrange
        def invalid_branches = [
                "master",
                "develop",
                "release/test",
                "hotfix/test"
        ]

        for(String branch : invalid_branches) {
            def gitflow = new Gitflow(branch: branch)

            //Act / Assert
            assertFalse(branch + " should be integration branch", gitflow.isIntegrationBranch())
        }
    }

    @Test
    void should_be_main_branch() {
        //Arrange
        def valid_branches = [
                "master",
                "develop"
        ]

        for(String branch : valid_branches) {
            def gitflow = new Gitflow(branch: branch)

            //Act / Assert
            assertTrue(branch + " should be main branch", gitflow.isMainBranch())
        }
    }

    @Test
    void should_not_be_main_branch() {
        //Arrange
        def invalid_branches = [
                "feature/test",
                "bugfix/test",
                "release/test",
                "hotfix/test"
        ]

        for(String branch : invalid_branches) {
            def gitflow = new Gitflow(branch: branch)

            //Act / Assert
            assertFalse(branch + " should be main branch", gitflow.isMainBranch())
        }
    }

    @Test
    void should_test_when_branch_is_pull_request() {
        //Arrange
        def valid_test = new Gitflow(branch: 'example', is_pull_request: true)
        def invalid_test = new Gitflow(branch: 'example', is_pull_request: false)

        //Act / Assert
        assertTrue("Should be pull request", valid_test.isPullRequest())
        assertFalse("Should not be pull request", invalid_test.isPullRequest())
    }

    @Test
    void should_return_source_branch() {
        //Arrange
        def test_branch = "test_branch"
        def gitflow = new Gitflow(branch: test_branch)

        //Act / Assert
        assertEquals("Branch set should be " + test_branch, test_branch, gitflow.getSourceBranch())
    }

    @Test
    void should_get_lookahead_branch_when_branch_is_set() {
        //Arrange
        def script_result = "test"
        def gitflow = new Gitflow(
                branch: 'example',
                script: [
                        sh: {
                            return script_result
                        }
                ]
        )

        //Act
        def result = gitflow.getLookaheadBranch()

        //Assert
        assertEquals("Should return script response of " + script_result, script_result, result)
    }

    @Test
    void should_trim_lookahead_branch_result() {
        //Arrange
        def script_result = "test       "
        def expected_result = "test"
        def gitflow = new Gitflow(
                branch: 'example',
                script: [
                        sh: {
                            return script_result
                        }
                ]
        )

        //Act
        def result = gitflow.getLookaheadBranch()

        //Assert
        assertEquals("Should return script response of " + expected_result, expected_result, result)
    }

    @Test
    void should_get_static_master_branch_as_lookahead_branch_for_package_branches() {
        //Arrange
        String[] branches = [
                'release/test',
                'hotfix/test'
        ]
        String expectedBranch = 'master'

        for(String branch : branches) {
            def gitflow = new Gitflow(
                    branch: branch
            )

            //Act
            def result = gitflow.getLookaheadBranch()

            //Assert
            assertEquals("Should return script response of " + expectedBranch, expectedBranch, result)
        }
    }

    @Test(expected = Exception.class)
    void should_throw_exception_when_getting_lookahead_branch_script_result_returns_empty_string() {
        //Arrange
        def errorMessage = "Unable to determine the parent branch"
        def gitflow = new Gitflow(
                script: [
                        sh: {
                            return ""
                        }
                ]
        )

        //Act
        try {
            gitflow.getLookaheadBranch()
        }
        catch (Exception ex) {
            //Assert
            assertEquals(errorMessage, ex.getMessage())
            throw ex
        }

        //Assert
        fail("Should not get to this point, exception should be thrown")
    }

    @Test
    void should_get_patch_increment_type_flag_for_hotfix_branch() {
        //Arrange
        def gitflow = new Gitflow(branch: "hotfix/test")

        //Act
        def result = gitflow.getIncrementType()

        //Assert
        assertEquals("Should return patch flag", "p", result)
    }

    @Test
    void should_get_minor_increment_type_flag_for_release_branch() {
        //Arrange
        def gitflow = new Gitflow(branch: "release/test")

        //Act
        def result = gitflow.getIncrementType()

        //Assert
        assertEquals("Should return minor flag", "m", result)
    }

    @Test(expected = Exception.class)
    void should_throw_exception_when_trying_to_get_update_flag_for_invalid_branch() {
        //Arrange
        def errorMessage = "Incorrect use of increment type function"
        def gitflow = new Gitflow(branch: "feature/test")

        //Act
        try {
            gitflow.getIncrementType()
        }
        catch (Exception ex) {
            //Assert
            assertEquals(errorMessage, ex.getMessage())
            throw ex
        }

        //Assert
        fail("Should not get to this point, exception should be thrown")
    }

    @Test
    void should_transform_feature_branch_name_into_new_version() {
        //Arrange
        String branch = 'feature/ID123-test_branch'
        String expected = 'feature_ID123-test-branch'
        def gitflow = new Gitflow(branch: branch)

        //Act
        String result = gitflow.getNextVersion(null, null)

        //Assert
        assertEquals(expected, result)
    }

    @Test
    void should_get_new_patch_release_number_for_hotfix_branch() {
        //Arrange
        String projectKey = "example_project_key"
        String releaseType = "p"
        String gitTag = "git_tag"
        String branch = "hotfix/test"
        String newVersion = "1.2.345"
        Map<String, String> jobResult = [
                number: newVersion
        ]

        def gitflow = new Gitflow(
                branch: branch,
                script: [
                        sh: {
                            return newVersion
                        },
                        build: {
                            return jobResult
                        },
                        copyArtifacts: {
                        },
                        specific: { number ->
                            assertEquals("Job is incorrect", newVersion, number.toString())
                        },
                        string: { Map<String, String> map ->
                            String name = map.get('name')
                            String value = map.get('value')

                            if(name != 'PROJECT_KEY' && name != 'RELEASE_TYPE' && name != 'GIT_TAG') {
                                throw new Exception("Invalid key for string function")
                            }

                            if(name == 'PROJECT_KEY') {
                                assertEquals("Should have correct project key: " + projectKey, projectKey, value)
                            }

                            if(name == 'RELEASE_TYPE') {
                                assertEquals("Should have release type: " + releaseType, releaseType, value)
                            }

                            if(name == 'GIT_TAG') {
                                assertEquals("Should have correct git tag: " + gitTag, gitTag, value)
                            }
                        }
                ]
        )

        //Act
        String result = gitflow.getNextVersion(projectKey, gitTag)

        //Assert
        assertEquals("Should return the correct version", result, newVersion)
    }

    @Test
    void should_get_new_minor_release_number_for_release_branch() {
        //Arrange
        String projectKey = "example_project_key"
        String releaseType = "m"
        String gitTag = "git_tag"
        String branch = "release/test"
        String newVersion = "1.2.345"
        Map<String, String> jobResult = [
                number: newVersion
        ]

        def gitflow = new Gitflow(
                branch: branch,
                script: [
                        sh: {
                            return newVersion
                        },
                        build: {
                            return jobResult
                        },
                        copyArtifacts: {
                        },
                        specific: { number ->
                            assertEquals("Job is incorrect", newVersion, number.toString())
                        },
                        string: { Map<String, String> map ->
                            String name = map.get('name')
                            String value = map.get('value')

                            if(name != 'PROJECT_KEY' && name != 'RELEASE_TYPE' && name != 'GIT_TAG') {
                                throw new Exception("Invalid key for string function")
                            }

                            if(name == 'PROJECT_KEY') {
                                assertEquals("Should have correct project key: " + projectKey, projectKey, value)
                            }

                            if(name == 'RELEASE_TYPE') {
                                assertEquals("Should have release type: " + releaseType, releaseType, value)
                            }

                            if(name == 'GIT_TAG') {
                                assertEquals("Should have correct git tag: " + gitTag, gitTag, value)
                            }
                        }
                ]
        )

        //Act
        String result = gitflow.getNextVersion(projectKey, gitTag)

        //Assert
        assertEquals("Should return the correct version", result, newVersion)
    }

    @Test
    void should_return_true_for_bump_commit() {
        //Arrange
        def gitflow = new Gitflow(
                branch: "feature/test",
                script: [
                        sh: {
                            return Constants.bumpCommit
                        }
                ]
        )

        //Act
        def result = gitflow.isBumpCommit()

        //Assert
        assertTrue('Should be a bump commit', result)
    }

    @Test
    void should_return_false_for_no_bump_commit() {
        //Arrange
        def gitflow = new Gitflow(
                branch: "feature/test",
                script: [
                        sh: {
                            return 'no'
                        }
                ]
        )

        //Act
        def result = gitflow.isBumpCommit()

        //Assert
        assertFalse('Should not be bump commit', result)
    }

    @Test
    void should_exit_build_for_release_branch_for_bump_commit() {
        //Arrange
        def gitflow = new Gitflow(
                branch: "release/test",
                is_pull_request: false,
                script: [
                        sh: {
                            return Constants.bumpCommit
                        }
                ]
        )

        //Act
        def result = gitflow.shouldExitBuild()

        //Assert
        assertTrue('Should exit build', result)
    }

    @Test
    void should_exit_build_for_hotfix_branch_for_bump_commit() {
        //Arrange
        def gitflow = new Gitflow(
                branch: "hotfix/test",
                is_pull_request: false,
                script: [
                        sh: {
                            return Constants.bumpCommit
                        }
                ]
        )

        //Act
        def result = gitflow.shouldExitBuild()

        //Assert
        assertTrue('Should exit build', result)
    }

    @Test
    void should_not_exit_build_if_not_package_branch() {
        //Arrange
        def gitflow = new Gitflow(
                branch: "bugfix/test",
                is_pull_request: false,
                script: [
                        sh: {
                            return Constants.bumpCommit
                        }
                ]
        )

        //Act
        def result = gitflow.shouldExitBuild()

        //Assert
        assertFalse('Should not exit build', result)
    }

    @Test
    void should_not_exit_build_if_pull_request() {
        //Arrange
        def gitflow = new Gitflow(
                branch: "release/test",
                is_pull_request: true,
                script: [
                        sh: {
                            return Constants.bumpCommit
                        }
                ]
        )

        //Act
        def result = gitflow.shouldExitBuild()

        //Assert
        assertFalse('Should exit build', result)
    }

    @Test
    void should_not_exit_build_if_not_bump_commit() {
        //Arrange
        def gitflow = new Gitflow(
                branch: "release/test",
                is_pull_request: false,
                script: [
                        sh: {
                            return 'something'
                        }
                ]
        )

        //Act
        def result = gitflow.shouldExitBuild()

        //Assert
        assertFalse('Should not exit build', result)
    }

    @Test
    void should_package_build_for_release_branch() {
        //Arrange
        def gitflow = new Gitflow(
                branch: "release/test",
                is_pull_request: false,
                script: [
                        sh: { Map<String, String> items ->
                            def script = items.get('script')
                            if(script.contains('log')) {
                                return 'not bump'
                            } else if(script.contains('diff')) {
                                return 'no'
                            } else if(script.contains('parent')) {
                                return 'not required'
                            } else if(script.contains('fetch')) {
                                return 'not required'
                            }
                            throw new Exception('Invalid use of sh')
                        }
                ]
        )

        //Act
        def result = gitflow.shouldPackageBuild()

        //Assert
        assertTrue('Should package release build', result)
        assertFalse('Should not run integration build', gitflow.shouldRunIntegrationTest())
    }

    @Test
    void should_package_build_for_hotfix_branch() {
        //Arrange
        def gitflow = new Gitflow(
                branch: "hotfix/test",
                is_pull_request: false,
                script: [
                        sh: { Map<String, String> items ->
                            def script = items.get('script')
                            if(script.contains('log')) {
                                return 'not bump'
                            } else if(script.contains('diff')) {
                                return 'yes'
                            } else if(script.contains('parent')) {
                                return 'not required'
                            } else if(script.contains('fetch')) {
                                return 'not required'
                            }
                            throw new Exception('Invalid use of sh')
                        }
                ]
        )

        //Act
        def result = gitflow.shouldPackageBuild()

        //Assert
        assertTrue('Should package release build', result)
        assertFalse('Should not run integration build', gitflow.shouldRunIntegrationTest())
    }

    @Test
    void should_not_package_build_if_not_package_branch() {
        //Arrange
        def gitflow = new Gitflow(
                branch: "feature/test",
                is_pull_request: false,
                script: [
                        sh: { Map<String, String> items ->
                            def script = items.get('script')
                            if(script.contains('log')) {
                                return 'not bump'
                            } else if(script.contains('diff')) {
                                return 'yes'
                            } else if(script.contains('parent')) {
                                return 'not required'
                            } else if(script.contains('fetch')) {
                                return 'not required'
                            }
                            throw new Exception('Invalid use of sh')
                        }
                ]
        )

        //Act
        def result = gitflow.shouldPackageBuild()

        //Assert
        assertFalse('Should not package release build', result)
    }

    @Test
    void should_not_package_build_if_pull_request() {
        //Arrange
        def gitflow = new Gitflow(
                branch: "hotfix/test",
                is_pull_request: true,
                script: [
                        sh: { Map<String, String> items ->
                            def script = items.get('script')
                            if(script.contains('log')) {
                                return 'not bump'
                            } else if(script.contains('diff')) {
                                return 'yes'
                            } else if(script.contains('parent')) {
                                return 'not required'
                            } else if(script.contains('fetch')) {
                                return 'not required'
                            }
                            throw new Exception('Invalid use of sh')
                        }
                ]
        )

        //Act
        def result = gitflow.shouldPackageBuild()

        //Assert
        assertFalse('Should not package release build', result)
    }

    @Test
    void should_not_package_build_if_bump_commit() {
        //Arrange
        def gitflow = new Gitflow(
                branch: "hotfix/test",
                is_pull_request: false,
                script: [
                        sh: { Map<String, String> items ->
                            def script = items.get('script')
                            if(script.contains('log')) {
                                return Constants.bumpCommit
                            } else if(script.contains('diff')) {
                                return 'yes'
                            } else if(script.contains('parent')) {
                                return 'not required'
                            } else if(script.contains('fetch')) {
                                return 'not required'
                            }
                            throw new Exception('Invalid use of sh')
                        }
                ]
        )

        //Act
        def result = gitflow.shouldPackageBuild()

        //Assert
        assertFalse('Should not package release build', result)
    }

    @Test
    void should_not_package_build_if_hotfix_branch_has_no_difference_to_parent() {
        //Arrange
        def gitflow = new Gitflow(
                branch: "hotfix/test",
                is_pull_request: false,
                script: [
                        sh: { Map<String, String> items ->
                            def script = items.get('script')
                            if(script.contains('log')) {
                                return 'not bump'
                            } else if(script.contains('diff')) {
                                return 'no'
                            } else if(script.contains('parent')) {
                                return 'not required'
                            } else if(script.contains('fetch')) {
                                return 'not required'
                            }
                            throw new Exception('Invalid use of sh')
                        }
                ]
        )

        //Act
        def result = gitflow.shouldPackageBuild()

        //Assert
        assertFalse('Should not package release build', result)
    }

    @Test
    void has_git_difference_to_parent() {
        //Arrange
        def gitflow = new Gitflow(
                branch: "hotfix/test",
                is_pull_request: false,
                script: [
                        sh: { Map<String, String> items ->
                            def script = items.get('script')
                            if(script.contains('diff')) {
                                return 'yes'
                            } else if(script.contains('parent')) {
                                return 'not required'
                            } else if(script.contains('fetch')) {
                                return 'not required'
                            }
                            throw new Exception('Invalid use of sh')
                        }
                ]
        )

        //Act
        def result = gitflow.hasGitDifferenceToParent()

        //Assert
        assertTrue('Should have difference', result)
    }

    @Test
    void does_not_have_git_difference_to_parent() {
        //Arrange
        def gitflow = new Gitflow(
                branch: "hotfix/test",
                is_pull_request: false,
                script: [
                        sh: { Map<String, String> items ->
                            def script = items.get('script')
                            if(script.contains('diff')) {
                                return 'no'
                            } else if(script.contains('parent')) {
                                return 'not required'
                            } else if(script.contains('fetch')) {
                                return 'not required'
                            }
                            throw new Exception('Invalid use of sh')
                        }
                ]
        )

        //Act
        def result = gitflow.hasGitDifferenceToParent()

        //Assert
        assertFalse('Should not have difference', result)
    }
}
