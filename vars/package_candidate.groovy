#!/usr/bin/env groovy

def call(config) {

    stage('Docker Candidate Build') {
        docker_tag_version = config.docker_helper.getDockerTag(project_version)

        sh "docker build -t ${config.imageName}:${docker_tag_version} ."
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
        withCredentials([[$class: 'UsernamePasswordMultiBinding', credentialsId:'dockerhub', usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD']]) {
            sh 'docker login -u $USERNAME -p $PASSWORD -e admin@example.com'
            sh "docker push ${config.imageName}:${docker_tag_version}"
        }

        sshagent(credentials: ['ssh']) {
            sh "git push origin ${config.gitflow.getSourceBranch()}"
            sh "git push origin ${project_version}"
        }
    }
}

return this
