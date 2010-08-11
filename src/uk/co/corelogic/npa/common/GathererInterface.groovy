package uk.co.corelogic.npa.metrics

/**
 * Public interface for Gatherers.  These methods should be implemented as a minimum for each Gatherer
*/
public interface GathererInterface {

    public sample(met, variables)
    public sample(met)
    public getGroupRegistration(groupReg)
    public setGroupRegistration(groupReg)
    public getAll()
    public registerMetrics()  // Gatherer must register the metric names it supplies
    

}