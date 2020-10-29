package Product;

import jade.core.Agent;
import java.util.ArrayList;

import jade.core.AID;
import jade.lang.acl.ACLMessage;
import jade.proto.ContractNetInitiator;
import java.util.Vector;


/**
 *
 * @author Ricardo Silva Peres <ricardo.peres@uninova.pt>
 */
public class ProductAgent extends Agent {    
    
    String id;
    ArrayList<String> executionPlan = new ArrayList<>();
    // TO DO: Add remaining attributes required for your implementation
    
    @Override
    protected void setup() {
        Object[] args = this.getArguments();
        this.id = (String) args[0];
        this.executionPlan = this.getExecutionList((String) args[1]);
        System.out.println("Product launched: " + this.id + " Requires: " + executionPlan);
        
        
        
        // TO DO: Add necessary behaviour/s for the product to control the flow
        // of its own production 
        
//        //@Amaral
//        ACLMessage msg = new ACLMessage(ACLMessage.CFP); //CALL FOR PROPOSALS //TODO acho que é esta que se tem que mudar
//        msg.addReceiver(new AID("responder", false));
//        this.addBehaviour(new initiator(this,msg));
//        //System.out.println(msg.toString());
        
        
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
    
    
    //@Amaral aka Jade tutorial
        
    //Meaning to do a first CFP
//    private class initiator extends ContractNetInitiator{
//        public initiator(Agent a, ACLMessage msg){
//            super (a,msg);
//        }
//
//        @Override
//        protected void handleInform(ACLMessage inform){
//            System.out.println(myAgent.getLocalName() + ": Inform message received");
//        }
//
//        //@Override
//        protected void hadleAllResponses (Vector responses, Vector acceptances){
//            System.out.println(myAgent.getLocalName() + ":All PROPOSALS received");
//            ACLMessage auxMsg = (ACLMessage)responses.get(0);
//            ACLMessage reply = auxMsg.createReply();
//            reply.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
//            acceptances.add(reply);
//        }
//    }
        
}
