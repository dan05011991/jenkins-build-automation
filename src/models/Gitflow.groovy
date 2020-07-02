package models

class Gitflow {
    private final def script
    private final String branch
    private final Boolean is_pull_request

    Gitflow(Map<String, ?> items) {
        this.script = items.get("script")
        this.branch = items.get("branch")
        this.is_pull_request = items.get("is_pull_request")

        if(branch == null) {
            throw new Exception('Gitflow has not been setup correctly')
        }
    }

    def isValid() {
        return isMainBranch() || isPackageBranch() || isIntegrationBranch()
    }

    def isMasterBranch() {
        return branch == 'master'
    }

    def isDevelopBranch() {
        return branch == 'develop'
    }

    def isHotfixBranch() {
        return branch.startsWith('hotfix/')
    }

    def isReleaseBranch() {
        return branch.startsWith('release/')
    }

    def isBugfixBranch() {
        return branch.startsWith('bugfix/')
    }

    def isFeatureBranch() {
        return branch.startsWith('feature/')
    }

    def isPackageBranch() {
        return isReleaseBranch() || isHotfixBranch()
    }

    def isIntegrationBranch() {
        return isFeatureBranch() || isBugfixBranch()
    }

    def isMainBranch() {
        return isMasterBranch() || isDevelopBranch()
    }

    def isPullRequest() {
        return is_pull_request
    }

    def getSourceBranch() {
        return branch
    }

    def getLookaheadBranch() {
        if(branch.startsWith('release/') || branch.startsWith('hotfix/')) {
            return 'master'
        }
        if(branch.startsWith('feature/')) {
            return 'develop'
        }
        
        def branch = script.sh(
                script: "./get_parent_branch.sh",
                returnStdout: true).trim()

        if (branch?.trim()) {
            return branch.trim()
        }

        throw new Exception('Unable to determine the parent branch')
    }

    def getIncrementType() {
        if(isReleaseBranch()) {
            return 'm'
        } else if(isHotfixBranch()) {
            return 'p'
        }
        throw new Exception('Incorrect use of increment type function')
    }

    def getNextVersion(String key, String tag) {

        if(isFeatureBranch() || isBugfixBranch()) {
            return branch.replace("_", "-")
                         .replace("/", "_")
        }

        def type = getIncrementType()
        def job = script.build(
                job: 'SemVer',
                parameters: [
                    script.string(name: 'PROJECT_KEY', value: "${key}"),
                    script.string(name: 'RELEASE_TYPE', value: "${type}"),
                    script.string(name: 'GIT_TAG', value: "${tag}")
                ],
                propagate: true,
                wait: true)

        script.copyArtifacts(
                fingerprintArtifacts: true,
                projectName: 'SemVer',
                selector: script.specific("${job.number}")
        )

        def version = script.sh(
                script: 'cat version',
                returnStdout: true
        ).trim()

        return version
    }

    def isBumpCommit() {
        def lastCommit = script.sh(
                script: 'git log -1',
                returnStdout: true)
        if (lastCommit.contains("[Automated commit: version bump]")) {
            return true
        } else {
            return false
        }
    }

    def shouldExitBuild() {
        return isPackageBranch() && isBumpCommit() && !isPullRequest()
    }

    // If it's a feature branch or a package branch - we update the version
    // Feature / Bugfix branch = transform the branch name into a version
    // Package branch = uses job to generate new version
    def shouldUpdateVersion() {
        return isFeatureBranch() || isFeatureBranch() ||  shouldPackageBuild()
    }

    def shouldRunIntegrationTest() {
        return !shouldPackageBuild()
    }

    def shouldPackageBuild() {
        // Don't package if this isn't a package branch (hotfix or release)
        if(!isPackageBranch()) {
            return false
        }

        // Don't package if this is a pull request (avoids repackaging) or is a bump commit (already packaged)
        if(isPullRequest() || isBumpCommit()) {
            return false
        }

        // If request branch = package
        if(isReleaseBranch()) {
            return true
        }

        // If hotfix branch, only package if there is a git different to parent (e.g. master)
        // Avoids packaging when branch is created
        // Different behaviour to release branches which package on branch creation
        return isHotfixBranch() && hasGitDifferenceToParent()
    }

    def hasGitDifferenceToParent() {
        String parentBranch = getLookaheadBranch()
        script.sh(script: "git fetch --prune")
        return script.sh(script: """
                if [ "\$(git diff origin/${parentBranch} 2> /dev/null)" ]; then 
                    echo "yes"; 
                fi
            """, returnStdout: true) == 'yes'
    }
}