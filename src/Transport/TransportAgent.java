package Transport;

import jade.core.Agent;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import Libraries.ITransport;
import Resource.ResourceAgent;
import Utilities.Constants;
import Utilities.DFInteraction;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.domain.FIPAAgentManagement.FailureException;
import jade.proto.AchieveREResponder;
/**
 *
 * @author Ricardo Silva Peres <ricardo.peres@uninova.pt>
 */
public class TransportAgent extends Agent {

    String id;
    ITransport myLib;
    String description;
    String[] associatedSkills;
    

    @Override
    protected void setup() {
        Object[] args = this.getArguments();
        this.id = (String) args[0];
        this.description = (String) args[1];

        //Load hw lib
        try {
            String className = "Libraries." + (String) args[2];
            Class cls = Class.forName(className);
            Object instance;
            instance = cls.newInstance();
            myLib = (ITransport) instance;
            System.out.println(instance);
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
            Logger.getLogger(TransportAgent.class.getName()).log(Level.SEVERE, null, ex);
        }

        myLib.init(this);
        this.associatedSkills = myLib.getSkills();
        System.out.println("Transport Deployed: " + this.id + " Executes: " + Arrays.toString(associatedSkills));
        
        try {
           
            DFInteraction.RegisterInDF(this,this.associatedSkills,"dfservice_resource"); //DFInteraction.RegisterInDF(this, associatedSkills, id);
            if(Constants.DEBUG)System.out.println("Registered in DF " + this.getLocalName() + "SKILLS " + Arrays.toString(this.associatedSkills));
        } catch (FIPAException ex) {
            Logger.getLogger(ResourceAgent.class.getName()).log(Level.SEVERE, null, ex);
        }
        this.addBehaviour(new TransportAgent.responder(this, MessageTemplate.MatchPerformative(ACLMessage.CFP)));
        // TO DO: Register in DF
        // TO DO: Add responder behaviour/s
    }
    
        public class responder extends AchieveREResponder {
        
        public responder(Agent a, MessageTemplate mt){
            super(a, mt);
        }
        @Override
        protected ACLMessage handleRequest(ACLMessage request) throws NotUnderstoodException, RefuseException{
            System.out.println(myAgent.getLocalName() + ": Processing REQUEST message");
            ACLMessage msg = request.createReply();
            msg.setPerformative(ACLMessage.AGREE);
            return msg;
        }
        @Override
        protected ACLMessage prepareResultNotification(ACLMessage request, ACLMessage response) throws FailureException{
            System.out.println(myAgent.getLocalName() + ": Preparing results of REQUEST");
            block(5000);
            ACLMessage msg = request.createReply();
            msg.setPerformative(ACLMessage.INFORM);
            return msg;
        }
    }
   

    @Override
    protected void takeDown() {
        super.takeDown();
    }
}
