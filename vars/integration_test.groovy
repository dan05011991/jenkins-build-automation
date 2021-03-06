#!/usr/bin/env groovy

def call(config) {

    def unique_Id = UUID.randomUUID().toString()

    // If pull request then merge latest from base integration branch
    // feature branches = merge from develop
    // bugfix branches = merge from release
    if (config.gitflow.is_pull_request) {
        source_branch = config.gitflow.getSourceBranch()
        target_branch = config.gitflow.getTargetBranch()

        echo "Lookahead merge from base branch ${target_branch} to ${source_branch}"

        sshagent(credentials: ['ssh']) {
            sh """
                git checkout ${target_branch}
                git pull origin ${target_branch}
                git checkout ${source_branch}
                git merge ${target_branch}
            """
        }
    }

    if (config.buildType == 'docker-in-maven') {
        stage('Docker In Maven Build') {
            try {
                sh "docker build -t ${unique_Id} -f ${config.test} ."
                sh "docker run --name ${unique_Id} ${config.testMounts} ${unique_Id}"
                sh "docker cp \$(docker ps -aqf \"name=${unique_Id}\"):/usr/webapp/target/surefire-reports ."
            } finally {
                junit 'surefire-reports/**/*.xml'

                sh "docker rm -f ${unique_Id}"
                sh "docker rmi ${unique_Id}"
            }
        }
    } else if (config.buildType == 'maven') {
        stage('Maven Build') {
            try {
                sh "docker build -t ${unique_Id} -f ${config.test} ."
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
                sh "docker build -t ${unique_Id} -f ${config.test} ."
                sh "docker run --name ${unique_Id} ${unique_Id} ./node_modules/gulp/bin/gulp test"
                sh "docker cp \$(docker ps -aqf \"name=${unique_Id}\"):/usr/webapp/tests/junit ."
            } finally {
                junit 'junit/**/*.xml'

                sh "docker rm -f ${unique_Id}"
                sh "docker rmi ${unique_Id}"
            }
        }
    } else if (config.buildType == 'webpack') {
        stage('Webpack Build') {
            try {
                sh "docker build -t ${unique_Id} -f ${config.test} ."
                sh "docker run --name ${unique_Id} ${unique_Id} npm test"
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

    if(config.gitflow.isIntegrationBranch()) {
        echo 'Pushing intermediate image'
        config.docker_helper.pushDeveloperImage(config.imageName)
    }
}

return this
