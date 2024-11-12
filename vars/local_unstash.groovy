def call(name, targetDir="/jstorage") {

    // Get Jenkins job number and 
    def buildNumber = env.BUILD_NUMBER
    def BRANCH_NAME = env.BRANCH_NAME

    // Replace / with - in branch name
    def branchName = BRANCH_NAME.replaceAll("/", "-")

    if (!fileExists(targetDir)) {
        error "Cache directory does not exist"
    }

    // unstash file
    def cacheName = "cache-${name}-${branchName}-${buildNumber}.tar.gz"
    println "Unstashing cache file: ${cacheName}"
    if (!fileExists("/jstorage/" + cacheName)) {
        error "Cache file does not exist"
    }
    sh 'tar -xvf  "/jstorage/' + cacheName + '" -C .'
}