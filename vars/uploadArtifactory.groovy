def call(project, branch, targetname, filepattern) {
  
  def server = Artifactory.server "nuc-docker"

  root = 'sdg-generic-development/'
  if (project == 'hdl') {
    target = root+'hdl'
  }
  else if (project == 'TransceiverToolbox') {
    target = root+'TransceiverToolbox'
  }
  target = target+"/"+targetname
  
  // Example layout
  // master
  //  TransceiverToolbox/master/trx-toolbox-hash
  //  Last 4 kept for master
  // others
  //  TransceiverToolbox/dev/branch/trx-toolbox-hash
  //  Last 2 kept
  //  Folder deleted when merged into master after 7 days
  // release
  //  TransceiverToolbox/release/trx-toolbox-tag
  
  echo '-------------------'
  echo env.JOB_NAME
  echo '-------------------'
  def env = System.getenv()
  println(env['JENKINS_HOME'])
  echo '-------------------'
  println(env['JOB_NAME'])
  echo '-------------------'
  println(env['BRANCH_NAME'])
  echo '-----printenv-------'
  sh 'printenv'
  echo '-------------------'
  echo ${BRANCH_NAME}  
  echo '-------------------'
  //echo ${GIT_BRANCH#*/}
  echo '-------------------'
  
  def uploadSpec = """{
    "files": [
      {
        "pattern": "${filepattern}",
        "target": "${target}"
      }
   ]
  }"""
  
  echo "-----Artifactory Upload Spec-----"
  echo uploadSpec
  
  
  //server.setProps spec: setPropsSpec, props: “p1=v1;p2=v2”, failNoOp: true
  
  
  // Collect meta
  buildInfo = Artifactory.newBuildInfo()
  
  // Do the upload Pew pew
  def buildInfoUL = server.upload spec: uploadSpec

  // Merge the upload and build-info objects.
  buildInfo.append buildInfoUL

  // Publish the build to Artifactory
  server.publishBuildInfo buildInfo
  
  
}

@NonCPS
def shellout(command) {
 
  //def command = "git --version"
  def proc = command.execute()
  proc.waitFor()              

  println "Process exit code: ${proc.exitValue()}"
  println "Std Err: ${proc.err.text}"
  println "Std Out: ${proc.in.text}"
  
  return proc.in.text
}

