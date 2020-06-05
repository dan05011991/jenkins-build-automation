package models

import com.lesfurets.jenkins.unit.BasePipelineTest
import org.junit.Test

import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertFalse
import static org.junit.Assert.assertTrue
import static org.junit.Assert.fail

class DockerTests extends BasePipelineTest {

    @Test
    void should_return_true_when_image_exists() {
        //Arrange
        def exists = "yes"
        def script = [
                sh: {
                    return exists
                }
        ]
        def gitflow = new Gitflow(
                branch: 'example',
                script: script
        )
        def docker = new Docker(
                script: script,
                gitflow: gitflow
        )

        //Act
        def result = docker.doesDockerImageExist("test")

        //Assert
        assertTrue("Image should exist", result)
    }

    @Test
    void should_return_false_when_image_does_not_exist() {
        //Arrange
        def script = [
                sh: {
                    throw new Exception("Image doesn't exist")
                }
        ]
        def gitflow = new Gitflow(
                branch: 'example',
                script: script
        )
        def docker = new Docker(
                script: script,
                gitflow: gitflow
        )

        //Act
        def result = docker.doesDockerImageExist("test")

        //Assert
        assertFalse("Image does not exists", result)
    }

    @Test
    void should_get_docker_tag_for_master_branch() {
        //Arrange
        String version = '1.0.0'
        def gitflow = new Gitflow(
                branch: 'master'
        )
        def docker = new Docker(
                gitflow: gitflow
        )

        //Act
        def result = docker.getDockerTag(version)

        //Assert
        assertEquals("Version number should not be changed", version, result)
    }

    @Test
    void should_get_docker_tag_for_develop_branch() {
        //Arrange
        String version = '1.0.0'
        def gitflow = new Gitflow(
                branch: 'develop'
        )
        def docker = new Docker(
                gitflow: gitflow
        )

        //Act
        def result = docker.getDockerTag(version)

        //Assert
        assertEquals("Version number be suffixed with snapshot", version + '-SNAPSHOT', result)
    }

    @Test
    void should_get_docker_tag_for_hotfix_branch() {
        //Arrange
        String version = '1.0.0'
        def gitflow = new Gitflow(
                branch: 'hotfix/test'
        )
        def docker = new Docker(
                gitflow: gitflow
        )

        //Act
        def result = docker.getDockerTag(version)

        //Assert
        assertEquals("Version number be suffixed with release candidate", version + '-release-candidate', result)
    }

    @Test
    void should_get_docker_tag_for_release_branch() {
        //Arrange
        String version = '1.0.0'
        def gitflow = new Gitflow(
                branch: 'release/test'
        )
        def docker = new Docker(
                gitflow: gitflow
        )

        //Act
        def result = docker.getDockerTag(version)

        //Assert
        assertEquals("Version number be suffixed with release candidate", version + '-release-candidate', result)
    }

    @Test(expected = Exception.class)
    void should_throw_exception_when_getting_docker_tag_for_branch_which_is_not_allowed() {
        //Arrange
        def errorMessage = 'Attempting to get docker tag for a branch which is not allowed'
        def gitflow = new Gitflow(
                branch: 'feature/dev'
        )
        def docker = new Docker(
                gitflow: gitflow
        )

        //Act
        try {
            docker.getDockerTag('1.0.0')
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
    void should_get_reference_tag_for_master_branch() {
        //Arrange
        String version = '1.0.0'
        def gitflow = new Gitflow(
                branch: 'master'
        )
        def docker = new Docker(
                gitflow: gitflow
        )

        //Act
        def result = docker.getReferenceTag(version)

        //Assert
        assertEquals("Version number be suffixed with release candidate", version + '-release-candidate', result)
    }

    @Test
    void should_get_reference_tag_for_hotfix_branch() {
        //Arrange
        String version = '1.0.0'
        def gitflow = new Gitflow(
                branch: 'hotfix/test'
        )
        def docker = new Docker(
                gitflow: gitflow
        )

        //Act
        def result = docker.getReferenceTag(version)

        //Assert
        assertEquals("Version number be suffixed with snapshot", version + '-SNAPSHOT', result)
    }

    @Test
    void should_get_reference_tag_for_release_branch() {
        //Arrange
        String version = '1.0.0'
        def gitflow = new Gitflow(
                branch: 'release/test'
        )
        def docker = new Docker(
                gitflow: gitflow
        )

        //Act
        def result = docker.getReferenceTag(version)

        //Assert
        assertEquals("Version number be suffixed with snapshot", version + '-SNAPSHOT', result)
    }

    @Test(expected = Exception.class)
    void should_throw_exception_when_getting_reference_tag_for_branch_which_is_not_allowed() {
        //Arrange
        def errorMessage = 'Attempting to get reference tag for a branch which is not allowed'
        def gitflow = new Gitflow(
                branch: 'feature/dev'
        )
        def docker = new Docker(
                gitflow: gitflow
        )

        //Act
        try {
            docker.getReferenceTag('1.0.0')
        }
        catch (Exception ex) {
            //Assert
            assertEquals(errorMessage, ex.getMessage())
            throw ex
        }

        //Assert
        fail("Should not get to this point, exception should be thrown")
    }
}
