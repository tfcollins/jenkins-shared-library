
def call(java.util.ArrayList listOfResources, UseNFS=false) {
    assert listOfResources instanceof java.util.List

    args = ['--privileged']

    for (i = 0; i < listOfResources.size(); i++) {
        if (listOfResources[i].equalsIgnoreCase( 'MATLAB' )) {
            echo '----Adding MATLAB Resources----'
            if (UseNFS) {
                args.add('-v "/nfs/apps/MATLAB":"/usr/local/MATLAB":ro')
                args.add('-v "/nfs/apps/resources/dot_matlab":"/root/.matlabro":ro')
                args.add('-v "/nfs/apps/resources/mlhsp":"/mlhsp":ro')
            }
        else {
                args.add('-v "/usr/local/MATLAB":"/usr/local/MATLAB":ro')
                args.add('-v "/root/.matlab":"/root/.matlabro":ro')
                args.add('-v "/mlhsp":"/mlhsp":ro')
        }
        }
        else if (listOfResources[i].equalsIgnoreCase( 'Vivado' )) {
            echo '----Adding Vivado Resources----'
            if (UseNFS) {
                args.add('-v "/nfs/apps/Xilinx":"/opt/Xilinx":ro')
                args.add('-v "/nfs/apps/resources/dot_Xilinx":"/root/.Xilinxro":ro')
            }
        else {
                args.add('-v "/opt/Xilinx":"/opt/Xilinx":ro')
                args.add('-v "/root/.Xilinx":"/root/.Xilinxro":ro')
        }
            args.add('-e "LM_LICENSE_FILE=$LM_LICENSE_FILE" -e "XILINXD_LICENSE_FILE=$XILINXD_LICENSE_FILE"')
            // Zombie processed get created without this argument
            // https://stackoverflow.com/questions/55733058/vivado-synthesis-hangs-in-docker-container-spawned-by-jenkins
            args.add('--init')
        }
        else {
            args.add(listOfResources[i])
        }
        // Add correct MAC to licenses work in Docker
        withCredentials([string(credentialsId: 'MAC_ADDR', variable: 'MAC_ADDR')]) {
            args.add('--mac-address ' + MAC_ADDR)
        }
    }

    return args
}
