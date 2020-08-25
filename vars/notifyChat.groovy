def call(project, message) {
    if (project == 'TransceiverToolbox') {
        channel = '#matlab'
    }
  else if (project == 'hdl') {
        channel = '#hdl'
  }
  else {
        channel = '#random'
  }

    slackSend(channel: channel, message: message)
}
