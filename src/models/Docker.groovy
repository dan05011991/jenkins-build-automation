package models

class Docker {
    private final def script
    private final Gitflow gitflow
    private final String developerRepo = 'hub.docker.com'
    private final String releaseRepo = 'hub.docker.com'

    Docker(Map<String, ?> items) {
        this.script = items.get("script")
        this.gitflow = items.get("gitflow")
    }

    def doesDockerImageExist(image, tag) {
        try {
            script.sh(script: "docker pull ${developerRepo}/${image}:${tag}")
            return true
        } catch(Exception ex) {
            return false
        }
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