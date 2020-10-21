def call(depends, hdlCommit, linuxCommit, firmwareVersion, bootfileSource) {
    def harness =  new sdg.Gauntlet()
    harness.construct(depends, hdlCommit, linuxCommit, firmwareVersion, bootfileSource)
    return harness
}
