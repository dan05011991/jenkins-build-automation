#!/usr/bin/env groovy
import models.Docker

def call(config) {

    def docker_helper = new Docker(script: this, gitflow: config.gitflow)
    def unique_Id = UUID.randomUUID().toString()

    // If pull request then merge latest from base integration branch
    // feature branches = merge from develop
    // bugfix branches = merge from release
    if (config.gitflow.is_pull_request) {
        source_branch = config.gitflow.getSourceBranch()
        lookahead_branch = config.gitflow.getLookaheadBranch()

        echo "Lookahead merge from base branch ${lookahead_branch} to ${source_branch}"

        sh """
                git checkout ${lookahead_branch}
                git pull origin ${lookahead_branch}
                git checkout ${source_branch}
                git merge ${lookahead_branch}
            """
    }

    if (config.buildType == 'maven') {
        stage('Maven Build') {
            try {
                sh "docker build -f ${config.test} . -t ${unique_Id}"
                sh "docker run --name ${unique_Id} ${unique_Id} mvn surefire-report:report"
                sh "docker cp \$(docker ps -aqf \"name=${unique_Id}\"):/usr/webapp/target/surefire-reports ."
            } finally {
                junit 'surefire-reports/**/*.xml'

                sh "docker rm -f ${unique_Id}"
                sh "docker rmi ${unique_Id}"
            }
        }
    } else if (config.buildType == 'gulp') {
        stage('Gulp Build') {
            try {
                sh "docker build -f ${config.test} . -t ${unique_Id}"
                sh "docker run --name ${unique_Id} ${unique_Id} ./node_modules/gulp/bin/gulp test"
                sh "docker cp \$(docker ps -aqf \"name=${unique_Id}\"):/usr/webapp/tests/junit ."
            } finally {
                junit 'junit/**/*.xml'

                sh "docker rm -f ${unique_Id}"
                sh "docker rmi ${unique_Id}"
            }
        }
    } else {
        throw new Exception('Invalid build type specified')
    }

    if (config.gitflow.isMasterBranch()) {

        stage('Re-tag Docker Image') {
            project_version = sh([
                    script      : 'git describe --tags | sed -n -e "s/\\([0-9]\\)-.*/\\1/ p"',
                    returnStdout: true
            ]).trim()

            docker_version = docker_helper.getDockerTag(project_version)

            if (!docker_helper.doesDockerImageExist(config.imageName + docker_version)) {
                referenceTag = docker_helper.getReferenceTag(project_version)
                sh "docker pull ${config.imageName}${referenceTag}"
                sh "docker tag ${config.imageName}${referenceTag} ${config.imageName}${docker_version}"

                withDockerRegistry([credentialsId: "dockerhub", url: ""]) {
                    sh "docker push ${config.imageName}${docker_version}"
                }
            }
        }
    }
}

return this