#!/usr/bin/env groovy
import models.Docker
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
        def docker_helper = new Docker(
                script: this,
                gitflow: config.gitflow)

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
                    'Create pipeline scripts': create_pipeline_step(),
                    'Developer Docker login': docker_login('dev-docker-repo', docker_helper.developerRepo),
                    'Release Docker login': docker_login('release-docker-repo', docker_helper.releaseRepo)
            )
        }

        if (gitflow.shouldExitBuild()) {
            echo "This is a bump commit build - exiting early"
            return
        }

        // If it's a feature branch or a package branch - we update the version
        // Feature branch = transform the branch name into a version
        // Package branch = uses job to generate new version
        if (gitflow.isFeatureBranch() || gitflow.shouldPackageBuild()) {
            update_project_version(
                    projectKey: config.projectKey,
                    buildType: config.buildType,
                    gitflow: gitflow
            )
        }

        // Should run integration test (Non-package routes)
        if (gitflow.shouldRunIntegrationTest()) {
            integration_test(
                    buildType: config.buildType,
                    test: config.test,
                    testMounts: config.testMounts,
                    gitflow: gitflow
            )
        }

        // Package candidate (hotfix, release branches excluding PR)
        if (gitflow.shouldPackageBuild()) {
            package_candidate(
                    buildType: config.buildType,
                    imageName: config.imageName,
                    test: config.test,
                    testMounts: config.testMounts,
                    docker_helper: docker_helper
            )
        }

        // Re-tag candidate
        if (gitflow.isMasterBranch()) {
            release_candidate(
                    imageName: config.imageName,
                    docker_helper: docker_helper
            )
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

def docker_login(credentialKey, url) {
    return {
        withCredentials([[$class: 'UsernamePasswordMultiBinding',
                          credentialsId: credentialKey,
                          usernameVariable: 'USERNAME',
                          passwordVariable: 'PASSWORD']]) {
            sh "docker login -u $USERNAME -p $PASSWORD -e admin@example.com ${url}"
        }
    }
}

def createScript(scriptName) {
    def scriptContent = libraryResource "com/pipeline/scripts/${scriptName}"
    writeFile file: "${scriptName}", text: scriptContent
    sh "chmod +x ${scriptName}"
}

return this