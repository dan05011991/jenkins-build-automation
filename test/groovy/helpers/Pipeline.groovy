package helpers

import com.lesfurets.jenkins.unit.PipelineTestHelper

import static com.lesfurets.jenkins.unit.global.lib.LibraryConfiguration.library
import static com.lesfurets.jenkins.unit.global.lib.ProjectSource.projectSource

class Pipeline {
    static void setupLibrary(PipelineTestHelper helper) {
        def library = library().name('commons')
                .defaultVersion('<notNeeded>')
                .allowOverride(true)
                .implicit(true)
                .targetPath('<notNeeded>')
                .retriever(projectSource())
                .build()
        helper.registerSharedLibrary(library)
        helper.clearCallStack()
    }

    static Map<String, String> getScript() {
        return [
                sh           : {
                    Map<String, String> items ->
                        def script = items.get('script')
                        if (script.contains('cat version')) {
                            return '1.0.1'
                        }
                        if (script.contains('get_parent_hash.sh')) {
                            return '1234567890'
                        }
                        throw new Exception('Invalid use of sh')
                },
                string       : {
                    return ""
                },
                build        : {
                    return [
                            number: '12345'
                    ]
                },
                specific     : {
                    return ""
                },
                copyArtifacts: {
                    return ""
                }
        ]
    }
}
