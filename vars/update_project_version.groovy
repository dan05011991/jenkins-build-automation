#!/usr/bin/env groovy

def call(config) {
    stage('Update project version') {

        gitTag = sh([
                script      : 'git describe --tags | sed -n -e "s/\\([0-9]\\)-.*/\\1/ p"',
                returnStdout: true
        ]).trim()

        project_version = config.gitflow.getNextVersion(config.projectKey, gitTag)

        if (config.buildType == 'docker-in-maven' || config.buildType == 'maven') {
            stage('Maven Version Update') {
                sh "mvn versions:set -DnewVersion=${project_version}"
                sh 'git add pom.xml'
                sh 'git commit -m "[Automated commit: version bump]"'
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
                sh 'git commit -m "[Automated commit: version bump]"'
            }
        } else if (config.buildType == 'webpack') {
            stage('Webpack Version Update') {

                ui_version = sh(
                        script: "sed -n \"s/^.*version.*\\\"\\(.*\\)\\\".*\$/\\1/ p\" package.json | tr -d '\\n'",
                        returnStdout: true
                )

                sh("""
                    #!/bin/bash
                    sed -i 's/\"version\": \"${ui_version}\"/\"version\": \"${project_version}\"/g' package.json
                """)

                sh 'git add package.json'
                sh 'git commit -m "[Automated commit: version bump]"'
            }
        }
    }
}

return this