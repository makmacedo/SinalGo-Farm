
package projects.fazenda.models.mobilityModels;

import projects.defaultProject.models.mobilityModels.RandomWayPoint;
import sinalgo.configuration.Configuration;
import sinalgo.configuration.CorruptConfigurationEntryException;
import sinalgo.nodes.Node;
import sinalgo.nodes.Position; 

public class TempIdeal extends RandomWayPoint{
    
        private final int minBoiTemp;
        private final int maxBoiTemp; 
    
	public TempIdeal() throws CorruptConfigurationEntryException{
            super();
            this.minBoiTemp = Configuration.getIntegerParameter("Boi/minIdeal");
            this.maxBoiTemp = Configuration.getIntegerParameter("Boi/maxIdeal");
	}
                
        @Override
	public Position getNextPos(Node n){
                if(n.getTemperatura()< maxBoiTemp && n.getTemperatura()> minBoiTemp)
                    return n.getPosition(); //Dentro do intervalo de temperatura ideal retorna a mesma posição
                else{
                    Position newPos = new Position();
                    newPos = super.getNextPos(n);
                    return newPos;
                }
	}      
}
