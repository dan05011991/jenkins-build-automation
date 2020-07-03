pipeline {
    agent any

    parameters {
        choice(
                name: 'Project',
                choices: ['dan05011991/barebones-react','dan05011991/demo-application-backend','vickeryw/bandpCore'],
                description: ''
        )
        string(
                name: 'Label',
                defaultValue: 'DEFAULT',
                description: 'This is unique name which will be prefixed on the end of the branch e.g. feature/mylabel'
        )
    }

    environment {
        SOURCE_BRANCH = 'develop'
    }

    stages {
        stage('Clean') {
            steps {
                cleanWs()
            }
        }

        stage('Start Release') {
            steps {
                git(
                    branch: "${SOURCE_BRANCH}",
                    url: "git@github.com:dan05011991/demo-application-backend.git",
                    credentialsId: 'ssh'
                )

                script {
                    sshagent(credentials: ['ssh']) {
                        sh "git checkout -b feature/${env.Label}"
                        sh "git push origin feature/${env.Label}"
                    }
                }
            }
        }
    }
}
