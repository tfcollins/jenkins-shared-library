
deff call(java.util.ArrayList listOfResources){

  //assert listOfResources instanceof java.util.List

  args = ['--privileged']

  for (i = 0; i<listOfResources.size(); i++) {

    if (listOfResources[i] == 'MATLAB') {
        args.add('-v "/nfs/apps/MATLAB":"/usr/local/MATLAB":ro')
        args.add('-v "/nfs/apps/resources/dot_matlab":"/root/.matlab":ro')
        args.add('-v "/nfs/apps/resources/mlhsp":"/mlhsp":ro')
    }
    if (listOfResources[i] == 'Vivado') {
        args.add('-v "/nfs/apps/Xilinx":"/opt/Xilinx":ro')
        args.add('-v "/nfs/apps/resources/dot_Xilinx":"/root/.Xilinx":ro')
    }

  }

}
