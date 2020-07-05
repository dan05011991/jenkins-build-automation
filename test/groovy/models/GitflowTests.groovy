package models

import com.lesfurets.jenkins.unit.BasePipelineTest
import helpers.Constants
import org.junit.Test
import org.springframework.util.Assert

import static org.junit.Assert.*

class GitflowTests extends BasePipelineTest {

    @Test
    void should_setup_gitflow_for_valid_branches() throws Exception {
        //Arrange
        for(String good_branch : Constants.good_branches) {
            //Act
            new Gitflow(source: good_branch)

            //Assert - here for readability
            Assert.isTrue(true)
        }
    }

    @Test
    void should_return_false_when_branch_is_invalid() throws Exception {
        //Arrange
        for(String bad_branch : Constants.bad_branches) {
            try {
                new Gitflow(source: bad_branch)
            } catch(Exception ex) {
                continue
            }
            // Here to cause a failed test
            Assert.isTrue(false)
        }
    }

    @Test
    void should_violate_pull_request_restrictions() throws Exception {
        //Arrange
        List<Tuple<String, String>> tests = new ArrayList<>()
        //Release
        tests.add(new Tuple('release/test', 'master'))
        tests.add(new Tuple('release/test', 'release/test2'))
        tests.add(new Tuple('release/test', 'feature/test'))
        tests.add(new Tuple('release/test', 'hotfix/test'))
        tests.add(new Tuple('release/test', 'bugfix/test'))

        //Hotfix
        tests.add(new Tuple('release/test', 'master'))
        tests.add(new Tuple('release/test', 'release/test2'))
        tests.add(new Tuple('release/test', 'feature/test'))
        tests.add(new Tuple('release/test', 'hotfix/test'))
        tests.add(new Tuple('release/test', 'bugfix/test'))

        //Feature
        tests.add(new Tuple('release/test', 'master'))
        tests.add(new Tuple('release/test', 'release/test2'))
        tests.add(new Tuple('release/test', 'feature/test'))
        tests.add(new Tuple('release/test', 'hotfix/test'))
        tests.add(new Tuple('release/test', 'bugfix/test'))

        //Bugfix
        tests.add(new Tuple('release/test', 'master'))
        tests.add(new Tuple('release/test', 'develop'))
        tests.add(new Tuple('release/test', 'feature/test'))
        tests.add(new Tuple('release/test', 'hotfix/test'))
        tests.add(new Tuple('release/test', 'bugfix/test'))


        for(Tuple test : tests) {
            try {
                //Act
                new Gitflow(source: test[0], target: test[1], is_pull_request: true)
            } catch(Exception ex) {
                //Assert
                continue
            }
            throw new Exception(String.format('Expected an exception to be thrown for source %s into target %s', test[0], test[1]))
        }
    }

    @Test
    void should_correctly_identify_master_branch() {
        //Arrange / Act / Assert
        assertTrue(new Gitflow(source: "master").isMasterBranch())
        assertTrue(new Gitflow(source: "develop").isMasterBranch('master'))
        assertFalse(new Gitflow(source: "develop").isMasterBranch())
    }

    @Test
    void should_correctly_identify_develop_branch() {
        //Arrange / Act / Assert
        assertTrue(new Gitflow(source: "develop").isDevelopBranch())
        assertTrue(new Gitflow(source: "master").isDevelopBranch('develop'))
        assertFalse(new Gitflow(source: "master").isDevelopBranch())
    }

    @Test
    void should_correctly_identify_hotfix_branch() {
        //Arrange / Act / Assert
        assertTrue(new Gitflow(source: "hotfix/test").isHotfixBranch())
        assertTrue(new Gitflow(source: "master").isHotfixBranch('hotfix/test'))
        assertFalse(new Gitflow(source: "master").isHotfixBranch())
    }

    @Test
    void should_correctly_identify_release_branch() {
        //Arrange / Act / Assert
        assertTrue(new Gitflow(source: "release/test").isReleaseBranch())
        assertTrue(new Gitflow(source: "master").isReleaseBranch('release/test'))
        assertFalse(new Gitflow(source: "master").isReleaseBranch())
    }

