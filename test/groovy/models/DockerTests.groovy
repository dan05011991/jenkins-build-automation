package models

import com.lesfurets.jenkins.unit.BasePipelineTest
import org.junit.Test

import static org.junit.Assert.*

class DockerTests extends BasePipelineTest {

    @Test
    void should_push_docker_image() {
        //Arrange
        def script = [
                sh: {
                    return '1.0.0'
                }
        ]
        def docker = new Docker(
                script: script
        )

        //Act
        docker.pushDeveloperImage('test-image')

        //Assert

    }

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
                source: 'master',
                script: script
        )
        def docker = new Docker(
                script: script,
                gitflow: gitflow
        )

        //Act
        def result = docker.doesDockerImageExist("test", "1.0.0")

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
                source: 'master',
                script: script
        )
        def docker = new Docker(
                script: script,
                gitflow: gitflow
        )

        //Act
        def result = docker.doesDockerImageExist("test", "1.0.0")

        //Assert
        assertFalse("Image does not exists", result)
    }

    @Test
    void should_get_docker_tag_for_master_branch() {
        //Arrange
        String version = '1.0.0'
        def gitflow = new Gitflow(
                source: 'master'
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
                source: 'develop'
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
                source: 'hotfix/test'
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
                source: 'release/test'
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
                source: 'feature/dev'
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
                source: 'master'
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
                source: 'hotfix/test'
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
                source: 'release/test'
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
                source: 'feature/dev'
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
