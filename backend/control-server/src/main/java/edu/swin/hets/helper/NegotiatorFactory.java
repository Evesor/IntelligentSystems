package edu.swin.hets.helper;

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
            String ourName,
            String opponentName,
            PowerSaleProposal firstOffer,
            GlobalValues currentGlobals,
            String conversationID)
            throws ExecutionException{
        switch (arguments.get(0)) {
            case ("HoldForFirstOfferPrice"): return createHoldForFirstOfferPrice(firstOffer, conversationID,
                    opponentName, currentGlobals.getTime(), arguments);
            case ("LinearUtilityDecentNegotiator"): return createLinearUtilityDecentNegotiator(utilityFun,
                    ourName, opponentName, firstOffer, currentGlobals.getTime() ,conversationID, arguments);
        }
        throw new ExecutionException(new Throwable("Did not find the Negotiation function."));
    }

    private INegotiationStrategy createHoldForFirstOfferPrice(PowerSaleProposal firstOffer, String conversationID,
                                                              String opponentName, Integer timeSlice, List<String> args) {

        Integer maxTimes = 20;
        try {
            maxTimes = Integer.parseInt(args.get(1));
        } catch (Exception e) {  } // Leave as default
        //TODO, change from defaults.
        return new HoldForFirstOfferPrice(firstOffer, conversationID,
                opponentName, timeSlice, maxTimes, 0.5, 0.5, 0.5);
    }

    private INegotiationStrategy createLinearUtilityDecentNegotiator (IUtilityFunction utilityFun,
                                                                      String ourName,
                                                                      String opponentName,
                                                                      PowerSaleProposal firstOffer,
                                                                      Integer timeSlice,
                                                                      String conversationID,
                                                                      List<String> params) throws ExecutionException {
        if (params.size() != 4) {
            throw new ExecutionException(new Throwable("Wrong number of inputs"));
        }
        return new LinearUtilityDecentNegotiator(utilityFun, ourName,
                opponentName, firstOffer , timeSlice, Double.parseDouble(params.get(1)),
                Double.parseDouble(params.get(2)),
                Integer.parseInt(params.get(3)),
                0.99,
                conversationID);

    }
}
