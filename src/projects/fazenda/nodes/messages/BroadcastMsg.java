package projects.fazenda.nodes.messages;

import sinalgo.nodes.messages.Message;

/**
 * Messagem enviada pela antena para os bois para que eles se conetem a ela 
 * se estiverem no setor
 */
public class BroadcastMsg extends Message {
    
    public boolean obrigaConectar = false;
    
	@Override
	public Message clone() {
		return this; 
	}

}
