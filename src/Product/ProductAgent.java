package Product;

import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.SequentialBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.proto.ContractNetInitiator;
import java.util.ArrayList;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import jade.proto.AchieveREInitiator;

//@HJ
import Utilities.DFInteraction;
import jade.core.AID;
import jade.core.behaviours.Behaviour;
import java.util.Enumeration;

import Utilities.Constants;
import jade.core.behaviours.SimpleBehaviour;

import Libraries.IResource;

/**
 * Prof: Ricardo Peres
 * @authors: Henrique Joaquim, Pedro Amaral & João Carvalho
 */

// -gui GlueStation1:Resource.ResourceAgent("GS1","GS1","TestLibrary","GlueStation1");GlueStation2:Resource.ResourceAgent("GS2","GS2","TestLibrary","GlueStation2");Operator:Resource.ResourceAgent("OP", "OP", "TestLibrary", "Source");GUI:Utilities.ConsoleAgent();
// -gui GUI:Utilities.ConsoleAgent();


public class ProductAgent extends Agent {

    String id;
    ArrayList<String> executionPlan = new ArrayList<>();
    // TO DO: Add remaining attributes required for your implementation
    
    String current_location, next_location;
    int execution_step;
    AID bestResource, agv,ta;
    boolean request_agv, ra_negotiation_done, skill_done, transport_done;
    
    
    public boolean search_resource_InDF_Done = false;

    @Override
    protected void setup() {
        Object[] args = this.getArguments();
        this.id = (String) args[0];
        this.executionPlan = this.getExecutionList((String) args[1]);
        System.out.println("Product launched: " + this.id + " Requires: " + executionPlan);
        
        
        // TO DO: Add necessary behaviour/s for the product to control the flow
        // of its own production 
        
        this.execution_step = 0;
        this.current_location = "Source";
        
        this.request_agv = false;
        this.ra_negotiation_done = false;   //hj: this bool is used so we can "block" the TA call until the negotiation with RA is done
        this.transport_done = false;        //hj: this bool is used so we can "block" the execution of a Skill b4 finishing transportation
        this.skill_done = false;            //hj: this bool is used so we can "block" the next iteration of the sequential behaviour
        
        //Sequência de ações prevista: Procurar recursos -> Pedir Transporte -> Pedir Execução -> (done) -> repetir até acabar a lista
        SequentialBehaviour sb = new SequentialBehaviour();
        for(int i=0; i < executionPlan.size(); i++){
            //next skill -> search in DF
            sb.addSubBehaviour(new search_resource_InDF(this));
            sb.addSubBehaviour(new transport(this));
            sb.addSubBehaviour(new request_skill(this));
            sb.addSubBehaviour(new finish_execution_step(this));
            this.addBehaviour(sb);
        }

    }

    @Override
    protected void takeDown() {
        super.takeDown(); //To change body of generated methods, choose Tools | Templates.
    }

     private ArrayList<String> getExecutionList(String productType){
        switch(productType){
            case "A": return Utilities.Constants.PROD_A;
            case "B": return Utilities.Constants.PROD_B;
            case "C": return Utilities.Constants.PROD_C;
        }
        return null;
    }
     


 //***********************************************************************************************************************
//***********************************************************************************************************************
//CN initiator    
    //Referência: http://www.iro.umontreal.ca/~dift6802/jade/src/examples/protocols/ContractNetInitiatorAgent.java
    private class CNinitiator extends ContractNetInitiator{

        private final ACLMessage msg;
            
            public CNinitiator(Agent a, ACLMessage msg){
                super(a, msg);
                this.msg = msg;
            }
            
            @Override
            protected void handleInform(ACLMessage inform){
                System.out.println(myAgent.getLocalName() + ": INFORM message received.");
                
                next_location = inform.getContent();
                
                if(current_location.equals(next_location)) //situação em que já estamos na localização pretendida (p.ex: duas aplicações de glue seguidas em que não precisamos de trocar de estação)
                    request_agv = false;
                else
                    request_agv = true;
                
                ra_negotiation_done = true;
            }
            
