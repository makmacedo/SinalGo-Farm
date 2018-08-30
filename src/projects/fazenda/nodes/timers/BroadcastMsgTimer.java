package projects.fazenda.nodes.timers;

import projects.fazenda.nodes.messages.BroadcastMsg;
import projects.fazenda.nodes.nodeImplementations.Antena;
import sinalgo.configuration.Configuration;
import sinalgo.configuration.CorruptConfigurationEntryException;
import sinalgo.nodes.timers.Timer;
import sinalgo.tools.Tools;
import sinalgo.tools.statistics.Distribution;

/**
 * The antennas send periodically an invite message - this timer triggers 
 * the broadcast.
 */
public class BroadcastMsgTimer extends Timer {
	Distribution dist = null;
	int refreshRate = 0;
	int refreshCounter = 0;
	
	// If set to true, the antenna requires the nodes to register again
	// such that it can drop old mobileNodes 
        
	public boolean requireSubscription = false; 
	
	public BroadcastMsgTimer() {
		try {
                        dist = Distribution.getDistributionFromConfigFile("Antena/IntervaloBroadcast");
			refreshRate = Configuration.getIntegerParameter("Antena/refreshRate");
		} catch (CorruptConfigurationEntryException e) {
			Tools.fatalError(e.getMessage());
		}
	}
	
	@Override
	public void fire() {
		BroadcastMsg msg = new BroadcastMsg();
		refreshCounter--;
		if(refreshCounter <= 0) {
			((Antena) this.node).resetNeighborhood();
			msg.obrigaConectar = true;
			refreshCounter = refreshRate; // reset the counter
		}
		
		this.node.broadcast(msg);
		double time = dist.nextSample();
		if(time <=0) {
			Tools.fatalError("Invalid offset time for inviteInterval: " + time + " is <= 0.");
		}
		this.startRelative(time, this.node);
	}
}
