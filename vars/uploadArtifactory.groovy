def call(project, filepattern) {

  //def server = Artifactory.newServer url: SERVER_URL, credentialsId: CREDENTIALS

  if (project == 'hdl') {
    target = 'hdl'
  }
  else if (project == 'TransceiverToolbox') {
    target = 'TransceiverToolbox'
  }
  
  
  def uploadSpec = """{
    "files": [
      {
        "pattern": "${filepattern}",
        "target": "${target}"
      }
   ]
  }"""
  
  echo uploadSpec
  
  // Do the upload Pew pew
  //server.upload spec: uploadSpec


}
