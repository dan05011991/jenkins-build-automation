package models

import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class VersionManagerTests {
    private String savedReleaseNumber = '2.3.0'
    private String savedPatchNumber = '2.2.5'
    private String savedProjectNumber = '2.3.5'

    private String inputFile = 'resources/testFile'

    @Before
    void setup() {
        FileWriter fw = new FileWriter(inputFile)
        fw.write('release=2.3.0\n')
        fw.write('patch=2.2.5\n')
        fw.write('0d84cca385dc8f6fe9c38f81a7986349abf4e260-release/test-branch=2.3.5\n')
        fw.close()
    }

    @After
    void cleanup() {
        File file = new File(inputFile)
        file.delete()
    }

    @Test
    void should_not_throw_exception_for_minor_version_change_flag() {
        //Arrange

        //Act
        def manager = new VersionManager(
                script: '?',
                projectKey: inputFile,
                incrementType: 'm',
                closestGitTag: '?',
                parentHash: '1234567890',
                baseBranch: 'release/another'
        )


        //Assert - here for readability
        Assert.assertTrue(true)
    }

    @Test
    void should_not_throw_exception_for_patch_version_change_flag() {
        //Arrange

        //Act
        def manager = new VersionManager(
                script: '?',
                projectKey: inputFile,
                incrementType: 'm',
                closestGitTag: '?',
                parentHash: '1234567890',
                baseBranch: 'release/another'
        )


        //Assert - here for readability
        Assert.assertTrue(true)
    }

    @Test(expected = Exception.class)
    void should_throw_exception_for_unsupported_major_version_change_flag() {
        //Arrange

        //Act
        def manager = new VersionManager(
                script: '?',
                projectKey: '?',
                incrementType: 'M',
                closestGitTag: '?',
                parentHash: '?',
                baseBranch: '?'
        )


        //Assert
        Assert.fail("Shouldn't have gotten here, should exception")
    }

    @Test(expected = Exception.class)
    void should_throw_exception_for_invalid_version_change_flag() {
        //Arrange

        //Act
        def manager = new VersionManager(
                script: '?',
                projectKey: '?',
                incrementType: '?',
                closestGitTag: '?',
                parentHash: '?',
                baseBranch: '?'
        )


        //Assert
        Assert.fail("Shouldn't have gotten here, should exception")
    }

    @Test
    void should_generated_new_release_version() {
        //Arrange
        def generatedVersion = '2.4.0'
        def script = [
                sh: { item ->
                    if(item['script'] == 'echo "$(./semver.sh -m ' + savedReleaseNumber + ')"') {
                        return generatedVersion
                    }
                    throw new Exception('Invalid use of sh')
                }
        ]

        def manager = new VersionManager(
                script: script,
                projectKey: inputFile,
                incrementType: 'm',
                closestGitTag: '?',
                parentHash: '1234567890',
                baseBranch: 'release/another'
        )

        //Act
        def version = manager.updateVersion()

        //Assert
        def map = readFile()
        Assert.assertEquals(generatedVersion, version)
        Assert.assertEquals(4, map.size())
        Assert.assertEquals(this.savedPatchNumber, map['patch'])
        Assert.assertEquals(generatedVersion, map['release'])
        Assert.assertEquals(this.savedProjectNumber, map['0d84cca385dc8f6fe9c38f81a7986349abf4e260-release/test-branch'])
        Assert.assertEquals(generatedVersion, map['1234567890-release/another'])
    }

    @Test
    void should_generated_new_patch_version_for_released_product() {
        //Arrange
        def generatedVersion = '2.2.6'
        def closestGitTag = savedPatchNumber
        def script = [
                sh: { item ->
                    if(item['script'] == 'echo "$(./semver.sh -p ' + savedPatchNumber + ')"') {
                        return generatedVersion
                    }
                    throw new Exception('Invalid use of sh')
                }
        ]

        def manager = new VersionManager(
                script: script,
                projectKey: inputFile,
                incrementType: 'p',
                closestGitTag: closestGitTag,
                parentHash: '?',
                baseBranch: '?'
        )

        //Act
        def version = manager.updateVersion()

        //Assert
        def map = readFile()
        Assert.assertEquals(generatedVersion, version)
        Assert.assertEquals(3, map.size())
        Assert.assertEquals(generatedVersion, map['patch'])
        Assert.assertEquals(this.savedReleaseNumber, map['release'])
        Assert.assertEquals(this.savedProjectNumber, map['0d84cca385dc8f6fe9c38f81a7986349abf4e260-release/test-branch'])
    }

    @Test
    void should_generated_new_patch_version_for_new_release() {
        //Arrange
        def generatedVersion = '2.3.1'
        def closestGitTag = savedProjectNumber
        def script = [
                sh: { item ->
                    if(item['script'] == 'echo "$(./semver.sh -p ' + this.savedReleaseNumber + ')"') {
                        return generatedVersion
                    }
                    throw new Exception('Invalid use of sh')
                }
        ]

        def manager = new VersionManager(
                script: script,
                projectKey: inputFile,
                incrementType: 'p',
                closestGitTag: closestGitTag,
                parentHash: '?',
                baseBranch: '?'
        )

        //Act
        def version = manager.updateVersion()

        //Assert
        def map = readFile()
        Assert.assertEquals(generatedVersion, version)
        Assert.assertEquals(3, map.size())
        Assert.assertEquals(generatedVersion, map['patch'])
        Assert.assertEquals(this.savedReleaseNumber, map['release'])
        Assert.assertEquals(this.savedProjectNumber, map['0d84cca385dc8f6fe9c38f81a7986349abf4e260-release/test-branch'])
    }

    @Test
    void should_generated_new_patch_version_for_ongoing_candidate_release() {
        //Arrange
        def generatedVersion = '2.3.6'
        def closestGitTag = savedProjectNumber
        def script = [
                sh: { item ->
                    if(item['script'] == 'echo "$(./semver.sh -p ' + this.savedProjectNumber + ')"') {
                        return generatedVersion
                    }
                    throw new Exception('Invalid use of sh')
                }
        ]

        def manager = new VersionManager(
                script: script,
                projectKey: inputFile,
                incrementType: 'm',
                closestGitTag: closestGitTag,
                parentHash: '0d84cca385dc8f6fe9c38f81a7986349abf4e260',
                baseBranch: 'release/test-branch'
        )

        //Act
        def version = manager.updateVersion()

        //Assert
        def map = readFile()
        Assert.assertEquals(generatedVersion, version)
        Assert.assertEquals(3, map.size())
        Assert.assertEquals(this.savedPatchNumber, map['patch'])
        Assert.assertEquals(this.savedReleaseNumber, map['release'])
        Assert.assertEquals(generatedVersion, map['0d84cca385dc8f6fe9c38f81a7986349abf4e260-release/test-branch'])
    }

    private Map<String, String> readFile() {
        def map = [:]
        def file = new File(inputFile)
        def lines = file.readLines()

        lines.each { line ->
            def items = "${line}".split('=')
            map[items[0]] = items[1]
        }

        return map
    }
}
