pipeline {
    agent any

    options {
        buildDiscarder(logRotator(
                daysToKeepStr: '7',
                numToKeepStr: '5',
                artifactDaysToKeepStr: '5',
                artifactNumToKeepStr: '5'
        ))
    }

    parameters {
        choice(
                name: 'Project',
                choices: [
                        'Please Select',
                        'dan05011991/barebones-react',
                        'dan05011991/demo-application-backend','vickeryw/bandpCore'
                ],
                description: ''
        )
        string(
                name: 'Label',
                defaultValue: 'DEFAULT',
                description: 'This is unique name which will be prefixed on the end of the branch e.g. hotfix/mylabel'
        )
    }

    environment {
        SOURCE_BRANCH = 'master'
    }

    stages {
        stage('Clean') {
            steps {
                cleanWs()
            }
        }

        stage('Start Hotfix') {
            steps {
                git(
                    branch: "${SOURCE_BRANCH}",
                    url: "git@github.com:dan05011991/demo-application-backend.git",
                    credentialsId: 'ssh'
                )

                script {
                    sshagent(credentials: ['ssh']) {
                        sh "git checkout -b hotfix/${env.Label}"
                        sh "git push origin hotfix/${env.Label}"
                    }
                }
            }
        }
    }
}
