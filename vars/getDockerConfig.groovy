
def call(java.util.ArrayList listOfResources, UseNFS=false){

  assert listOfResources instanceof java.util.List

  args = ['--privileged']

  for (i = 0; i<listOfResources.size(); i++) {

    if (listOfResources[i].equalsIgnoreCase( 'MATLAB' )) {
        echo "----Adding MATLAB Resources----"
        if (UseNFS) {
          args.add('-v "/nfs/apps/MATLAB":"/usr/local/MATLAB":ro')
          args.add('-v "/nfs/apps/resources/dot_matlab":"/root/.matlab":ro')
          args.add('-v "/nfs/apps/resources/mlhsp":"/mlhsp":ro')
        }
        else {
          args.add('-v "/usr/local/MATLAB":"/usr/local/MATLAB":ro')
          args.add('-v "/root/.matlab":"/root/.matlab":ro')
          args.add('-v "/mlhsp":"/mlhsp":ro')
        }
    }
    if (listOfResources[i].equalsIgnoreCase( 'Vivado' )) {
        echo "----Adding Vivado Resources----"
        if (UseNFS) {
          args.add('-v "/nfs/apps/Xilinx":"/opt/Xilinx":ro')
          args.add('-v "/nfs/apps/resources/dot_Xilinx":"/root/.Xilinx":ro')
        }
        else {
          args.add('-v "/opt/Xilinx":"/opt/Xilinx":ro')
          args.add('-v "/root/.Xilinx":"/root/.Xilinx":ro');
        }
    }
    // Add correct MAC to licenses work in Docker
    withCredentials([string(credentialsId: 'MAC_ADDR', variable: 'MAC_ADDR')]) {
      args.add('--mac-address '+MAC_ADDR)
    }

  }

  return args
}
