package edu.swin.hets.helper;

import edu.swin.hets.helper.negotiator.BoulwareNegotiator;
import edu.swin.hets.helper.negotiator.HoldForFirstOfferPrice;
import edu.swin.hets.helper.negotiator.LinearUtilityDecentNegotiator;
import java.util.concurrent.ExecutionException;
import java.util.List;
/******************************************************************************
 *  Use: Create negotiations based on JSON arguments.
 *  PS: The code is bodgy as but Huan told me to do it so that's ok.
 *  Throws: Lots.
 *****************************************************************************/
public class NegotiatorFactory {
    public static Integer MIN_NUMBER_OF_ARGS = 2; //Is type + the minim number of args the simplest strat takes
    public static NegotiatorFactory Factory = new NegotiatorFactory();

    public INegotiationStrategy getNegotiationStrategy(
            List<String> arguments,
            IUtilityFunction utilityFun,
            String opponentName,
            PowerSaleProposal firstOffer,
            GlobalValues currentGlobals,
            String conversationID)
            throws ExecutionException{
        switch (arguments.get(0)) {
            case ("HoldForFirstOfferPrice"): return createHoldForFirstOfferPrice(firstOffer, conversationID,
                    opponentName, currentGlobals, arguments);
            case ("LinearUtilityDecentNegotiator"): return createLinearUtilityDecentNegotiator(firstOffer,
                    conversationID, opponentName, currentGlobals, utilityFun, arguments);
            case ("BoulwareNegotiator") : return createBoulwareNegotiator(firstOffer,
                    conversationID, opponentName, currentGlobals, utilityFun, arguments);
        }
        throw new ExecutionException(new Throwable("Did not find the Negotiation function type"));
    }

    /*
    *  Args order: maxNegotiationTime, maxStallTime, costTolerance, volumeTolerance, timeTolerance
     */
    private INegotiationStrategy createHoldForFirstOfferPrice(PowerSaleProposal firstOffer,
                                                              String conversationID,
                                                              String opponentName,
                                                              GlobalValues currentGlobals,
                                                              List<String> args) throws ExecutionException {
        int maxNegotiationTime = 20;
        int maxStallTime = 10;
        double costTolerance = 0.1;
        double volumeTolerance = 0.1;
        double timeTolerance = 0.1;
        if (args.size() == 6) {
            try {
                maxNegotiationTime = Integer.parseInt(args.get(1));
                maxStallTime = Integer.parseInt(args.get(2));
                costTolerance = Double.parseDouble(args.get(3));
                volumeTolerance = Double.parseDouble(args.get(4));
                timeTolerance = Double.parseDouble(args.get(5));
            } catch (Exception e) {
                throw new ExecutionException(new Throwable("Was passed invalid params to HoldForFirstOfferPrice"));
            }
        }// Leave as default
               return new HoldForFirstOfferPrice(firstOffer, conversationID, opponentName, currentGlobals,
                maxNegotiationTime, maxStallTime, costTolerance, volumeTolerance, timeTolerance);
    }
    /*
    *  Args order: maxNegotiationTime, maxStallTime, priceJump, volumeJump, timeJump, acceptTolerance.
     */
    private INegotiationStrategy createLinearUtilityDecentNegotiator (PowerSaleProposal firstOffer,
                                                                      String conversationID,
                                                                      String opponentsName,
                                                                      GlobalValues currentGlobals,
                                                                      IUtilityFunction utilityFunction,
                                                                      List<String> args) throws ExecutionException {
        int maxNegotiationTime = 20;
        int maxStallTime = 10;
        double priceJump = 0.1;
        double volumeJump = 0.1;
        int timeJump = 1;
        double acceptTolerance = 0.99;
        if (args.size() == 7) {
            try {
                maxNegotiationTime = Integer.parseInt(args.get(1));
                maxStallTime = Integer.parseInt(args.get(2));
                priceJump = Double.parseDouble(args.get(3));
                volumeJump = Double.parseDouble(args.get(4));
                timeJump = Integer.parseInt(args.get(5));
                acceptTolerance = Double.parseDouble(args.get(6));
            } catch (Exception e) {
                throw new ExecutionException(new Throwable("Was passed invalid params to linerDecentNegotiator"));
            }
        }// Leave as default
        return new LinearUtilityDecentNegotiator(firstOffer, conversationID, opponentsName, currentGlobals,
                maxNegotiationTime, maxStallTime, priceJump, volumeJump, timeJump, acceptTolerance, utilityFunction);

    }
    /*
    *  Args order: maxNegotiationTime, maxStallTime, priceJump, volumeJump, timeJump, acceptTolerance.
     */
    private INegotiationStrategy createBoulwareNegotiator (PowerSaleProposal firstOffer,
                                                           String conversationID,
                                                           String opponentsName,
                                                           GlobalValues currentGlobals,
                                                           IUtilityFunction utilityFunction,
                                                           List<String> args) throws ExecutionException {
        int maxNegotiationTime = 20;
        int maxStallTime = 10;
        double priceJump = 0.1;
        double volumeJump = 0.1;
        int timeJump = 1;
        double acceptTolerance = 0.99;
        if (args.size() == 7) {
            try {
                maxNegotiationTime = Integer.parseInt(args.get(1));
                maxStallTime = Integer.parseInt(args.get(2));
                priceJump = Double.parseDouble(args.get(3));
                volumeJump = Double.parseDouble(args.get(4));
                timeJump = Integer.parseInt(args.get(5));
                acceptTolerance = Double.parseDouble(args.get(6));
            } catch (Exception e) {
                throw new ExecutionException(new Throwable("Was passed invalid params to linerDecentNegotiator"));
            }
        }// Leave as default
        return new BoulwareNegotiator(firstOffer, conversationID, opponentsName, currentGlobals,
                maxNegotiationTime, maxStallTime, priceJump, volumeJump, timeJump, acceptTolerance, utilityFunction);


    }
}