    @Test
    void should_correctly_identify_bugfix_branch() {
        //Arrange / Act / Assert
        assertTrue(new Gitflow(source: "bugfix/test").isBugfixBranch())
        assertTrue(new Gitflow(source: "master").isBugfixBranch('bugfix/test'))
        assertFalse(new Gitflow(source: "master").isBugfixBranch())
    }

    @Test
    void should_correctly_identify_feature_branch() {
        //Arrange / Act / Assert
        assertTrue(new Gitflow(source: "feature/test").isFeatureBranch())
        assertTrue(new Gitflow(source: "master").isFeatureBranch('feature/test'))
        assertFalse(new Gitflow(source: "master").isFeatureBranch())
    }

    @Test
    void should_be_package_branch() {
        //Arrange
        def valid_branches = [
                "release/test",
                "hotfix/test"
        ]

        for(String branch : valid_branches) {
            def gitflow = new Gitflow(source: branch)

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
            def gitflow = new Gitflow(source: branch)

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
            def gitflow = new Gitflow(source: branch)

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
            def gitflow = new Gitflow(source: branch)

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
            def gitflow = new Gitflow(source: branch)

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
            def gitflow = new Gitflow(source: branch)

            //Act / Assert
            assertFalse(branch + " should be main branch", gitflow.isMainBranch())
        }
    }

    @Test
    void should_test_when_branch_is_pull_request() {
        //Arrange
        def valid_test = new Gitflow(source: 'bugfix/test', target: 'release/test', is_pull_request: true)
        def invalid_test = new Gitflow(source: 'master', is_pull_request: false)

        //Act / Assert
        assertTrue("Should be pull request", valid_test.isPullRequest())
        assertFalse("Should not be pull request", invalid_test.isPullRequest())
    }

    @Test
    void should_return_source_branch() {
        //Arrange
        def test_branch = "master"
        def gitflow = new Gitflow(source: test_branch)

        //Act / Assert
        assertEquals("Branch set should be " + test_branch, test_branch, gitflow.getSourceBranch())
    }

    @Test
    void should_return_target_branch() {
        //Arrange
        def test_branch = "master"
        def gitflow = new Gitflow(source: 'release/test', target: test_branch)

        //Act / Assert
        assertEquals("Branch set should be " + test_branch, test_branch, gitflow.getTargetBranch())
    }

    @Test
    void should_get_parent_branch() {
        //Arrange / Act / Assert
        assertEquals('master', new Gitflow(source: 'release/test').getParentBranch())
        assertEquals('master', new Gitflow(source: 'hotfix/test').getParentBranch())
        assertEquals('develop', new Gitflow(source: 'feature/test').getParentBranch())
        assertEquals('master', new Gitflow(source: 'release/test', target: 'master').getParentBranch())
        assertEquals('master', new Gitflow(source: 'hotfix/test', target: 'master').getParentBranch())
        assertEquals('develop', new Gitflow(source: 'feature/test', target: 'develop').getParentBranch())
    }

    @Test
    void should_get_patch_increment_type_flag_for_hotfix_branch() {
        //Arrange
        def gitflow = new Gitflow(source: "hotfix/test")

        //Act
        def result = gitflow.getIncrementType()

        //Assert
        assertEquals("Should return patch flag", "p", result)
    }

    @Test
    void should_get_minor_increment_type_flag_for_release_branch() {
        //Arrange
        def gitflow = new Gitflow(source: "release/test")

        //Act
        def result = gitflow.getIncrementType()

        //Assert
        assertEquals("Should return minor flag", "m", result)
    }

