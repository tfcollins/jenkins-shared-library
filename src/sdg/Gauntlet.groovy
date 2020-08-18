package sdg

/** A map that holds all constants and data members that can be override when constructing  */
gauntEnv

/**
 * Imitates a constructor
 * Defines an instance of Consul object. All according to api
 * @param dependencies - List of strings which are names of dependencies
 * @return
 */

 // def dependencies = ["nebula","libiio","libiio-py"]
// def hdlBranch = "hdl_2019_r1"
// def linuxBranch = "2019_R1"
// def bootfile_source = 'artifactory' // options: sftp, artifactory, http, local

def construct(List dependencies, hdlBranch, linuxBranch, bootfile_source){

    gauntEnv = [
            dependencies: dependencies,
            hdlBranch: hdlBranch,
            linuxBranch: linuxBranch,
            bootfile_source: bootfile_source,
            agents_online: '',
            debug: false,
            board_map: [:],
            stages: [],
            agents: '',
            boards: '',
            required_hardware: [],
            setup_called: false,
            configure_called: false
    ]

    gauntEnv.agents_online = getOnlineAgents()
}

def print_agents() {
    println(gauntEnv.agents_online)
}

def setup_agents() {

  def board_map = [:]

  // Query each agent for their connected hardware
  def jobs = [:]
  for (agent in gauntEnv.agents_online) {
    println("Agent: "+agent)

    def agent_name = agent

    jobs[agent_name] = {
      node(agent_name) {
        stage('Query agents') {
          setupAgent("nebula")
          // Get necessary configuration for basic work
          board = nebula("update-config board-config board-name")
          board_map[agent_name] = board
        }
      }
    }

  }

  stage('Get Available\nTest Boards') {
      parallel jobs
  }

  gauntEnv.board_map = board_map
  (agents, boards) = splitMap(board_map)
  gauntEnv.agents = agents
  gauntEnv.boards = boards
}

def stage_library(String stage_name){

  switch(stage_name) {
    case "UpdateBOOTFiles":
      println("Added Stage UpdateBOOTFiles")
      cls = {
        stage("Update BOOT Files") {
          nebula("dl.bootfiles --design-name="+board)
          nebula("manager.update-boot-files --folder=outs")
        }
      };
      break;
    case "CollectLogs":
      println("Added Stage CollectLogs")
      cls = {
        stage("Collect Logs") {
          echo "Collect Logs"
        }
      };
      break;
    case "PyADITests":
      cls = {
          stage('Run Python Tests') {
            ip = nebula("uart.get-ip")
            println("IP: "+ip)
            sh "git clone https://github.com/analogdevicesinc/pyadi-iio.git"
            dir("pyadi-iio")
            {
                sh "ls"
                run_i("pip3 install -r requirements.txt")
                run_i("pip3 install -r requirements_dev.txt")
                run_i("pip3 install pylibiio")
                run_i("python3 -m pytest -v -k 'not stress' -s --uri='ip:"+ip+"' -m "+board.replaceAll("-","_"))
            }
          }
        }
        break;
      default:
        throw new Exception("Unknown library stage: "+stage_name)

    }

    return cls;
}

def add_stage(cls) {
  gauntEnv.stages.add(cls)
}

def run_agents() {

  // Start stages for each node with a board
  jobs = [:]
  for (i=0; i<gauntEnv.boards.size(); i++) {

    def agent = gauntEnv.agents[i]
    def board = gauntEnv.boards[i]
    def num_stages = gauntEnv.stages.size()

    println("Agent: "+agent+" Board: "+board)

    jobs[board] = {
      node(agent) {
        for (k=0; k<num_stages; k++)
          gauntEnv.stages[k].call()
      }
    }
  }

  stage('Update and Test') {
      parallel jobs
  }

}

/**
 * Set list of required devices for test
 * Input must be a list of strings
 * Strings must be associated with a board configuration name.
 * For example: zynq-zc702-adv7511-ad9361-fmcomms2-3
 */
def set_required_hardware(List board_names) {
 assert board_names instanceof java.util.List
 gauntEnv.required_hardware = board_names
}

def check_required_hardware() {
 
 def s = gauntEnv.required_hardware.size();
 def b = gauntEnv.boards.size();

 for(i=0; i<s; i++) {
  if (! gauntEnv.boards.contains(gauntEnv.required_hardware[i]) )
   error(gauntEnv.required_hardware[i]+' not found in harness. Failing pipeline')
 }
 
}


def run_stages() {
  setup_agents()
  check_required_hardware()
  run_agents()
}

// Private methods
@NonCPS
private def splitMap(map){

    def keys = [];
    def values = [];
    for (entry in map){
        keys.add(entry.key)
        values.add(entry.value)
    }
    return [keys, values];
}

@NonCPS
private def getOnlineAgents(){
    def jenkins = Jenkins.instance
    def online_agents = []
    for (agent in jenkins.getNodes()) {
      def computer = agent.computer
      if (computer.name=="alpine")
        continue;
      if (!computer.offline)
        online_agents.add(computer.name)
    }
    println(online_agents)
    return online_agents
}

private def checkOs(){
    if (isUnix()) {
        def uname = sh script: 'uname', returnStdout: true
        if (uname.startsWith("Darwin")) {
            return "Macos"
        }
        // Optionally add 'else if' for other Unix OS
        else {
            return "Linux"
        }
    }
    else {
        return "Windows"
    }
}

private def nebula(cmd, full=false, show_log=false){
    // full=false
    if (show_log)
      cmd = "show-log " + cmd
    cmd = "nebula "+cmd
    if (checkOs()=="Windows") {
        script_out = bat(script: cmd, returnStdout: true).trim()
    }
    else {
        script_out = sh(script: cmd, returnStdout: true).trim()
    }
    // Remove lines
    if (!full){
        lines = script_out.split("\n");
        if (lines.size()==1)
            return script_out
        out = ""
        added = 0
        for (i=1; i<lines.size(); i++) {
            if (lines[i].contains("WARNING"))
                continue;
            if (added>0)
                out = out+"\n"
            out = out + lines[i]
            added = added + 1;
        }
    }
    return out
}

private def install_nebula() {
    if (checkOs()=="Windows") {
        bat 'git clone https://github.com/tfcollins/nebula.git'
        dir('nebula')
        {
            bat 'pip install -r requirements.txt'
            bat 'python setup.py install'
        }
    }
    else {
        sh 'git clone https://github.com/tfcollins/nebula.git'
        dir('nebula')
        {
            sh 'pip3 install -r requirements.txt'
            sh 'python3 setup.py install'
        }
    }
}

private def setupAgent(Object... args){
    try {
        for (i=0; i<args.length; i++) {
            println(args[i])
            if (args[i]=="nebula")
                install_nebula()
        }
    }
    finally {
        cleanWs();
    }
}


private def run_i(cmd){
    if (checkOs()=="Windows") {
        bat cmd
    }
    else {
        sh cmd
    }
}
