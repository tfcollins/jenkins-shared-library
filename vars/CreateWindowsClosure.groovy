def call(IIO, API_VERSION) {
    def runner_node = 'windows10'
    def PYPACKAGE = 'ON'
    def API = API_VERSION;
    return {
        // Lock node to only run 1 job at a time
        node(runner_node) {
            ws('C:/jenkins_ws/ws'+API+'_'+IIO) {
                stage('Windows Build') {
                    // vagrant_vm('win10_generic') {
                        withEnv(['IIO_ENABLE=' + IIO, 'PYPACKAGE=' + PYPACKAGE, 'API=' + API]) {
                            try {
                                stage('Build') {
                                    // checkout scm
                                    stage('Checkout') {
                                        checkout([
                                            $class: 'GitSCM',
                                            branches: scm.branches,
                                            doGenerateSubmoduleConfigurations: false,
                                            extensions: [[
                                                $class: 'SubmoduleOption',
                                                disableSubmodules: false,
                                                parentCredentials: true,
                                                recursiveSubmodules: true,
                                                reference: '',
                                                trackingSubmodules: false
                                            ]],
                                            submoduleCfg: [],
                                            userRemoteConfigs: scm.userRemoteConfigs
                                        ])
                                    }
                                    bat 'copy "build_scripts\\*" "%API%\" '
                                    dir(API) {
                                        bat '''
                                        call "C:\\Program Files (x86)\\Microsoft Visual Studio\\2019\\BuildTools\\VC\\Auxiliary\\Build\\vcvarsamd64_x86.bat"
                                        run_build.bat
                                        '''
                                        // dir('navassa-profile-gen') {
                                        //     dir('package') {
                                        //         // Will include system info txt
                                        //         uploadArtifactory('NavassaProfileGen', '*')
                                        //         archiveArtifacts artifacts: '**'
                                        //     }
                                        // }
                                    }
                                    dir('package') {
                                        // Will include system info txt
                                        uploadArtifactory('NavassaProfileGen', '*')
                                        archiveArtifacts artifacts: '**'
                                    }
                                }
                            }
                            finally {
                                cleanWs()
                            }
                        }
                    // }
                }
            //}
            }
        }
    }
}
