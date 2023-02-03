def call(String toolbox, String branch, String build) {
  
    cmd = 'curl -l -u "tcollins":"'
    withCredentials([string(credentialsId: 'JENKINS_HW_SERVER_TOKEN', variable: 'JENKINS_HW_SERVER_TOKEN')]) {
      cmd = cmd + JENKINS_HW_SERVER_TOKEN + '" '
    }
    withCredentials([string(credentialsId: 'JENKINS_HW_SERVER_IP', variable: 'JENKINS_HW_SERVER_IP')]) {
      cmd = cmd + '"http://' + JENKINS_HW_SERVER_IP + '/jenkins/job/MATLAB_BOOT_BIN_Testing/buildWithParameters?token=trxtoolboxbootbins'
    }
    cmd = cmd+'&TOOLBOX='+toolbox
    cmd = cmd+'&BRANCH='+branch
    cmd = cmd+'&BUILD='+build
    cmd = cmd+'"'

  sh cmd
}
