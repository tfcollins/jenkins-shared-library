def call(depends, hdlBranch, linuxBranch, bootfile_source){
    def harness =  new sdg.Gauntlet()
    harness.construct(depends, hdlBranch, linuxBranch, bootfile_source)
    return harness
}
