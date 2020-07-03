#!/usr/bin/env groovy
import models.VersionManager
import java.util.regex.Pattern

node {
    properties([
        disableConcurrentBuilds(),
        buildDiscarder(logRotator(daysToKeepStr: '7', numToKeepStr: '5')),
        parameters ([
            string(
                    name: 'PROJECT_KEY',
                    defaultValue: 'DEFAULT'
            ),
            string(
                    name: 'RELEASE_TYPE',
                    defaultValue: 'DEFAULT'
            ),
            string(
                    name: 'GIT_TAG',
                    defaultValue: 'DEFAULT'
            ),
            string(
                    name: 'PARENT_HASH',
                    defaultValue: 'DEFAULT'
            ),
            string(
                    name: 'BASE_BRANCH',
                    defaultValue: 'DEFAULT'
            )
        ])
    ])

    stage('Setup') {
        sh 'git config --global user.email "jenkins@bnp.com"'
        sh 'git config --global user.name "Jenkins Admin"'

        cleanWs()

        git(
                branch: "master",
                url: "git@github.com:dan05011991/versioning.git",
                credentialsId: 'ssh'
        )

        createScript('semver.sh')
    }

    stage('Update') {
        def manager = new VersionManager(
                script: this,
                projectKey: "${pwd()}/${PROJECT_KEY}",
                incrementType: "${RELEASE_TYPE}",
                closestGitTag: "${GIT_TAG}",
                parentHash: "${PARENT_HASH}",
                baseBranch: "${BASE_BRANCH}"

        )

        newVersion = manager.updateVersion()
    }

    stage('Save') {
        sh("rm semver.sh")

        sh("git add ${PROJECT_KEY}")
        sh("git commit -m \"Bumped version for ${PROJECT_KEY}\"")

        sshagent(credentials: ['ssh']) {
            sh("git push origin master")
        }

        sh("echo ${newVersion} > version")
        archiveArtifacts artifacts: 'version', fingerprint: true
    }
}

def check_input(input) {
    if(input == 'DEFAULT') {
        throw new Exception('Input parameter is not set')
    }
}

def createScript(scriptName) {
    def scriptContent = libraryResource "com/pipeline/scripts/${scriptName}"
    writeFile file: "${scriptName}", text: scriptContent
    sh "chmod +x ${scriptName}"
}
