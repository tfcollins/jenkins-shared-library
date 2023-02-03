def call(String toolbox) {
  
    def branch = env.BRANCH_NAME
    if (!env.BRANCH_NAME) {
        println('Branch name not found in environment, checking through git')
        sh 'git branch > branchname'
        sh 'sed -i "s/[*]//" branchname'
        branch = readFile('branchname').trim()
    }
    println('Found branch: ' + branch)
  
    def commit = env.GIT_COMMIT
    if (!env.GIT_COMMIT) {
        println('Git commit hash not found in environment, checking through git')
        sh 'git rev-parse --short HEAD > commit'
        commit = readFile('commit').trim()
    }
    println('Found git hash: ' + commit)
  
    build = env.BUILD_ID + '-' + commit
  
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
