def call(name, targetDir="/jstorage") {

    // Get Jenkins job number and 
    def buildNumber = env.BUILD_NUMBER
    def BRANCH_NAME = env.BRANCH_NAME

    // Replace / with - in branch name
    def branchName = BRANCH_NAME.replaceAll("/", "-")

    // Check if targetDir folder exists
    def directory = new File(directoryPath)
    if (!(directory.exists() && directory.isDirectory())) {
        error "Cache directory does not exist"
    }

    // Create stash file
    def cacheName = "cache-${name}-${branchName}-${buildNumber}.tar.gz"
    println "Stashing cache file: ${cacheName}"
    sh 'tar -czvf "/jstorage/' + cacheName + '" .'

}