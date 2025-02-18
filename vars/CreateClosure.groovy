def call(variant, IIO_ENABLED, PACKAGE_ENABLED, PYPACKAGE_ENABLED, ISARM, DOC_ENABLED, TEST_ENABLED, API_VERSION) {

    def OS = variant[0]
    def OS_VERSION = variant[1]
    def IMAGE = variant[2]
    def IIO = IIO_ENABLED
    def PACKAGE = PACKAGE_ENABLED
    def PYPACKAGE = PYPACKAGE_ENABLED
    def DOC = DOC_ENABLED
    def TEST = TEST_ENABLED
    def API = API_VERSION;
    def ARCH;
    def INSIDE;

    if (ISARM == true) {
        ARCH = variant[3]
        INSIDE = '-u root -v /usr/bin/qemu-arm-static:/usr/bin/qemu-arm-static --platform=linux/' + ARCH + '  --cap-add=SYS_PTRACE --security-opt seccomp=unconfined --privileged'
    } else {
        ARCH = 'x86_64'
        INSIDE = '-u root'
    }

    return { node('docker') {
            sh 'hostname'
            docker.image(IMAGE).inside(INSIDE) {
                ws(OS+'_'+OS_VERSION+'_'+ARCH+'_'+API){
                withEnv(['OS=' + OS, 'OS_VERSION=' + OS_VERSION, 'IIO=' + IIO,
                         'TEST=' + TEST, 'PACKAGE=' + PACKAGE, 'PYPACKAGE=' + PYPACKAGE,
                         'DOC=' + DOC, 'ARCH=' + ARCH, 'IMAGE=' + IMAGE, 'API=' + API]) {
                    try {
                        stage('Checkout') {
                            if (OS=='fedora') {
                                sh 'yum install -y git'
                            }
                            if (OS=='debian') {
                                sh 'apt update'
                                sh 'apt install -y git'
                            }
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
                        if (DOC == 'true') {
                            unstash 'table'
                        }
                        // Move Helper scripts to API folders
                        sh 'cp build_scripts/* $API/'
                        dir(API) {
                            sh 'git config --global --add safe.directory "*"'
                            sh 'git config --global --add safe.directory "/scratch/*"'
                            stage('Install dependencies') {
                                sh 'chmod +x run_preinstall.sh && ./run_preinstall.sh'
                            }
                            stage('Build and Tests Standalone') {
                                sh 'chmod +x run_build.sh && ./run_build.sh'
                            }
                            dir('navassa-profile-gen') {
                                // dir('_packages') {
                                //     uploadArtifactory('NavassaProfileGen', '*')
                                // }
                                // archiveArtifacts artifacts: '_packages/**'
                                // stash name: 'packages' + OS + '_' + OS_VERSION + '_' + ARCH, includes: '_packages/**'
                                if (DOC == 'true') {
                                    sh 'mkdir docs'
                                    sh 'cp -r build/src/libadrv9002-iio/docs/sphinx/* docs/'
                                    publishHTML([allowMissing: false, alwaysLinkToLastBuild: false, keepAll: true, reportDir: 'docs', reportFiles: 'index.html', reportName: 'libadrv9002-iio Documentation'])
                                }
                            }
                        }
                        dir('_packages') {
                            uploadArtifactory('NavassaProfileGen', '*')
                        }
                        archiveArtifacts artifacts: '_packages/**'
                        stash name: 'packages' + OS + '_' + OS_VERSION + '_' + ARCH, includes: '_packages/**'
                    }
                    finally {
                        cleanWs()
                    }
                         }
            }
            }
    }
}
}
