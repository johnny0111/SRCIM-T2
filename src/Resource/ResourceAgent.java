package Resource;

import jade.core.Agent;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import Libraries.IResource;
import Utilities.Constants;
import Utilities.DFInteraction;
import jade.core.behaviours.SequentialBehaviour;

import jade.domain.FIPAAgentManagement.FailureException;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.ContractNetResponder;

import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAException;
import jade.domain.DFService;
import jade.proto.AchieveREResponder;
import java.util.Set;
        /**
 *
 * @author Ricardo Silva Peres <ricardo.peres@uninova.pt>
 */
public class ResourceAgent extends Agent {

    String id;
    IResource myLib;
    String description;
    String[] associatedSkills;
    String location;
    boolean isAvailable;

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
            myLib = (IResource) instance;
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
            Logger.getLogger(ResourceAgent.class.getName()).log(Level.SEVERE, null, ex);
        }

        this.location = (String) args[3];
        this.isAvailable = true;

        myLib.init(this);
        this.associatedSkills = myLib.getSkills();
        System.out.println("Resource Deployed: " + this.id + " Executes: " + Arrays.toString(associatedSkills));

        //TO DO: Register in DF with the corresponding skills as services
        //A inscrição no DF deve ser o primeiro passo a ser executado por um agente, para que este possa ser
        //procurado pelos outros agentes presentes na plataforma. 
        

        
        
        try {
            DFInteraction.RegisterInDF(this,this.associatedSkills,Constants.DFSERVICE_RESOURCE); //DFInteraction.RegisterInDF(this, associatedSkills, id);
            //if(Constants.DEBUG)System.out.println("Registered in DF " + this.getLocalName() + "SKILLS " + Arrays.toString(this.associatedSkills));
        } catch (FIPAException ex) {
            Logger.getLogger(ResourceAgent.class.getName()).log(Level.SEVERE, null, ex);
        }

        // TO DO: Add responder behaviour/s
        //this.addBehaviour(new CNresponder(this, MessageTemplate.MatchPerformative(ACLMessage.CFP)));
        
        
        //@Amaral
        //To be Tested
        
        //this.addBehaviour(new FIPAresponder(this, MessageTemplate.MatchPerformative(ACLMessage.REQUEST)));
        
        SequentialBehaviour sb = new SequentialBehaviour();
        
        
        sb.addSubBehaviour(new CNresponder(this, MessageTemplate.MatchPerformative(ACLMessage.CFP)));
        sb.addSubBehaviour(new FIPAresponder(this, MessageTemplate.MatchPerformative(ACLMessage.REQUEST)));
        this.addBehaviour(sb);
            
        
        
        
    }
    
    @Override
    protected void takeDown() {
        super.takeDown(); 
    }
    
   //CONTRACTNET RESPONDER
    private class CNresponder extends ContractNetResponder{
            
        public CNresponder(Agent a, MessageTemplate mt){
            super(a, mt);
        }            
    
        @Override
        protected ACLMessage handleCfp(ACLMessage cfp) throws RefuseException, FailureException,  NotUnderstoodException{
            
            System.out.println(myAgent.getLocalName() + ": Processing CFP message");
            ACLMessage msg = cfp.createReply();
            
            if(isAvailable){
                msg.setPerformative(ACLMessage.PROPOSE);
                String proposal = Integer.toString((int) Math.random()); //Random because we need them to be different, so we can chose, and tell them appart;
                msg.setContent(proposal);
                System.out.println(this.getAgent().getName() + ": sent CFP PROPOSAL to " + cfp.getSender().getName());
            }
            else{
                msg.setPerformative(ACLMessage.REFUSE);
                System.out.println(this.getAgent().getName() + ": sent CFP REFUSE to " + cfp.getSender().getName());
            }

            return msg;

        }

        
        @Override
        protected ACLMessage handleAcceptProposal(ACLMessage cfp, ACLMessage propose, ACLMessage accept) throws FailureException {
            System.out.println(myAgent.getLocalName() + ": CFP PROPOSAL accepted by: " + cfp.getSender().getName()+ " executing: " + id);
            block(2000);
            ACLMessage msg = cfp.createReply();
            msg.setPerformative(ACLMessage.INFORM);
//            msg.getSender();
//            
//            
            msg.addReceiver(cfp.getSender());
            //@HJ
            //faz sentido este setContent?
            
            //@Amaral - Acho que mais vale fazer logo execute skill aqui
            msg.setContent(location);
            isAvailable = false;        //depois de executar o skill voltamos a meter a true

            
            return msg;
        }

       
    }
    
    //@Amaral
    
    private class FIPAresponder extends AchieveREResponder{
        public FIPAresponder(Agent a, MessageTemplate mt){
            
            super(a,mt);
        }
        
        @Override
        protected ACLMessage handleRequest (ACLMessage request) throws NotUnderstoodException, RefuseException{
            System.out.println(myAgent.getLocalName() + ": Preparing result of REQUEST");
            
            block(5000);
            ACLMessage msg = request.createReply();
            msg.setPerformative(ACLMessage.AGREE);
            
            return msg;
            
        }
        
        @Override
        protected ACLMessage prepareResultNotification(ACLMessage request, ACLMessage responde) throws FailureException{
            System.out.println(myAgent.getLocalName() + ": Preparing result of REQUEST");
            block(5000);
            ACLMessage msg = request.createReply();
            msg.setPerformative(ACLMessage.INFORM);
            return msg;
        }
    }
    
}
