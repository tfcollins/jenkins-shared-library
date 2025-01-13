def call(API_VERSION, LIBRARY_REV) {
    def API = API_VERSION;
    def LIB_R = LIBRARY_REV;
    return {
        node('lab0 || lab1 || lab4 || merlin') {
            docker.image('ubuntu:20.04').inside() {
                stage('Checkout') {
                    sh 'apt update'
                    sh 'apt install -y git'
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
                    stage('Build Table') {
                        withEnv(['LIBREV=' + LIB_R, 'API=' + API, 'GIT_COMMIT=FETCH']) {
                            dir('packaging') {
                                sh 'apt update'
                                sh 'apt install -y python3 python3-pip'
                                sh 'python3 -m pip install -r requirements.txt'
                                sh 'python3 gen_table.py'
                                stash name: 'table', includes: 'table.md'
                                stash name: 'packages', includes: 'dls/*'
                                sh 'apt install -y zip'
                                sh 'zip -r all_packages.zip dls/'
                                archiveArtifacts artifacts: 'all_packages.zip'
                            }
                        }
                    }
                }
            }
        }
    }
}
