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
import Libraries.ITransport;
import java.util.StringTokenizer;

import Product.ProductAgent;
/**
 *
 * @prof: Ricardo Silva Peres <ricardo.peres@uninova.pt>
 * @authors: Henrique Joaquim, Joao Carvalho & Pedro Amaral
 */
public class TransportAgent extends Agent {

    String id;
    ITransport myLib;
    String description;
    String[] associatedSkills;
    
    //@HJ
    String initial_position, dest_position;
    boolean isAvailable;
    

    @Override
    protected void setup() {
        Object[] args = this.getArguments();
        this.id = (String) args[0];
        this.description = (String) args[1];
        this.isAvailable=true;
        

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
        
        //Register in DF
        try {
            DFInteraction.RegisterInDF(this,this.associatedSkills,Constants.DFSERVICE_RESOURCE); //DFInteraction.RegisterInDF(this, associatedSkills, id);
            if(Constants.DEBUG)System.out.println("Registered in DF " + this.getLocalName() + "SKILLS:" + Arrays.toString(this.associatedSkills));
        
        } catch (FIPAException ex) {
            Logger.getLogger(ResourceAgent.class.getName()).log(Level.SEVERE, null, ex);
        }
        // Responder behaviour/s
        this.addBehaviour(new TransportAgent.responder(this, MessageTemplate.MatchPerformative(ACLMessage.REQUEST)));
        
        
    }
    
    @Override
    protected void takeDown() {
        super.takeDown();
    }
    
    //**** FIPA REQUEST ACHIEVE RESPONDER
        public class responder extends AchieveREResponder {
        
        public responder(Agent a, MessageTemplate mt){
            super(a, mt);
            System.out.println(myAgent.getLocalName() + "(TA): Called to action Request from: ");
            
        }
        @Override
        protected ACLMessage handleRequest(ACLMessage request) throws NotUnderstoodException, RefuseException{
            System.out.println(myAgent.getLocalName() + "(TA): Received Transportation Request from: " + request.getSender().getName());
            ACLMessage msg = request.createReply();
            
            if(isAvailable){
                
                isAvailable=false;
                
                StringTokenizer st = new StringTokenizer(request.getContent(), Constants.TOKEN);
                initial_position = st.nextToken();
                dest_position = st.nextToken();
                
                msg.setPerformative(ACLMessage.AGREE);
                System.out.println(myAgent.getLocalName() + "(TA): sent AGREE to: " + request.getSender().getName());

            }else{
                msg.setPerformative(ACLMessage.REFUSE);
                System.out.println(myAgent.getLocalName() + "(TA): sent REFUSE to: " + request.getSender().getName());

            }
            
            return msg;
        }
        
        @Override
        protected ACLMessage prepareResultNotification(ACLMessage request, ACLMessage response) throws FailureException{
            System.out.println(myAgent.getLocalName() + ": Preparing results of transportation REQUEST");
            
            //@HJ
            //ver isto (no código do Pedro está assim mas a função que está
            //definida em ITranspor.java só tem 2 argumentos de entrada
            //myLib.executeMove(initial_position, dest_position, id);
            
            myLib.executeMove(initial_position, dest_position);
            
            ACLMessage msg = request.createReply();
            msg.setPerformative(ACLMessage.INFORM);
            
            System.out.println(myAgent.getLocalName() + " (TA): Performed MOVE operation to: " + request.getSender());
            isAvailable = true;
            
            return msg;
        }
    }
  
}
