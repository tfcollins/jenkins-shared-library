package sdg

/** A map that holds all constants and data members that can be override when constructing  */
gauntEnv

/**
 * Imitates a constructor
 * Defines an instance of Consul object. All according to api
 * @param ip - ip for consul
 * @param port - port for consul
 * @return
 */
def construct(List dependencies){

    gauntEnv = [
            ip : ip,
            port: port,
            agents_online: '',
            debug: false // default value that can be overwritten
    ]

    gauntEnv.agents_online = getOnlineAgents()
}

def print_agents() {
    println(gauntEnv.agents_online)
}


@NonCPS
def getOnlineAgents(){
    def jenkins = Jenkins.instance
    def online_agents = []
    for (agent in jenkins.getNodes()) {
      def computer = agent.computer
      println(agent)
      println(computer.name)
      if (computer.name=="alpine")
        continue;
      if (!computer.offline)
        online_agents.add(computer.name)
    }
    println(online_agents)
    return online_agents
}

def checkOs(){
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
