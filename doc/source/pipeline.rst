Gauntlet Hardware Pipeline
==========================

The main purpose of this shared library is to provide a standardize pipeline for software project that want to run code against hardware targets. The figure below shows an example pipeline that is generated using the Jenkinsfile below it.


.. graphviz:: pipeline_ex.dot

These pipelines have 3 main phases. Starting from the left side of the pipeline, phase 1 is the first two horizontal stages. In the first stage each agent is queried to determined available hardware. Then in the second stage, this information is return to the master node and the necessary downstream stages are determined. This will occur in all pipeline configuration using the **Gaunlet** class. The generated downstream stages will be based on how the **harness** object is configured. In the Jenkinsfile below, four stages are added from the :ref:`library-label`. These stages are run in the order in which they are added. Each of these stages will be run for each target board configuration. By default all available hardware is used in this phase. If a single board or hardware setup is desired, the *set_required_hardware* method should be used.

The final phase is for post processing, here logs and artifacts are gathered for collection. These will typically be saved to artifactory of to a logging server for analysis.

The available flags are documented for reference in the Jenkinsfile below. See the generated groovydoc for explicit syntax and all available methods.

Example Jenkinsfile
-------------------

.. code:: groovy

        // Pipeline

        @Library('tfc-lib') _ // Not necessary when we turn on global libraries :)

        // Define dependencies required on all agents
        def dependencies = ["nebula","libiio","libiio-py"]
        // Define target HDL release or branch whos related files will be collected from
        // the build server
        def hdlCommit = "hdl_2019_r1" // Can be commit hash, latest, or release branch name
        // Define target Linux release or branch whos related files will be collected from
        // the build server
        def linuxCommit = "2019_R1" // Can be commit hash, latest, or release branch name
        // Define firmware version to download for appropriate devices (M2K or Pluto)
        // If M2K or Pluto are not undertest this does nothing
        def firmwareVersion = "v0.32" // Must be in reference to a github release of form vX.XX
        // Set the source of the bootfiles to be used. This is where bootfiles will be
        // downloaded from and deploy to eat board
        def bootfileSource = 'http' // options: sftp, artifactory, http, local
        def harness = getGauntlet(dependencies, hdlCommit, linuxCommit, firmwareVersion, bootfileSource)

        // Set required board (Fail otherwise)
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

