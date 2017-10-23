package edu.swin.hets.helper;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.swin.hets.helper.negotiator.HoldForFirstOfferPrice;
import edu.swin.hets.helper.negotiator.LinearUtilityDecentNegotiator;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.List;
/******************************************************************************
 *  Use: Create negotiations based on JSON arguments.
 *  PS: The code is bodgy as but Huan told me to do it so that's ok.
 *  Throws: Lots.
 *****************************************************************************/
public class NegotiatorFactory {
    public static NegotiatorFactory Factory = new NegotiatorFactory();

    public INegotiationStrategy getNegotiationStrategy(
            List<String> input,
            IUtilityFunction utilityFun,
            String ourName,
            String opponentName,
            PowerSaleProposal firstOffer,
            Integer timeSlice,
            String conversationID)
            throws ExecutionException{
        switch (input.get(0)) {
            case ("HoldForFirstOfferPrice"): return new HoldForFirstOfferPrice(firstOffer, conversationID,
                    opponentName, timeSlice);
            case ("LinearUtilityDecentNegotiator"): return createLinearUtilityDecentNegotiator(utilityFun,
                    ourName, opponentName, firstOffer, timeSlice,conversationID, input);
        }
        throw new ExecutionException(new Throwable("Did not find the Negotiation function."));
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
                Integer.parseInt(params.get(3)), conversationID);

    }
}
