package projects.fazenda.nodes.nodeImplementations;

import java.awt.Color;
import java.awt.Graphics;
import java.util.Comparator;
import java.util.TreeSet;
import java.util.Vector;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.io.IOException;
import javax.swing.JOptionPane;

import projects.fazenda.nodes.messages.*;
import projects.fazenda.nodes.timers.AntennaNeighborhoodClearTimer;
import projects.fazenda.nodes.timers.BroadcastMsgTimer;

import sinalgo.configuration.Configuration;
import sinalgo.configuration.CorruptConfigurationEntryException;
import sinalgo.configuration.WrongConfigurationException;
import sinalgo.gui.transformation.PositionTransformation;
import sinalgo.nodes.Node;
import sinalgo.nodes.messages.Inbox;
import sinalgo.nodes.messages.Message;
import sinalgo.tools.Tools;

public class Antena extends Node {
    
    	private float temperatura;
        private boolean definido = false;
        private int tMin;
        private int tMax;
        private int nBois;       
        private int rounds;
        private boolean aumenta;
        private int hora; 
        private float amplitudeTermica;
        private int dia; // 24x o parametro hora = rounds passado pelo config.xml (1 dia = 12000 rounds)
        private int nSteps; //numero de rounds que leva cada step de aumento ou diminuiçao de temp
        private float iSteps; //intervalo a realizar inc ou dec de temperatura por step


        
        public float getTemp(){
            if (!definido){
                this.temperatura = atualizaTemp(temperatura);
                return temperatura;
            } else {
                return temperatura;
            }
        }
        
        public float atualizaTemp(float t){
            // realiza dois steps de aumento ou diminuição por hora 
            amplitudeTermica = (tMax - tMin);
            nSteps = dia/48; //dois step de temperatura por hora
            iSteps = amplitudeTermica/48;
  
            if (rounds%nSteps==0){
                if (aumenta){
                    t += iSteps;
                    if(t>=tMax){ aumenta = false; }
                    return t;
                } else {
                    t -= iSteps;
                    if(t<=tMin){ aumenta = true; }
                    return t;
                }
            } return t;
        }
        
	// a list of all antennas
	private static Vector<Antena> antennaList = new Vector<Antena>();
	
	@Override
	public void checkRequirements() throws WrongConfigurationException {
	}

	@Override
	public void handleMessages(Inbox inbox) {
            FileWriter log;
            BufferedWriter bf;
                   
                try {
                    log = new FileWriter("logFazenda.txt",true);
                     bf = new BufferedWriter(log);
                     
                    if (rounds % (dia/24) == 0){
                        if(this.ID==1){
                            System.out.println("------------------------------------------------------- ");
                            bf.append("------------------------------------------------------- \n");
                        }
                    //Exibindo no console as informações da mensagem de dados
                        if(hora == 24){ hora = 0; }

                        nBois = this.neighbors.size() + this.oldNeighborhood.size(); 

                        System.out.println("hora: "+hora+":00"+" Setor: "+this.ID+" Temperatura: "+getTemp()+"\tBois: "+nBois);
                        bf.append("hora: "+hora+":00"+" Setor: "+this.ID+" Temperatura: "+getTemp()+"\tBois: "+nBois+"\n");
                        bf.close();

                        hora++;

                    }
                } catch (IOException ex) {
                    Logger.getLogger(Antena.class.getName()).log(Level.SEVERE, null, ex);
                }
            
            
		while(inbox.hasNext()) {
			Message msg = inbox.next();
			if(msg instanceof ConnectMsg) {
				neighbors.add(inbox.getSender());
			}
			else if(msg instanceof DisconnectMsg) { 
				neighbors.remove(inbox.getSender());
			}
		}
                rounds++;
	}
	
	private boolean isNeighbor(Node aNode) {
		if(neighbors.contains(aNode)) {
			return true;
		}
		if(oldNeighborhood.contains(aNode)) {
			return true;
		} 
		return false;
	}
	
