package models

import com.cloudbees.groovy.cps.NonCPS

class Gitflow {
    private final def script
    private final String source
    private final String target
    private final Boolean is_pull_request

    Gitflow(Map<String, ?> items) {
        this.script = items.get("script")
        this.source = items.get("source")
        this.target = items.get("target")
        this.is_pull_request = items.get("is_pull_request")

        if(source == null) {
            throw new Exception('Gitflow has not been setup correctly')
        }

        validateGitflow()
    }

    @NonCPS
    def validateGitflow() {
        if(!isMainBranch() && !isPackageBranch() && !isIntegrationBranch()) {
            throw new Exception("Invalid branch syntax. Must follow standard GitFlow process")
        }

        if(this.is_pull_request) {
            if(this.target == null) {
                throw new Exception("Gitflow was unable to determine target branch for pull request")
            }

            if(isMasterBranch(this.target) && !isPackageBranch()) {
                throw new Exception("Gitflow violation, attempting to merge into master from invalid source ${this.source}")
            }

            if(isDevelopBranch(this.target) && !isFeatureBranch()) {
                throw new Exception("Gitflow violation, attempting to merge into develop from invalid source ${this.source}")
            }

            if(isReleaseBranch(this.target) && !isBugfixBranch()) {
                throw new Exception("Gitflow violation, attempting to merge into a release from invalid source ${this.source}")
            }

            if(isFeatureBranch(this.target) && !isFeatureBranch()) {
                throw new Exception("Gitflow violation, attempting to merge into a feature from invalid source ${this.source}")
            }

            if(isHotfixBranch(this.target) && !isBugfixBranch()) {
                throw new Exception("Gitflow violation, attempting to merge into a hotfix from invalid source ${this.source}")
            }

            if(isBugfixBranch(this.target)) {
                throw new Exception("Gitflow violation, attempting to merge into a bugfix, this is not permitted for a user")
            }

            if(isMasterBranch(this.target)) {
                throw new Exception('Gitflow violation, attempting to merge into master, this is not permitted by a user')
            }
        }
    }

    @NonCPS
    def isMasterBranch(branch = this.source) {
        return branch == 'master'
    }

    @NonCPS
    def isDevelopBranch(branch = this.source) {
        return branch == 'develop'
    }

    @NonCPS
    def isHotfixBranch(String branch = this.source) {
        return hasPrefix(branch, 'hotfix/')
    }

    @NonCPS
    def isReleaseBranch(String branch = this.source) {
        return hasPrefix(branch, 'release/')
    }

    @NonCPS
    def isBugfixBranch(String branch = this.source) {
        return hasPrefix(branch, 'bugfix/')
    }

    @NonCPS
    def isFeatureBranch(String branch = this.source) {
        return hasPrefix(branch, 'feature/')
    }

    @NonCPS
    private boolean hasPrefix(String haystack, String needle) {
        return haystack.startsWith(needle) && ((haystack.length() - needle.length()) > 0)
    }

    @NonCPS
    def isPackageBranch(branch = this.source) {
        return isReleaseBranch(branch) || isHotfixBranch(branch)
    }

    @NonCPS
    def isIntegrationBranch(branch = this.source) {
        return isFeatureBranch(branch) || isBugfixBranch(branch)
    }

    @NonCPS
    def isMainBranch(branch = this.source) {
        return isMasterBranch(branch) || isDevelopBranch(branch)
    }

    def isPullRequest() {
        return is_pull_request
    }

    def getSourceBranch() {
        return source
    }

    def getTargetBranch() {
        return target
    }

    def getParentBranch() {
        if(this.target != null) {
            return this.target
        }
        if(isPackageBranch()) {
            return 'master'
        }
        if(isFeatureBranch()) {
            return 'develop'
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
            return source.replace("_", "-")
                         .replace("/", "_")
        }

        // Retrieves parent branch and then gets the hash when it branched off
        def parentHash = getNearestParentHash(this.getParentBranch(), this.source)

        def type = getIncrementType()
        def job = script.build(
                job: 'System_Jobs/SemVer',
                parameters: [
                    script.string(name: 'PROJECT_KEY', value: "${key}"),
                    script.string(name: 'RELEASE_TYPE', value: "${type}"),
                    script.string(name: 'GIT_TAG', value: "${tag}"),
                    script.string(name: 'PARENT_HASH', value: "${parentHash}"),
                    script.string(name: 'BASE_BRANCH', value: "${this.source}")
                ],
                propagate: true,
                wait: true)

        script.copyArtifacts(
                fingerprintArtifacts: true,
                projectName: 'System_Jobs/SemVer',
                selector: script.specific("${job.number}")
        )

        def version = script.sh(
                script: 'cat version',
                returnStdout: true
        ).trim()

        return version
    }

    String getNearestParentHash(String parentBranch, String baseBranch) {
        def result = script.sh(
                script: "./get_parent_hash.sh origin/${parentBranch} ${baseBranch}",
                returnStdout: true)
                .trim()

        if(result == null || result.length() == 0) {
            throw new Exception("Unable to retrieve hash from parent branch ${parentBranch} for base ${baseBranch}")
        }
        return result
    }

    def isBumpCommit() {
        def lastCommit = script.sh(
                script: 'git log -1',
                returnStdout: true)
        if (lastCommit.contains("[Automated commit: Prepare project for next iteration]")) {
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
        return isFeatureBranch() || isBugfixBranch() ||  shouldPackageBuild()
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
        String parentBranch = getParentBranch()
        script.sh(script: "git fetch --prune")
        return script.sh(script: """
                if [ "\$(git diff origin/${parentBranch} 2> /dev/null)" ]; then 
                    echo "yes"; 
                fi
            """, returnStdout: true) == 'yes'
    }
}