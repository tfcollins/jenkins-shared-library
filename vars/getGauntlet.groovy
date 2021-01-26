def call(depends, hdlBranch, linuxBranch, firmwareVersion, bootfile_source, libad9361Version=null) {
    def harness =  new sdg.Gauntlet()
    harness.construct(depends, hdlBranch, linuxBranch, firmwareVersion, bootfile_source, libad9361Version)
    return harness
}
