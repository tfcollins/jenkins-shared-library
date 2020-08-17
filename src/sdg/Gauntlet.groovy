package sdg

/** A map that holds all constants and data members that can be override when constructing  */
gauntEnv

/**
 * Imitates a constructor
 * Defines an instance of Consul object. All according to api
 * @param dependencies - List of strings which are names of dependencies
 * @return
 */
def construct(List dependencies){

    gauntEnv = [
            dependencies: dependencies,
            agents_online: '',
            debug: false,
            board_map: [:],
            agents: '',
            boards: '',
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
          board = nebula("update-config board-config board-name")
          println("BOARD: "+board)
          board_map[agent_name] = board
        }
      }
    }

  }

  stage('Get Available\nTest Boards') {
      parallel jobs
  }

  println(board_map)
  gauntEnv.board_map = board_map
  (agents, boards) = splitMap(board_map)
  gauntEnv.agents = agents
  gauntEnv.boards = boards
}

def test_stage(board,ip) {
  stage("Test"){
    println("IP: "+ip)
    println("board: "+board)
  }
}

def run_agents() {

  // Start stages for each node with a board
  jobs = [:]
  for (i=0; i<gauntEnv.boards.size(); i++) {

    def agent = gauntEnv.agents[i]
    def board = gauntEnv.boards[i]

    println("Agent: "+agent+" Board: "+board)

    jobs[board] = {
      node(agent) {

        stage('Update BOOT files') {
          nebula("dl.bootfiles --design-name="+board)
          nebula("manager.update-boot-files --folder=outs")
        }

        test_stage(board,"123")

        stage('Run Tests') {
          ip = nebula("uart.get-ip")
          println("IP: "+ip)
          sh "git clone https://github.com/analogdevicesinc/pyadi-iio.git"
          dir("pyadi-iio")
          {
              sh "ls"
              run("pip3 install -r requirements.txt")
              run("pip3 install -r requirements_dev.txt")
              run("pip3 install pylibiio")
              run("python3 -m pytest -v -s --uri='ip:"+ip+"' -m "+board.replaceAll("-","_"))
          }
        }

        stage('Collect Logs') {
          echo "Collecting logs"
        }

      }
    }

  }

  stage('Update and Test') {
      parallel jobs
  }

}


def run_stages() {

  setup_agents()
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
    println(script_out)
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

    println(out)
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

/*
private def run(cmd){
    if (checkOs()=="Windows") {
        bat cmd
    }
    else {
        sh cmd
    }
}
*/
