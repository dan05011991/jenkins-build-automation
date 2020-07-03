package models

class VersionManager {
    private def final script
    private final Map<String, String> map
    private final String projectKey
    private final String incrementType
    private final String closestGitTag
    private final String parentHash
    private final String baseBranch

    VersionManager(Map<String, ?> items) {
        this.map = new HashMap<>()
        this.script = getInput(items, 'script')
        this.projectKey = getInput(items, 'projectKey')
        this.incrementType = getInput(items, 'incrementType')
        this.closestGitTag = getInput(items, 'closestGitTag')
        this.parentHash = getInput(items, 'parentHash')
        this.baseBranch = getInput(items, 'baseBranch')
        this.map = readFile(this.projectKey)


        if(this.incrementType == 'M') {
            throw new Exception('Major version updates are not currently supported')
        }

        if (this.incrementType != 'm' && this.incrementType != 'p') {
            throw new Exception('Incorrect use of the release type flag')
        }
    }

    String updateVersion() {
        String version = getUpdatedVersion()
        writeFile(this.map, this.projectKey)
        return version
    }

    private String getUpdatedVersion() {
        String lookupKey = getLookupKey(this.incrementType)

        // If the key doesn't exist, create it and add the latest git tag in as the base version
        if(!map.containsKey(lookupKey)) {
            map[lookupKey] = this.closestGitTag
        }

        // If we are generating a minor release, check if we have issued a version to this specific branch
        if(this.incrementType == 'm') {
            String key = parentHash + '-' + this.baseBranch
            if(map.containsKey(key)) {
                map[key] = callSemVerScript('p', map[key])
            } else {
                map[lookupKey] = map[key] = callSemVerScript('m', map[lookupKey])
            }
            return map[key]
        }

        def majorMinorOnlyClosestGitTag = removePatchVersion(this.closestGitTag)
        def majorMinorOnlySavedVersion = removePatchVersion(map[lookupKey])

        if (majorMinorOnlyClosestGitTag == majorMinorOnlySavedVersion) {
            map[lookupKey] = callSemVerScript(this.incrementType, map[lookupKey])
        } else {
            map[lookupKey] = callSemVerScript(this.incrementType, majorMinorOnlyClosestGitTag, true)
        }

        return map[lookupKey]
    }

    private String removePatchVersion(String tag) {
        assert tag.length() > 0

        def majorMinorTag = tag.substring(0, tag.lastIndexOf("."))
        assert majorMinorTag.length() > 0

        return majorMinorTag
    }

    private String getLookupKey(type) {
        if(type == 'p') {
            return 'patch'
        } else if(type == 'm') {
            return 'release'
        }
        return new Exception('Invalid lookup type, unable to determine key')
    }

    private String callSemVerScript(String incrementType, String version, boolean  appendPatch = false) {
        String extra = ''

        if(appendPatch) {
            extra = '.0'
        }

        return script.sh(
                script: "echo \"\$(./semver.sh -${incrementType} ${version}${extra})\"",
                returnStdout: true)
                .trim()
    }

    private def getInput(Map<String, ?> items, String key) {
        def result = items.get(key)

        if(result == null) {
            throw new Exception(String.format("Input for key '%s' cannot be null", key))
        }

        return result
    }

    private Map<String, String> readFile(fileName) {
        def map = [:]
        def file = new File(fileName)
        def lines = file.readLines()

        lines.each { line ->
            def items = "${line}".split('=')

            if(items.length != 2) {
                throw new Exception("Incorrect format of version file")
            }

            map[items[0]] = items[1]
        }

        return map
    }

    void writeFile(Map<String, String> map, fileName) {
        FileWriter fw = new FileWriter(fileName)

        map.each { key, value ->
            fw.write(String.format('%s=%s\n', key,value))
        }

        fw.close()
    }
}
