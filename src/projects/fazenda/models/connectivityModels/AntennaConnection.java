
package projects.fazenda.models.connectivityModels;


import projects.fazenda.nodes.nodeImplementations.Antena;
import projects.fazenda.nodes.nodeImplementations.Boi;
import sinalgo.configuration.Configuration;
import sinalgo.configuration.CorruptConfigurationEntryException;
import sinalgo.models.ConnectivityModelHelper;
import sinalgo.nodes.Node;
import sinalgo.runtime.Global;
import sinalgo.runtime.Main;

/**
 * Implements a connection from a node to the antenna.
 */
public class AntennaConnection extends ConnectivityModelHelper {

	private static boolean initialized = false; // indicates whether the static fields of this class have already been initialized 
	private static double rMaxSquare; // we reuse the rMax value from the GeometricNodeCollection.
	
	/**
	 * The constructor reads the antenna-config settings from the config file.
	 * @throws CorruptConfigurationEntryException When there is a missing entry in the 
	 * config file.
	 */
	public AntennaConnection() throws CorruptConfigurationEntryException {
		if(! initialized) { // only initialize once
			double geomNodeRMax = Configuration.getDoubleParameter("GeometricNodeCollection/rMax");
			try {
				rMaxSquare = Configuration.getDoubleParameter("UDG/rMax");
			} catch(CorruptConfigurationEntryException e) {
				Global.log.logln("\nWARNING: Did not find an entry for UDG/rMax in the XML configuration file. Using GeometricNodeCollection/rMax.\n");
				rMaxSquare = geomNodeRMax;
			}
			if(rMaxSquare > geomNodeRMax) { // dangerous! This is probably not what the user wants!
				Main.minorError("WARNING: The maximum transmission range used for the UDG connectivity model is larger than the maximum transmission range specified for the GeometricNodeCollection.\nAs a result, not all connections will be found! Either fix the problem in the project-specific configuration file or the '-overwrite' command line argument.");
			}
			rMaxSquare = rMaxSquare * rMaxSquare;
			initialized = true;
		}
	}

	protected boolean isConnected(Node from, Node to) {
		// Antennas are hardwired - we exclude links between pairs of antennas.
		// MobileNodes are not connected among themselves
		if(from instanceof Antena && to instanceof Boi ||
				to instanceof Antena && from instanceof Boi) {
			double dist = from.getPosition().squareDistanceTo(to.getPosition());
			return dist < rMaxSquare;
		}
		return false;
	}

}
