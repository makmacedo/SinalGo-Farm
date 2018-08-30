package projects.fazenda.nodes.messages;

import sinalgo.nodes.messages.Message;

/**
 * Sent by a mobile node when it decides to switch to another antenna
 */
public class DisconnectMsg extends Message {

	@Override
	public Message clone() {
		return this; // read-only policy
	}

}
