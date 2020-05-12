#!/usr/bin/env groovy
import models.Docker

def call(config) {

    def docker_helper = new Docker(script: this, gitflow: config.gitflow)

    stage('Update project version') {

        gitTag = sh([
                script      : 'git describe --tags | sed -n -e "s/\\([0-9]\\)-.*/\\1/ p"',
                returnStdout: true
        ]).trim()

        project_version = config.gitflow.getNewReleaseVersion(config.projectKey, gitTag)

        if (config.buildType == 'maven') {
            stage('Maven Version Update') {
                sh "mvn versions:set -DnewVersion=${project_version}"
                sh 'git add pom.xml'
                sh 'git commit -m "[Automated commit: Project released]"'
            }
        } else if (config.buildType == 'gulp') {
            stage('Gulp Version Update') {

                ui_version = sh(
                        script: "sed -n \"s/^.*appVersion.*'\\(.*\\)'.*\$/\\1/ p\" conf/config-release.js | tr -d '\\n'",
                        returnStdout: true
                )

                sh("""
                                #!/bin/bash
                                sed -i "s/appVersion: '${ui_version}'/appVersion: '${project_version}'/g" conf/config-release.js
                            """)

                sh 'git add conf/config-release.js'
                sh 'git commit -m "[Automated commit: Project released]"'
            }
        } else if (config.buildType == 'webpack') {
            stage('Webpack Version Update') {

                ui_version = sh(
                    script: "sed -n \"s/^.*version.*\\\"\\(.*\\)\\\".*\$/\\1/ p\" package.json | tr -d '\\n'",
                    returnStdout: true
                )

                sh("""
                    #!/bin/bash
                    sed -i "s/version: \"${ui_version}\"/version: \"${project_version}\"/g" package.json
                """)

                sh 'git add package.json'
                sh 'git commit -m "[Automated commit: Project released]"'
            }
        }
    }

    stage('Docker Candidate Build') {
        docker_tag_version = docker_helper.getDockerTag(project_version)

        sh "docker build . -t ${config.imageName}:${docker_tag_version}"
    }

    stage('Prepare project for next iteration') {
        sh "git tag -a ${project_version} -m \"Release ${project_version}\""

        if (config.buildType == 'maven') {
            sh "mvn versions:set -DnewVersion=${project_version}-SNAPSHOT"
            sh 'mvn release:update-versions -B'
            sh 'git add pom.xml'
            sh 'git commit -m "[Automated commit: version bump]"'
        }
    }

    stage('Push Updates') {
        withDockerRegistry([credentialsId: "dockerhub", url: ""]) {
            sh "docker push ${config.imageName}:${docker_tag_version}"
        }

        sshagent(credentials: ['ssh']) {
            sh "git push origin ${config.gitflow.getSourceBranch()}"
            sh "git push origin ${project_version}"
        }
    }
}

return this