	TreeSet<Node> neighbors = new TreeSet<Node>(new NodeComparer());
	TreeSet<Node> oldNeighborhood = new TreeSet<Node>(new NodeComparer());
	
	public void resetNeighborhood() {
		// switch the two neighborhoods, s.t. the old neighborhood is the current neighborhood
		// and the new neighborhood becomes empty
		TreeSet<Node> temp = oldNeighborhood;
		oldNeighborhood = neighbors;
		neighbors = temp;
		neighbors.clear();
		// start a timer to clear the oldNeighborhood
		AntennaNeighborhoodClearTimer t = new AntennaNeighborhoodClearTimer(oldNeighborhood);
		t.startRelative(3, this); 
	}
	
	@Override
	public void init() {
		// start a msg timer to periodically send the invite msg 
		BroadcastMsgTimer timer = new BroadcastMsgTimer();
		timer.startRelative(1, this);
		antennaList.add(this);
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

	public String toString() {
		// show the list of subscribed nodes
		String list = "";
		for(Node n : neighbors) {
			list += " " + n.ID;
		}
		if(oldNeighborhood.size() > 0) {
			list += "\n(";
			for(Node n : oldNeighborhood) {
				list += " " + n.ID;
			}
			list += ")";
		}
		return Tools.wrapToLinesConsideringWS(list, 100);
	}

	private static int radius;
	{ try {
		radius = Configuration.getIntegerParameter("GeometricNodeCollection/rMax");
	} catch(CorruptConfigurationEntryException e) {
		Tools.fatalError(e.getMessage());
	}}
	
	public void draw(Graphics g, PositionTransformation pt, boolean highlight){
		Color bckup = g.getColor();
		g.setColor(Color.YELLOW);
		this.drawingSizeInPixels = (int) (defaultDrawingSizeInPixels * pt.getZoomFactor());
		super.drawNodeAsDiskWithText(g, pt, highlight, Integer.toString(this.ID), drawingSizeInPixels, Color.YELLOW);
		g.setColor(Color.LIGHT_GRAY);
		pt.translateToGUIPosition(this.getPosition());
		int r = (int) (radius * pt.getZoomFactor());
		g.drawOval(pt.guiX - r, pt.guiY - r, r*2, r*2); 
		g.setColor(bckup);
	}
        
        @NodePopupMethod(menuText = "definir temperatura")
	public void defineTemp() {
                float temp = -500;
                try{
                    temp = Float.parseFloat(JOptionPane.showInputDialog(null, "insira a temperatura fixa para o Setor"));
                }
                catch (NumberFormatException e) {
                    return;
                }
                finally{
                    if (temp!=-500){
                        this.temperatura = temp;
                        this.definido = true;
                    }
                }
	}
	

	public Antena() {
                try {
                    this.dia = 24*(Configuration.getIntegerParameter("hora"));
                } catch (CorruptConfigurationEntryException ex) {
                    Logger.getLogger(Antena.class.getName()).log(Level.SEVERE, null, ex);
                }
                try {
                    this.tMin = Configuration.getIntegerParameter("Clima/tempMin");
                    this.temperatura = Configuration.getIntegerParameter("Clima/tempMin");
                } catch (CorruptConfigurationEntryException ex) {
                    Logger.getLogger(Antena.class.getName()).log(Level.SEVERE, null, ex);
                }
                try {
                    this.tMax = Configuration.getIntegerParameter("Clima/tempMax");
                } catch (CorruptConfigurationEntryException ex) {
                    Logger.getLogger(Antena.class.getName()).log(Level.SEVERE, null, ex);
                }
		try {
                    this.defaultDrawingSizeInPixels = Configuration.getIntegerParameter("Antena/Size");
		} catch (CorruptConfigurationEntryException e) {
                    Tools.fatalError(e.getMessage());
		}
	}

	class NodeComparer implements Comparator<Node> {
		public int compare(Node n1, Node n2) {
			return n1.ID < n2.ID ? -1 : n1.ID == n2.ID ? 0 : 1;   
		}
	}

}
