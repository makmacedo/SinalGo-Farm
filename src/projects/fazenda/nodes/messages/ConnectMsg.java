package projects.fazenda.nodes.messages;

import sinalgo.nodes.messages.Message;

/**
 * Sent by a mobile node to the antenna to subscribe
 */
public class ConnectMsg extends Message {
	@Override
	public Message clone() {
		return this; // read-only policy
	}

}
