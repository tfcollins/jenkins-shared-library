def call() {

    def message = '';
    node('master') {
        checkout scm
        message = sh (script: 'git log -1 --pretty=%B', returnStdout: true).trim()
        echo 'GIT_COMMIT_MESSAGE: '+message
        cleanWs();
    }

    // Parse message for keys and values following CI: marker
    def operations = [];
    def lines = message.split('\n')
    def ci = false
    for (line in lines) {
        if (line.contains('CI:')) {
            ci = true
        }
        if (ci) {
            line = line.replace('CI:','')
            def parts = line.split(';')
            for (part in parts) {
          		part = part.trim()
                key = part.split('=')[0]
                String val0 = part.split('=')[1]
                val1 = null;
                if (val0.contains(':')) {
                  val0 = val0.split(':')[0]
                  val1 = val0.split(':')[1]
                  operations.push([key,val0,val1])
                } else
                  operations.push([key,val0])
            }
            break
        }
    }
    // Print out the map
    println 'Parsed signals:'
    for (entry in operations) {
        println entry
    }

    return operations;

}