            @Override
            protected void handleAllResponses(Vector responses, Vector acceptances) {
                System.out.println(myAgent.getLocalName() + ": All PROPOSALS received");
                
                //Evaluate proposals
                int bestProposal = -1;
                AID bestProposer = null;
                ACLMessage accept = null;
                Enumeration e = responses.elements();
                
                while(e.hasMoreElements()){
                    
                    ACLMessage msg = (ACLMessage)e.nextElement();
                    
                    if(msg.getPerformative() == ACLMessage.PROPOSE){
                        ACLMessage reply = msg.createReply();
                        reply.setPerformative(ACLMessage.REJECT_PROPOSAL);
                        acceptances.addElement(reply);
                        
                        int proposal = Integer.parseInt(msg.getContent()); // to define in RA (can be a random)
                        if(proposal > bestProposal){
                            bestProposal = proposal;
                            bestProposer = msg.getSender();
                            accept = reply;
                        }
                    }
                    else{
                        System.out.println("(CFP) REFUSE received from: " + msg.getSender().getLocalName());
                    }
                }
                
                //Acept best proposal
                if(accept != null){
                    System.out.println("Accepting proposal " + bestProposal + " from responder " + bestProposer.getLocalName());
                    accept.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
                    
                    bestResource = bestProposer; //so we can tell agv where it should go
                }
                
                else{
                
                    try {
                        /* //*******
                        
                        Aqui temos que arranjar maneira de agir em caso de não haver nenhuma proposta aceite.
                        Voltamos a fazer uma CFP mas agora para os agentes que já tinham respondido (array responses)
                        Uma vez que não há necessidade de voltar a procurar que agentes fazem determinado skill.
                        
                        */ //*******
                        
                        //perform cfp
                        //ACLMessage cfp= new ACLMessage(ACLMessage.CFP);
//                    int i=0; //debug purposes, so we can see how if the number of messages is ok
//                    while(e.hasMoreElements()){
//                        ACLMessage msg = (ACLMessage)e.nextElement();
//                        cfp.addReceiver(msg.getSender());
//                        System.out.println("Sent msg nr=" + i + " to agent" + msg.getSender().getLocalName() + "(CFP) ");
//                        i++;
//                    }

                    Thread.sleep(2000);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(ProductAgent.class.getName()).log(Level.SEVERE, null, ex);
                    }
               
                    myAgent.addBehaviour(new CNinitiator(myAgent,this.msg));
                
                }
                
  
                
            }
            
        }
      
    
//CN initiator 
//***********************************************************************************************************************
//***********************************************************************************************************************
    

 //***********************************************************************************************************************
//***********************************************************************************************************************
//Search RA in DF
    
    private class search_resource_InDF extends OneShotBehaviour {
        
    public search_resource_InDF(Agent a){
        super(a);
    }

