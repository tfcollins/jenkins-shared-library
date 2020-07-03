def call(project, filepattern) {
  
  root = 'sdg-generic-development/'
  ext = ''
  name = 'unnamed'
  if (project == 'hdl') {
    target = root+'hdl'
  }
  else if (project == 'TransceiverToolbox') {
    ext = ".mltbx"
    name = 'trx-toolbox'
    target = root+'TransceiverToolbox'
    
    if (env.BRANCH_NAME == 'master') {
      target = target+"/master"
    }
    else {
      target = target+"/dev/"+env.BRANCH_NAME
    }
    
  }
   
  // Example layout
  // master
  //  TransceiverToolbox/master/<hash>/<files>
  //  Last 4 kept for master
  // others
  //  TransceiverToolbox/dev/branch/<hash>/<files>
  //  Last 2 kept
  //  Folder deleted when merged into master after 7 days
  // release
  //  TransceiverToolbox/release/trx-toolbox-tag/<files>
  
  // Check if we have files to upload based on target
  sh 'ls '+filepattern+' > files_searched || true'
  def files_list = readFile('files_searched')
  println("Files found: "+files_list)
  if (files_list.length() <= 0) {
    return;
  }
  
  echo '-------Artifactory Git Hash lookup-------'
  sh 'git rev-parse --short HEAD > commit'
  def commit = readFile('commit').trim()
  println("Found git hash: "+commit)
  
  // Build folder/filename
  target = target+"/"+env.BUILD_ID+"-"+commit+"/"
  
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
  
  def server = Artifactory.server "nuc-docker"
  
  // Do the upload Pew pew
  server.upload spec: uploadSpec
  echo "Upload Complete"

  //def buildInfoUL = server.upload spec: uploadSpec

  // Merge the upload and build-info objects.
  //buildInfo.append buildInfoUL

  // Publish the build to Artifactory
  //server.publishBuildInfo buildInfo
  
  
}

@NonCPS
def shellout(command) {
 
  //def command = "git --version"
  def proc = command.execute()
  proc.waitFor()              

  println "Process exit code: ${proc.exitValue()}"
  println "Std Err: ${proc.err.text}"
  def val = proc.in.text
  
  return val
}
