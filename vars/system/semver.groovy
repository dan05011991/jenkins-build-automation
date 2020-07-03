#!/usr/bin/env groovy
import models.VersionManager
import java.util.regex.Pattern

node {
    parameters {
        string(
                name: 'PROJECT_KEY',
                defaultValue: 'DEFAULT'
        )
        string(
                name: 'RELEASE_TYPE',
                defaultValue: 'DEFAULT'
        )
        string(
                name: 'GIT_TAG',
                defaultValue: 'DEFAULT'
        )
        string(
                name: 'PARENT_HASH',
                defaultValue: 'DEFAULT'
        )
        string(
                name: 'BASE_BRANCH',
                defaultValue: 'DEFAULT'
        )
    }

    def manager = new VersionManager(
            script: this,
            projectKey: "${PROJECT_KEY}",
            incrementType: "${RELEASE_TYPE}",
            closestGitTag: "${GIT_TAG}",
            parentHash: "${PARENT_HASH}",
            baseBranch: "${BASE_BRANCH}"

    )

    cleanWs()
    
    git(
            branch: "master",
            url: "git@github.com:dan05011991/versioning.git",
            credentialsId: 'ssh'
    )
    
    createScript('semver.sh')

    String newVersion = manager.updateVersion()

    sh("rm semver.sh")

    sh("git add ${PROJECT_KEY}")
    sh("git commit -m \"Bumped version for ${PROJECT_KEY}\"")
    sh("git push origin master")

    sh("cat ${newVersion} > version")
    archiveArtifacts artifacts: 'version', fingerprint: true
}

def check_input(input) {
    if(input == 'DEFAULT') {
        throw new Exception('Input parameter is not set')
    }
}

def createScript(scriptName) {
    def scriptContent = libraryResource "com/corp/pipeline/scripts/${scriptName}"
    writeFile file: "${scriptName}", text: scriptContent
    sh "chmod +x ${scriptName}"
}