    @Test(expected = Exception.class)
    void should_throw_exception_when_trying_to_get_update_flag_for_invalid_branch() {
        //Arrange
        def errorMessage = "Incorrect use of increment type function"
        def gitflow = new Gitflow(source: "feature/test")

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
        def gitflow = new Gitflow(source: branch)

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
                source: branch,
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

                            if(name == 'PROJECT_KEY') {
                                assertEquals("Should have correct project key: " + projectKey, projectKey, value)
                                return
                            }

                            if(name == 'RELEASE_TYPE') {
                                assertEquals("Should have release type: " + releaseType, releaseType, value)
                                return
                            }

                            if(name == 'PARENT_HASH') {
                                assertEquals("Should have correct parent hash: " + newVersion, newVersion, value)
                                return
                            }

                            if(name == 'BASE_BRANCH') {
                                assertEquals("Should have correct base branch: " + branch, branch, value)
                                return
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
                source: branch,
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

                            if(name == 'PROJECT_KEY') {
                                assertEquals("Should have correct project key: " + projectKey, projectKey, value)
                                return
                            }

                            if(name == 'RELEASE_TYPE') {
                                assertEquals("Should have release type: " + releaseType, releaseType, value)
                                return
                            }

                            if(name == 'PARENT_HASH') {
                                assertEquals("Should have correct parent hash: " + newVersion, newVersion, value)
                                return
                            }

                            if(name == 'BASE_BRANCH') {
                                assertEquals("Should have correct base branch: " + branch, branch, value)
                                return
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
    void should_get_nearest_parent_from_script() {
        //Arrange
        def gitflow = new Gitflow(script: [sh: { return 'something' }], source: 'master')

        //Act
        def result = gitflow.getNearestParentHash('master', 'release/test')

        //Assert
        assertEquals('something', result)
    }

    @Test(expected = Exception.class)
    void should_throw_exception_for_blank_nearest_parent_from_script() {
        //Arrange
        def gitflow = new Gitflow(script: [sh: { return '' }], source: 'master')

        //Act
        gitflow.getNearestParentHash('master', 'release/test')

        //Assert - should not get here
        assertTrue(false)
    }

    @Test(expected = Exception.class)
    void should_throw_exception_for_null_nearest_parent_from_script() {
        //Arrange
        def gitflow = new Gitflow(script: [sh: { return null }], source: 'master')

        //Act
        gitflow.getNearestParentHash('master', 'release/test')

        //Assert - should not get here
        assertTrue(false)
    }

    @Test
    void should_return_true_for_bump_commit() {
        //Arrange
        def gitflow = new Gitflow(
                source: "feature/test",
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
                source: "feature/test",
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
                source: "release/test",
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
                source: "hotfix/test",
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
                source: "bugfix/test",
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
                source: "bugfix/test",
                target: 'release/test',
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
                source: "release/test",
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
    void should_update_version_number_for_build() {
        //Arrange / Act / Assert
        assertTrue(new Gitflow(script: [ sh: { return 'yes' }], source: 'feature/test').shouldUpdateVersion())
        assertTrue(new Gitflow(script: [ sh: { return 'yes' }], source: 'bugfix/test').shouldUpdateVersion())
        assertTrue(new Gitflow(script: [ sh: { return 'yes' }], source: 'release/test').shouldUpdateVersion())
        assertTrue(new Gitflow(script: [ sh: { return 'yes' }], source: 'hotfix/test').shouldUpdateVersion())
        assertFalse(new Gitflow(script: [ sh: { return 'yes' }], source: 'master').shouldUpdateVersion())
        assertFalse(new Gitflow(script: [ sh: { return 'yes' }], source: 'develop').shouldUpdateVersion())
    }

    @Test
    void should_package_build_for_release_branch() {
        //Arrange
        def gitflow = new Gitflow(
                source: "release/test",
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
    }

    @Test
    void should_package_build_for_hotfix_branch() {
        //Arrange
        def gitflow = new Gitflow(
                source: "hotfix/test",
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
    }

    @Test
    void should_not_package_build_if_not_package_branch() {
        //Arrange
        def gitflow = new Gitflow(
                source: "feature/test",
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
        assertTrue(gitflow.shouldRunIntegrationTest())
    }

    @Test
    void should_not_package_build_if_pull_request() {
        //Arrange
        def gitflow = new Gitflow(
                source: 'bugfix/test',
                target: 'hotfix/test',
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
        assertTrue(gitflow.shouldRunIntegrationTest())
    }

    @Test
    void should_not_package_build_if_bump_commit() {
        //Arrange
        def gitflow = new Gitflow(
                source: "hotfix/test",
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
        assertTrue(gitflow.shouldRunIntegrationTest())
    }

    @Test
    void should_not_package_build_if_hotfix_branch_has_no_difference_to_parent() {
        //Arrange
        def gitflow = new Gitflow(
                source: "hotfix/test",
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
        assertTrue(gitflow.shouldRunIntegrationTest())
    }

    @Test
    void has_git_difference_to_parent() {
        //Arrange
        def gitflow = new Gitflow(
                source: "hotfix/test",
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
                source: "hotfix/test",
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
