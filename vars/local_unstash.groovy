def call(name, targetDir="/jstorage", inDocker=true) {

    // Get Jenkins job number and 
    def buildNumber = env.BUILD_NUMBER
    def BRANCH_NAME = env.BRANCH_NAME

    // Replace / with - in branch name
    def branchName = BRANCH_NAME.replaceAll("/", "-")

    // Check if targetDir folder exists
    def directoryExists = sh script: "ls -d ${targetDir}", returnStatus: true
    if (directoryExists != 0) {
        error "Cache directory does not exist"
    }

    if (! inDocker) {
        targetDir = "/home/tcollins/jstorage"
    }

    // unstash file
    def cacheName = "cache-${name}-${branchName}-${buildNumber}.tar.gz"
    println "Unstashing cache file: ${cacheName}"
    // Check if cache file exists
    def fileExists = sh script: "ls -d ${targetDir}/${cacheName}", returnStatus: true
    if (fileExists != 0) {
        error "Cache file does not exist"
    }
    sh 'tar -xvf  "' + targetDir + '/' + cacheName + '" -C .'
}