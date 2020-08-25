Gauntlet Hardware Pipeline
==========================

The main purpose of this shared library is to provide a standardize pipeline for software project that want to run code against hardware targets.

.. graphviz:: pipeline_ex.dot


|
|

Example Jenkinsfile
-------------------

.. code:: groovy

        // Pipeline

        @Library('tfc-lib') _ // Not necessary when we turn on global libraries :)

        def dependencies = ["nebula","libiio","libiio-py"]
        def hdlBranch = "hdl_2019_r1"
        def linuxBranch = "2019_R1"
        def bootfile_source = 'artifactory' // options: sftp, artifactory, http, local
        def harness = getGauntlet(dependencies, hdlBranch, linuxBranch, bootfile_source)

        // Set required board (Fail otherwise)va
        // If not set all available hardware is used
        // harness.set_required_hardware(["zynq-adrv9361-z7035-fmc"])

        // Set stages (stages are run sequentially on agents)
        harness.add_stage(harness.stage_library("UpdateBOOTFiles"))
        harness.add_stage(harness.stage_library("LinuxTests"))
        harness.add_stage(harness.stage_library("PyADITests"))
        harness.add_stage(harness.stage_library("CollectLogs"))
        //Above is equivalent to harness.set_default_stages()


        // Custom test stage
        def mytest = {
            stage("Example Stage") {    
                 sh 'echo "Run my custom closure"'
                 sh 'echo "pew pew"'
            }
        }
        //harness.add_stage(mytest) // Uncomment to add stage to end of agent parallel pipeline

        // Go go
        harness.run_stages()

