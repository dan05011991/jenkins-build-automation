#!/usr/bin/env groovy
import models.Docker
import models.Gitflow

def call(Map config=[:], Closure body={}) {

    node {
        properties([
                disableConcurrentBuilds(),
                buildDiscarder(logRotator(daysToKeepStr: '7', numToKeepStr: '5'))
        ])

        sh 'git config --global user.email "jenkins@bnp.com"'
        sh 'git config --global user.name "Jenkins Admin"'

        // This section must be above the gitflow initialisation
        def is_pull_request = BRANCH_NAME.startsWith('PR-')
        def source_branch = BRANCH_NAME
        def target_branch = null
        def source_url = "${scm.userRemoteConfigs[0].url}"

        // Override source branch with change_branch in PR scenarios
        if (is_pull_request) {
            source_branch = CHANGE_BRANCH
            target_branch = CHANGE_TARGET
        }

        def gitflow = new Gitflow(
                script: this,
                source: source_branch,
                target: target_branch,
                is_pull_request: is_pull_request
        )
        def docker_helper = new Docker(
                script: this,
                gitflow: gitflow,
                developerRepo: config.developerRepo,
                releaseRepo: config.releaseRepo)

        stage('Clean') {

            cleanWs()

            echo "Source branch: ${source_branch}"
            echo "Target branch: ${target_branch}"
            echo "Source Url: ${source_url}"
            echo "Is Pull Request?: ${is_pull_request}"
        }

        stage('Pipeline setup') {
            parallel(
                    'Checkout Project': checkout_step(source_branch, source_url),
                    'Create pipeline scripts': create_pipeline_scripts(),
                    'Developer Docker login': docker_login('dev-docker-repo', docker_helper.developerRepo),
                    'Release Docker login': docker_login('release-docker-repo', docker_helper.releaseRepo)
            )
        }

        if (gitflow.shouldExitBuild()) {
            echo "This is a bump commit build - exiting early"
            return
        }

        // If it's a feature branch or a package branch - we update the version
        // Feature / Bugfix branch = transform the branch name into a version
        // Package branch = uses job to generate new version
        if (gitflow.shouldUpdateVersion()) {
            update_project_version(
                    projectKey: config.projectKey,
                    buildType: config.buildType,
                    gitflow: gitflow
            )
        }

        // Should run integration test (Non-package routes)
        if (gitflow.shouldRunIntegrationTest()) {
            integration_test(
                    imageName: config.imageName,
                    buildType: config.buildType,
                    test: config.test,
                    testMounts: config.testMounts,
                    gitflow: gitflow,
                    docker_helper: docker_helper
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

def create_pipeline_scripts() {
    return {
        createScript('get_parent_hash.sh')
    }
}

def docker_login(credentialKey, url) {
    return {
        withCredentials([[$class: 'UsernamePasswordMultiBinding',
                          credentialsId: credentialKey,
                          usernameVariable: 'USERNAME',
                          passwordVariable: 'PASSWORD']]) {
            try {
                sh "docker login -u $USERNAME -p $PASSWORD -e admin@example.com ${url}"
            } catch(Exception ex) {
                echo 'Failed using -e parameter, trying without ...'
                sh "docker login -u $USERNAME -p $PASSWORD ${url}"
            }
        }
    }
}

def createScript(scriptName) {
    echo "Creating pipeline script ${scriptName}"
    def scriptContent = libraryResource "com/pipeline/scripts/${scriptName}"
    writeFile file: "${scriptName}", text: scriptContent
    sh "chmod +x ${scriptName}"
}

return this