    @Override
    public void action() {
        
        DFAgentDescription[] available_agents = null;
        
        try {
            
            System.out.println("Looking for resource: " + executionPlan.get(execution_step));
            available_agents = DFInteraction.SearchInDFByName(executionPlan.get(execution_step),myAgent);
        
        } catch (FIPAException ex){
            Logger.getLogger(ProductAgent.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        if(available_agents != null){
            //System.out.println("DEBUG1 + AVAILABLE AGENT:" + available_agents.length);
            
            //perform cfp
            ACLMessage cfp= new ACLMessage(ACLMessage.CFP);
            
            for(int i=0; i < available_agents.length; i++){
                cfp.addReceiver(available_agents[i].getName());
                System.out.println("Sent msg nr: " + (i+1) + " to agent" + available_agents[i].getName().getLocalName()+ "(CFP) ");
            }
            
            myAgent.addBehaviour(new CNinitiator(myAgent,cfp));
        }
        
        else{
            System.out.println("Couldn't find resource: " + executionPlan.get(execution_step));
        }
        
    }
}
//Search RA in DF
//***********************************************************************************************************************
//***********************************************************************************************************************

//***********************************************************************************************************************
//***********************************************************************************************************************
//FIPA Request Initiator TA
    
    private class REInitiator_ta extends AchieveREInitiator {

        public REInitiator_ta(Agent a, ACLMessage msg) {
            super(a, msg);
            System.out.println("Initiated FIPA REQUEST as :" + myAgent.getLocalName()  );

        }

        @Override
        protected void handleAgree(ACLMessage agree) {
            System.out.println(myAgent.getLocalName() + ": AGREE message received");
        }

        @Override
        protected void handleInform(ACLMessage inform) {
            System.out.println(myAgent.getLocalName() + ": INFORM message received");
            
            current_location = next_location; 
            transport_done = true;
        }
        
        @Override
        protected void handleRefuse(ACLMessage refuse) {
            //falta adicionar coisas para quando o transporte é recusado
            //nomeadamente se já estiver ocupado noutro transporte  
        }
    }

//FIPA Request Initiator TA
//***********************************************************************************************************************
//***********************************************************************************************************************      

//***********************************************************************************************************************
//***********************************************************************************************************************
//Search TA in DF

    private class search_ta_InDF extends OneShotBehaviour{

        public search_ta_InDF(Agent a) {
            super(a);
        }


        @Override
        public void action() {
            
            DFAgentDescription[] available_agents = null;
        
            try {

                System.out.println("Looking for Transportation...");
                available_agents = DFInteraction.SearchInDFByName("sk_move", myAgent);

            } catch (FIPAException ex){
                Logger.getLogger(ProductAgent.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            if(available_agents != null){
                
                System.out.println(myAgent.getLocalName() + " found TA: " + available_agents.length);

                ACLMessage request2 = new ACLMessage(ACLMessage.REQUEST);
                
                request2.setContent(current_location + Constants.TOKEN + next_location);
                request2.setOntology(Constants.ONTOLOGY_MOVE);
                
                
                ta = available_agents[0].getName();
                request2.addReceiver(ta);
                
                    request2.getAllReceiver().next().toString();
                    
                

                System.out.println(myAgent.getLocalName() + ": requested " + available_agents[0].getName().getLocalName() /*+ /*"deixa imprimir: " +request.getAllReceiver().*/);

                myAgent.addBehaviour(new REInitiator_ta(myAgent, request2));
            }        
            else
                System.out.println(myAgent.getLocalName() + "Could not find TA");
        }  
    }
    
 
//Search TA in DF
//***********************************************************************************************************************
//***********************************************************************************************************************    
    
 //***********************************************************************************************************************
//***********************************************************************************************************************
//Transport
    
    private class transport extends SimpleBehaviour
    {
        private boolean finished;

        private transport(Agent a) {
            super(a);
            finished = false;
        }

        @Override
        public void action() {
            if(ra_negotiation_done){
                if(request_agv){
                    myAgent.addBehaviour(new search_ta_InDF(myAgent));
                    request_agv = false;
                }
                else{
                   transport_done = true; 
                }
                
                ra_negotiation_done = false; //clearing the variable
                this.finished = true;
            }
        }

        @Override
        //igualzinho ao tutorial
        public boolean done() {
            //if(this.finished){
                //System.out.println("Transport done or not necessary.");
                return this.finished;
            //}
        }
        
    }
    
//Transport
//***********************************************************************************************************************
//***********************************************************************************************************************
   
    
 //***********************************************************************************************************************
//***********************************************************************************************************************
//FIPA Request Initiator for RA
    
    private class REinitiator_ra extends AchieveREInitiator {

        public REinitiator_ra(Agent a, ACLMessage request) {
            super(a,request);
        }
       

        @Override
        protected void handleAgree(ACLMessage agree) {
            System.out.println(myAgent.getLocalName() + ": AGREE message received from: " + agree.getSender().getLocalName());
        }

        @Override
        protected void handleInform(ACLMessage inform) {
            System.out.println(myAgent.getLocalName() + ": INFORM message received from: " + inform.getSender().getLocalName());
            skill_done = true;
        }
    }
    
//FIPA Request Initiator for RA
//***********************************************************************************************************************
//***********************************************************************************************************************
    
    
    
//***********************************************************************************************************************
//***********************************************************************************************************************
//Execute Skill
      private class request_skill extends SimpleBehaviour {

        private boolean finished;

        public request_skill(Agent a) {
            super(a);
            this.finished = false;
        }

        @Override
        public void action() {
            if (transport_done) {
                
                ACLMessage request = new ACLMessage(ACLMessage.REQUEST);
                
                request.setContent(executionPlan.get(execution_step));      //skill pretendido
                request.addReceiver(bestResource);                          //definido aquando da cfp
                
                
                myAgent.addBehaviour(new REinitiator_ra(myAgent, request));
                
                transport_done = false;
                this.finished = true;
            }
        }

        @Override
        public boolean done() {
            return this.finished;
        }
    }
//Execute Skill
//***********************************************************************************************************************
//***********************************************************************************************************************
     


//***********************************************************************************************************************
//***********************************************************************************************************************
// Wait for the skill execution to be done
      
    private class finish_execution_step extends SimpleBehaviour {

        private boolean finished = false;

        public finish_execution_step(Agent a) {
            super(a);
        }

        @Override
        public void action() {
            if (skill_done) {
                System.out.println(myAgent.getLocalName() + " finished execution step: " + executionPlan.get(execution_step) + "\n");
                if(executionPlan.get(execution_step).equals("sk_drop")){
                    System.out.println("The manufacture of " + myAgent.getLocalName()  +" has been completed with SUCCESS!");
                }
                skill_done = false;
                execution_step++;
                this.finished = true;
                
            }
        }

        @Override
        public boolean done() {
            return this.finished;
        }
    }

// Wait for the skill execution to be done
//***********************************************************************************************************************
//***********************************************************************************************************************

}
