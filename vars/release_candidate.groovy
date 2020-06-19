#!/usr/bin/env groovy

def call(config) {

    stage('Re-tag Docker Image') {
        project_version = sh([
                script      : 'git describe --tags | sed -n -e "s/\\([0-9]\\)-.*/\\1/ p"',
                returnStdout: true
        ]).trim()

        docker_version = config.docker_helper.getDockerTag(project_version)

        if (!config.docker_helper.doesDockerImageExist(config.imageName + ":" + docker_version)) {
            referenceTag = config.docker_helper.getReferenceTag(project_version)
            sh "docker pull ${config.imageName}:${referenceTag}"
            sh "docker tag ${config.imageName}:${referenceTag} ${config.imageName}:${docker_version}"

            sh "docker push ${config.imageName}:${docker_version}"
        }
    }
}

return this
