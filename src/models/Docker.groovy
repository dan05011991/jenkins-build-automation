package models

class Docker {
    private final def script
    private final Gitflow gitflow

    Docker(Map<String, ?> items) {
        this.script = items.get("script")
        this.gitflow = items.get("gitflow")
    }

    def doesDockerImageExist(image) {
        def result = script.sh(script: "docker pull ${image} > /dev/null 2>&1; echo \$?", returnStdout: true)
                           .trim()
        return result == '0'
    }

    def getDockerTag(version) {
        if (gitflow.isMasterBranch()) {
            return version
        } else if (gitflow.isDevelopBranch()) {
            return version + '-SNAPSHOT'
        } else if (gitflow.isPackageBranch()) {
            return version + '-release-candidate'
        }
        throw new Exception("Attempting to get docker tag for a branch which is not allowed")
    }

    def getReferenceTag(version) {
        if (gitflow.isMasterBranch()) {
            return version + '-release-candidate'
        } else if (gitflow.isPackageBranch()) {
            return version + '-SNAPSHOT'
        }
        throw new Exception("Attempting to get reference tag for a branch which is not allowed")
    }
}