def call(project, filename) {
  
  root = ''
  ext = ''
  if (project == 'hdl') {
    target = root+'hdl'
  }
  else if (project == 'TransceiverToolbox') {
    ext = ".mltbx"
    target = 'toolboxes/'
    
    def branch = env.BRANCH_NAME
    if (!env.BRANCH_NAME) {
       println("Branch name not found in environment, checking through git")
       sh 'git branch > branchname'
       sh 'sed -i "s/[*]//" branchname'
       branch = readFile('branchname').trim()
    }
    
    println("Found branch: "+branch)
    if (branch == 'master') {
      target = target+'master'
    }
    else {
      target = target+"dev/"+branch
    }
    
  }
  println("---FTP pre-target root")
  println(target)
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
  sh 'ls '+filename+' > files_searched || true'
  def files_list = readFile('files_searched')
  println("Files found: "+files_list)
  if (files_list.length() <= 0) {
    println("No files to upload");
    return;
  }
   
  // Set FTP settings
  withCredentials([usernamePassword(credentialsId: 'FTP_USER', passwordVariable: 'FTP_PASS', usernameVariable: 'FTP_USERNAME')]) {
  withCredentials([string(credentialsId: 'FTP_SERVER', variable: 'FTP_SERVER')]) {
  withCredentials([string(credentialsId: 'FTP_ROOT_TARGET', variable: 'FTP_ROOT_TARGET')]) {
  
      upload_target = FTP_ROOT_TARGET+target
      println("Uploading: $filename")
      println("Target: $upload_target")
      sh 'lftp -e "set ssl:verify-certificate no; cd \"$upload_target\"; ls; bye" -u $FTP_USERNAME,$FTP_PASS $FTP_SERVER'
      
  }}}

}
