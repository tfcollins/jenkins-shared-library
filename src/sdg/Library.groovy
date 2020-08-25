
class Library {

    def stage(String stage_name) {
        switch (stage_name) {
      case 'UpdateBOOTFiles':
                println('Added Stage UpdateBOOTFiles')
                cls = {
                    stage('Update BOOT Files') {
                        nebula('dl.bootfiles --design-name=' + board)
                        nebula('manager.update-boot-files --folder=outs')
                    }
        };
                break
      case 'CollectLogs':
                println('Added Stage CollectLogs')
                cls = {
                    stage('Collect Logs') {
                        echo 'Collect Logs'
                    }
        };
                break
      case 'PyADITests':
                cls = {
                    stage('Run Python Tests') {
                        ip = nebula('uart.get-ip')
                        println('IP: ' + ip)
                        sh 'git clone https://github.com/analogdevicesinc/pyadi-iio.git'
                        dir('pyadi-iio')
              {
                            sh 'ls'
                            run('pip3 install -r requirements.txt')
                            run('pip3 install -r requirements_dev.txt')
                            run('pip3 install pylibiio')
                            run("python3 -m pytest -v -s --uri='ip:"+ip+"' -m " + board.replaceAll('-', '_'))
              }
                    }
                }
                break
        default:
          throw new Exception('Unknown library stage: ' + stage_name)
        }

        return cls
    }

}
