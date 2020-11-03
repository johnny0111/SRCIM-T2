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

//@HJ
import Utilities.DFInteraction;
import jade.core.AID;
import jade.core.behaviours.Behaviour;
import java.util.Enumeration;

/**
 *
 * @author Ricardo Silva Peres <ricardo.peres@uninova.pt>
 */

// -gui GUI:Utilities.ConsoleAgent();

public class ProductAgent extends Agent {    
    
    String id;
    ArrayList<String> executionPlan = new ArrayList<>();
    // TO DO: Add remaining attributes required for your implementation
    
    // @HJ
    String current_location, next_location;
    int execution_step;
    
    @Override
    protected void setup() {
        Object[] args = this.getArguments();
        this.id = (String) args[0];
        this.executionPlan = this.getExecutionList((String) args[1]);
        System.out.println("Product launched: " + this.id + " Requires: " + executionPlan);
        
        // TO DO: Add necessary behaviour/s for the product to control the flow
        // of its own production 
        
        //************************************ @Henrique Joaquim
        // é preciso registar no DF? Acho que como é o PA (não é procurado por outros) não é preciso
        this.execution_step = 0;
        
        SequentialBehaviour sb = new SequentialBehaviour();
        for(int i=0; i < executionPlan.size(); i++){
            //next skill -> search in DF
            sb.addSubBehaviour(new search_resource_InDF(this));
            
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

    //@HJ
    //Referência: http://www.iro.umontreal.ca/~dift6802/jade/src/examples/protocols/ContractNetInitiatorAgent.java
    private class CNinitiator extends ContractNetInitiator{
            
            public CNinitiator(Agent a, ACLMessage msg){
                super(a, msg);
            }
            
            @Override
            protected void handleInform(ACLMessage inform){
                System.out.println(myAgent.getLocalName() + ": INFORM message received");
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
                        System.out.println("(CFP) REFUSE received from: " + msg.getSender().getName());
                    }
                }
                
                //Acept best proposal
                if(accept != null){
                    System.out.println("Accepting proposal " + bestProposal + " from responder " + bestProposer.getName());
                    accept.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
                }
                /*
                else{
                
                // *******
                
                    Aqui temos que arranjar maneira de agir em caso de não haver nenhuma proposta aceite.
                    Voltamos a fazer uma CFP mas agora para os agentes que já tinham respondido (array responses)
                
                //********
                
                }
                */
  
                
            }
            
        }
    
    //***************************** @Henrique Joaquim
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
            //available_agents = DFInteraction.SearchInDFByType(executionPlan.get(execution_step),myAgent);
        
        } catch (FIPAException ex){
            Logger.getLogger(ProductAgent.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        if(available_agents != null){
            //perform cfp
            ACLMessage cfp= new ACLMessage(ACLMessage.CFP);
            
            for(int i=0; i < available_agents.length; i++){
                cfp.addReceiver(available_agents[i].getName());
                System.out.println("Sent msg nr=" + i + " to agent" + available_agents[i].getName() + "(CFP) ");
            }
            
            myAgent.addBehaviour(new CNinitiator(myAgent,cfp));
        }
        
        else{
            System.out.println("Couldn't find resource: " + executionPlan.get(execution_step));
        }
        
    }
}
    //*****************************
    
}
