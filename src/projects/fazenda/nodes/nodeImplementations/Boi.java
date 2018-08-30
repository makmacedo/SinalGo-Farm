 
package projects.fazenda.nodes.nodeImplementations;


import projects.fazenda.nodes.messages.DisconnectMsg;
import projects.fazenda.nodes.messages.BroadcastMsg;
import projects.fazenda.nodes.messages.ConnectMsg;
import sinalgo.configuration.Configuration;
import sinalgo.configuration.CorruptConfigurationEntryException;
import sinalgo.configuration.WrongConfigurationException;
import sinalgo.nodes.Node;
import sinalgo.nodes.messages.Inbox;
import sinalgo.nodes.messages.Message;
import sinalgo.tools.Tools;
import sinalgo.tools.logging.Logging;

public class Boi extends Node {
        
	float tempAmbiente = 0;
	Logging log = Logging.getLogger();//("smsLog.txt");
	
	Antena antenaAtual = null; // the antenna ths node is connected to, null if this node is not connected to an antenna
	private int seqIDCounter = 0;

	public Antena getAntenaAtual() {
		return antenaAtual;
	}
	
	public int getNextSeqID() {
		return ++seqIDCounter;
	}
        
        public float getTemp() {
                return tempAmbiente;
        }
	
	@Override
	public void checkRequirements() throws WrongConfigurationException {
	}

	@Override
	public void handleMessages(Inbox inbox) {
		boolean Conectar = false;
		Antena antenaAntiga = antenaAtual;
		
		while(inbox.hasNext()) {
			Message msg = inbox.next();

			if(msg instanceof BroadcastMsg) {
				BroadcastMsg broadcast = (BroadcastMsg) msg;
				// Conecta no Sync antena novo se este estiver mais proximo
				if(antenaAtual != null) {
					double distAntenaAntiga = antenaAtual.getPosition().squareDistanceTo(this.getPosition());
					double distNovaAntena = inbox.getSender().getPosition().squareDistanceTo(this.getPosition());
					if(distAntenaAntiga > distNovaAntena) { 
						this.antenaAtual = (Antena) inbox.getSender();
						Conectar = true;
					}
                                        else {
                                            if(broadcast.obrigaConectar) {
						Conectar = true; // subscirbe again
                                            }
                                        }
				} else {
					this.antenaAtual = (Antena) inbox.getSender();
					Conectar = true;
				}
			}
		}

		if(antenaAntiga != null && !antenaAtual.equals(antenaAntiga)) { // we switch to a different antenna
			// detach from current antenna
			DisconnectMsg disconnect = new DisconnectMsg();
			this.send(disconnect, antenaAntiga);
		}

		// subscribe to the closest Antenna
		if(Conectar) {
			ConnectMsg connect = new ConnectMsg();
			this.send(connect, antenaAtual);
                        tempAmbiente = antenaAtual.getTemp();  // atualiza para o boi a temperatura do setor ao qual ele esta conectado
                        super.setTemperatura(tempAmbiente);      // por meio desta temperatura o boi vai se mover ou nao de acordo com o modelo
                }                                               // de mobilidade e as diretrizes setadas no arquivo de config.xml  
                                                                 
	}                                                        

	public Boi() {
		try {
			this.defaultDrawingSizeInPixels = Configuration.getIntegerParameter("MobileNode/Size");
		} catch (CorruptConfigurationEntryException e) {
			Tools.fatalError(e.getMessage());
		}
	}
	
	public String toString() {
		if(antenaAtual != null) {
			return "Boi no Setor " + antenaAtual.ID;
		} else {
			return "Boi fora do alcançe dos Sync";
		}
	}
	
	@Override
	public void init() {
	}

	@Override
	public void neighborhoodChange() {
	}

	@Override
	public void preStep() {
	}

	@Override
	public void postStep() {
	}
        
}
