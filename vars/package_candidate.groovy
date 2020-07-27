#!/usr/bin/env groovy

def call(config) {

    def project_version = sh([
            script      : 'mvn help:evaluate -Dexpression=project.version -q -DforceStdout',
            returnStdout: true
    ]).trim()

    stage('Docker Candidate Build') {
        docker_tag_version = config.docker_helper.getDockerTag(project_version)

        if(config.buildType == 'docker-in-maven') {
            def unique_Id = UUID.randomUUID().toString()
            sh "docker build -t ${unique_Id} -f ${config.test} ."
            sh "docker run --name ${unique_Id} ${config.testMounts} ${unique_Id}"
            sh "docker rm -f ${unique_Id}"
            sh "docker rmi ${unique_Id}"

            def image = "${config.docker_helper.developerRepo}/${config.imageName}:${project_version}"
            sh "docker tag ${image} ${config.docker_helper.developerRepo}/${config.imageName}:${docker_tag_version}"
        } else {
            sh "docker build -t ${config.docker_helper.developerRepo}/${config.imageName}:${docker_tag_version} ."
        }
    }

    stage('Prepare project for next iteration') {
        sh "git tag -a ${project_version} -m \"Release ${project_version}\""

        if (config.buildType.indexOf('maven') > -1) {
            sh "mvn versions:set -DnewVersion=development -DprocessAllModules"
            sh 'git add pom.xml'
            sh 'git commit -m "[Automated commit: Prepare project for next iteration]"'
        }
    }

    stage('Push Updates') {
        sh "docker push ${config.docker_helper.developerRepo}/${config.imageName}:${docker_tag_version}"

        sshagent(credentials: ['ssh']) {
            sh "git push origin ${config.gitflow.getSourceBranch()}"
            sh "git push origin ${project_version}"
        }
    }
}

return this
