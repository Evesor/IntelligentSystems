package edu.swin.hets.agent;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.swin.hets.helper.GoodMessageTemplates;
import edu.swin.hets.helper.IMessageHandler;
import edu.swin.hets.helper.PowerSaleAgreement;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.ToDoubleFunction;
/******************************************************************************
 *  Use: Creates statistics about the system
 *  Name: Always only have one of these on the main container, have it name set
 *        to "StatisticsAgent"
 *  Preformatives Understood:
 *       - INFORM : Used to tell the stats agent about a new agreement.
 *             content: "An PowerSaleAgreement object."
 *****************************************************************************/
public class StatisticsAgent extends BaseAgent {
    private ArrayList<ArrayList<PowerSaleAgreement>> _agreements;

    private MessageTemplate _saleInformMessageTemplate = MessageTemplate.and(
            GoodMessageTemplates.ContatinsString(PowerSaleAgreement.class.getName()),
            MessageTemplate.MatchPerformative(ACLMessage.INFORM));

    public StatisticsAgent () {
        super.setup();
        _agreements = new ArrayList<>();
        _agreements.add(new ArrayList<>()); // Add list for first time-slice.
        addMessageHandler(_saleInformMessageTemplate, new SaleInformMessageHandler());
    }

    @Override
    protected void TimeExpired() {
        _agreements.add(new ArrayList<>());
    }

    @Override
    protected void TimePush(int ms_left) {

    }

    @Override
    protected String getJSON() {
        String json = "test";
        try {
            json = new ObjectMapper().writeValueAsString(
                    new StatisticsAgentData());
        }
        catch (JsonProcessingException e) {
            LogError("Error parsing data to json in " + getName() + " exeption thrown");
        }
        return json;
    }

    private class SaleInformMessageHandler implements IMessageHandler{
        public void Handler(ACLMessage msg) {
            PowerSaleAgreement agreement = getPowerSaleAgrement(msg);
            if (agreement != null) {
                ArrayList<PowerSaleAgreement> currentList = _agreements.get(_agreements.size() - 1);
                if (! currentList.stream().anyMatch((agg) -> agg.equalValues(agreement))) {
                    // No matches, add the agreement.
                    currentList.add(agreement);
                }
            }
        }
    }
    /******************************************************************************
     *  Use: Used by getJson to output data to server.
     *****************************************************************************/
    private class StatisticsAgentData implements Serializable {
        public List<Double> getAverage_Price () {
            return average((agg) -> agg.getCost());
        }

        public List<Double> getAverage_Volume () {
            return average((agg) -> agg.getAmount());
        }

        public List<Double> getAverage_Time () {
            return average((agg) -> agg.getEndTime() - agg.getStartTime());
        }

        // Function used to make average of values based on lambda of what value we want.
        private ArrayList<Double> average (ToDoubleFunction<PowerSaleAgreement> lambda) {
            ArrayList<Double> averageList = new ArrayList<>();
            for (ArrayList<PowerSaleAgreement> agreements : _agreements) {
                double runningTotal = 0;
                int numberOfItems = 0;
                for (PowerSaleAgreement agg : agreements) {
                    runningTotal += lambda.applyAsDouble(agg);
                    numberOfItems ++;
                }
                averageList.add(runningTotal / numberOfItems);
            }
            return averageList;
        }
    }
}
