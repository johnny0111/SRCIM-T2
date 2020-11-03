package Resource;

import jade.core.Agent;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import Libraries.IResource;
import Utilities.Constants;
import Utilities.DFInteraction;

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

        myLib.init(this);
        this.associatedSkills = myLib.getSkills();
        System.out.println("Resource Deployed: " + this.id + " Executes: " + Arrays.toString(associatedSkills));

        //TO DO: Register in DF with the corresponding skills as services
        //A inscrição no DF deve ser o primeiro passo a ser executado por um agente, para que este possa ser
        //procurado pelos outros agentes presentes na plataforma. 
        
//        DFAgentDescription dfd = new DFAgentDescription();
//        dfd.setName(this.getAID());
//        ServiceDescription sd = new ServiceDescription(); // Pointless? It's just the description
//        sd.setType("dfservice_resource");
//        sd.setName( getLocalName() );
//        dfd.addServices(sd);
//        try{
//            DFService.register(this,  dfd);
//            System.out.println("Registered " + getLocalName() + " in DF\n"+ this.description);
//        }catch (FIPAException ex) {
//            Logger.getLogger(ResourceAgent.class.getName()).log(Level.SEVERE,null,ex); //In tutorial it was Tutorial Agent
//        }
        
        
        try {
           
            DFInteraction.RegisterInDF(this,this.associatedSkills,"dfservice_resource"); //DFInteraction.RegisterInDF(this, associatedSkills, id);
            if(Constants.DEBUG)System.out.println("Registered in DF " + this.getLocalName() + "SKILLS " + Arrays.toString(this.associatedSkills));
        } catch (FIPAException ex) {
            Logger.getLogger(ResourceAgent.class.getName()).log(Level.SEVERE, null, ex);
        }

        // TO DO: Add responder behaviour/s
        
        //@Amaral
        this.addBehaviour(new responder(this, MessageTemplate.MatchPerformative(ACLMessage.CFP)));
        
    }
    
    private class responder extends ContractNetResponder{
            
        public responder(Agent a, MessageTemplate mt){
            super(a, mt);
        }            
    
    
    
        @Override
        protected ACLMessage handleCfp(ACLMessage cfp) throws RefuseException, FailureException,  NotUnderstoodException{
            System.out.println(myAgent.getLocalName() + ": Processing CFP message");

            ACLMessage msg = cfp.createReply();
            msg.setPerformative(ACLMessage.PROPOSE);
            
            
            return msg;

        }

        @Override
        protected ACLMessage handleAcceptProposal(ACLMessage cfp, ACLMessage propose, ACLMessage accept) throws FailureException {
            System.out.println(myAgent.getLocalName() + ":Preparing result of CFP");
            block(2000);
            ACLMessage msg = cfp.createReply();
            msg.setPerformative(ACLMessage.INFORM);
            String debug1 = Integer.toString((int) Math.random()); //Random because we need them to be different, so we can chose, and tell them appart;
            System.out.println(debug1);
            msg.setContent( debug1);
            
            return msg;
        }

       
    }
     //*/ @Amaral no longer
    @Override
    protected void takeDown() {
        super.takeDown(); 
    }
}
