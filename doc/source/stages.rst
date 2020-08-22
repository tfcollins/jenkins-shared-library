Pipeline Stage Library
======================

The common pipeline stages are:

Update board boot files: ``UpdateBOOTFiles``

.. code-block:: groovy

    stage("Update BOOT Files") {
      board = nebula("update-config board-config board-name")
      nebula("dl.bootfiles --design-name="+board)
      nebula("manager.update-boot-files --folder=outs")
    }

Collect XML test logs: ``CollectLogs``

.. code-block:: groovy

    stage("Collect Logs") {
      echo "Collect Logs"
    }

Basic Linux testing: ``LinuxTests``

.. code-block:: groovy

    stage("Linux Tests") {
      ip = nebula("uart.get-ip")
      nebula("net.check-dmesg --ip='"+ip+"'")
      nebula('driver.check-iio-devices --uri="ip:'+ip+'"')
    }

Device specific pyadi-iio based testing: ``PyADITests``

.. code-block:: groovy

    stage('Run Python Tests') {
      ip = nebula("uart.get-ip")
      board = nebula("update-config board-config board-name")
      println("IP: "+ip)
      sh "git clone https://github.com/analogdevicesinc/pyadi-iio.git"
      dir("pyadi-iio")
      {
          run_i("pip3 install -r requirements.txt")
          run_i("pip3 install -r requirements_dev.txt")
          run_i("pip3 install pylibiio")
          run_i("mkdir testxml")
          run_i("python3 -m pytest --junitxml=testxml/reports.xml -v -k 'not stress' -s --uri='ip:"+ip+"' -m "+board.replaceAll("-","_"))
          junit testResults: 'test/*.xml', allowEmptyResults: true
      }
    }
