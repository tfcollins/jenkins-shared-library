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

def call(branchNames, dockerHost, dockerArgs, dockerstages){

  def tests = [:]
  //def dockerHost = 'ubuntu-vm'
    
  if (dockerArgs instanceof List) {
      dockerArgs = dockerArgs.join(" ")
  }

  for ( i=0; i<branchNames.size(); i++ ) {

      def branchName = branchNames[i]
      //def dockerENVS = ["-e HDLBRANCH=${hdlBranch}","-e MLRELEASE=${MATLABRelease}","-e BOARD=${boardName}"]
      //def permuted

      tests[branch] = {
          node (label: dockerHost) {

              stage (branch) {
                  docker.image('tfcollins/hdl-ci:latest').inside(dockerArgs) {
                    dockerstages()
                  }
                  cleanWs()
              }

          }
      }
  }

  parallel tests

}
