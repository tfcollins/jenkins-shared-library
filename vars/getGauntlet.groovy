def call(depends){
    def harness =  new sdg.Gauntlet()
    harness.construct(depends)
    return harness
}
