package helpers

class Constants {
    public static final String bumpCommit = '[Automated commit: Prepare project for next iteration]'
    public static final String[] good_branches = [
            "master",
            "develop",
            "bugfix/test",
            "hotfix/test",
            "feature/test",
            "release/test"
    ]
    public static final List<String> bad_branches = generate_bad_branches()

    static List<String> generate_bad_branches() {
        List<String> bad_branches = new ArrayList<>()
        for(String good_branch : good_branches) {
            bad_branches.add('a' + good_branch)
            if(good_branch.contains('/')) {
                bad_branches.add(good_branch.split('/')[0])
            }
        }
        return bad_branches
    }
}
