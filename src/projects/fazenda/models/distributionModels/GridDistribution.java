
package projects.fazenda.models.distributionModels;


import java.util.Vector;

import sinalgo.configuration.Configuration;
import sinalgo.configuration.CorruptConfigurationEntryException;
import sinalgo.models.DistributionModel;
import sinalgo.nodes.Position;
import sinalgo.tools.statistics.Distribution;

public class GridDistribution extends DistributionModel {

	private java.util.Random rand = Distribution.getRandom();
	
	double radius = 0;
	double horizontalFactor = 0;
	double verticalFactor = 0;
	
	private Vector<Position> positions = new Vector<Position>();
	private int returnNum = 0;
	
	public void initialize(){
		try {
			radius = Configuration.getDoubleParameter("GeometricNodeCollection/rMax");
		} catch (CorruptConfigurationEntryException e) {
			e.printStackTrace();
		}
                // 6.0113145173  =               (1050 - 2 * 100 )/( 100  *1.414)
		horizontalFactor = (Configuration.dimX - 2*radius)/(radius*1.414);
		verticalFactor = (Configuration.dimY - 2*radius)/(radius*1.414);
		
		int ihF = (int)horizontalFactor; //6
		int ivF = (int)verticalFactor; //6
		
		int number = 0;
		
		for(int i = 0; i < ihF+1; i++){
			for(int j = 0; j < ivF+1; j++){
				if(number < numberOfNodes){
					positions.add(new Position(radius + i*(radius*1.414), radius + j*(radius*1.414), 0));
				}
			}
		}
	}
	
	@Override
	public Position getNextPosition() {
		if(returnNum < positions.size()){
			return positions.elementAt(returnNum++);
		}
		else{
			double randomPosX = rand.nextDouble() * Configuration.dimX;
			double randomPosY = rand.nextDouble() * Configuration.dimY;
			return new Position(randomPosX, randomPosY, 0);
		}
	}
	
	public void setParamString(String s){}

}
