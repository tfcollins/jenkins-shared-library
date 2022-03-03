/* Call examples
mystages = {
    stage("TEST1")
    {
       sh "echo TEST1"
    }
    stage("TEST2")
    {
        sh "echo TEST2"
    }
}

////////////////////////////
def dockerArgs2 = '--privileged'
def branches2 = ['ZED','ZC706']

buildParallelWithDocker(branches2, dockerArgs2, mystages)

buildParallelWithDocker(branches2, dockerArgs2){
  stage("TEST1")
  {
     sh "echo TEST1"
  }
  stage("TEST2")
  {
      sh "echo TEST2"
  }
}
*/

def call(branchNames, dockerHost, dockerArgs, dockerstages) {
    def tests = [:]

    if (dockerArgs instanceof List) {
        dockerArgs = dockerArgs.join(' ')
    }

    for ( i = 0; i < branchNames.size(); i++ ) {
        def bn = branchNames[i]
        
        if  (bn.class == String){
            def branchName = bn;
        }
        else {
            def branchName = bn.join('_')
        }

        tests[branchName] = {
            node (label: dockerHost) {
                stage (branchName) {
                    docker.image('tfcollins/hdl-ci:latest').inside(dockerArgs) {
                        sh 'chmod +x /usr/local/bin/docker-entrypoint.sh'
                        sh '/usr/local/bin/docker-entrypoint.sh'
                        dockerstages(bn)
                    }
                    cleanWs()
                }
            }
        }
    }

    parallel tests
}
