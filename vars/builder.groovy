#!/usr/bin/env groovy
import models.Gitflow

def call(Map config=[:], Closure body={}) {

    node {
        properties([
                disableConcurrentBuilds()
        ])

        // This section must be above the gitflow initialisation
        if (BRANCH_NAME.startsWith('PR-')) {
            source_branch = CHANGE_BRANCH
            is_pull_request = true
        } else {
            source_branch = BRANCH_NAME
            is_pull_request = false
        }

        def gitflow = new Gitflow(
                script: this,
                branch: source_branch,
                is_pull_request: is_pull_request
        )

        if (!gitflow.isValid()) {
            throw new Exception("Invalid branch syntax. Must follow standard GitFlow process")
        }

        stage('Clean') {

            cleanWs()

            source_url = "${scm.userRemoteConfigs[0].url}"

            echo "Source branch: ${source_branch}"
            echo "Source Url: ${source_url}"
            echo "Is Pull Request?: ${is_pull_request}"
        }

        stage('Pipeline setup') {
            parallel(
                    'Checkout Project': checkout_step(source_branch, source_url),
                    'Create pipeline scripts': create_pipeline_step()
            )
        }

        if(gitflow.shouldExitBuild()) {
            echo "This is a bump commit build - exiting early"
            return
        }

        if(!gitflow.shouldPackageBuild()) {
            integration_test(
                    imageName: config.imageName,
                    buildType: config.buildType,
                    test: config.test,
                    gitflow: gitflow
            )
        } else {
            package_candidate(
                    projectKey: config.projectKey,
                    imageName: config.imageName,
                    buildType: config.buildType,
                    test: config.test,
                    gitflow: gitflow
            )
        }

        if(gitflow.isMasterBranch() && config.autoDeploy) {
            gitTag = sh([
                    script      : 'git describe --tags | sed -n -e "s/\\([0-9]\\)-.*/\\1/ p"',
                    returnStdout: true
            ]).trim()

            build(
                job: 'Deploy',
                parameters: [
                        string(name: 'Image', value: "${config.imageName}"),
                        string(name: 'Tag', value: "${gitTag}")
                ],
                propagate: true,
                wait: true)
        }

        body()
    }
}

def checkout_step(source_branch, source_url) {
    return {
        git(
                branch: "${source_branch}",
                url: "${source_url}",
                credentialsId: 'ssh'
        )
    }
}

def create_pipeline_step() {
    return {
        createScript('get_parent_branch.sh')
    }
}

def createScript(scriptName) {
    def scriptContent = libraryResource "com/pipeline/scripts/${scriptName}"
    writeFile file: "${scriptName}", text: scriptContent
    sh "chmod +x ${scriptName}"
}

return this