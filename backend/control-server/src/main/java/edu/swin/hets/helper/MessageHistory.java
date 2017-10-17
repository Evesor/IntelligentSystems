package edu.swin.hets.helper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jade.lang.acl.ACLMessage;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.ArrayList;
/******************************************************************************
 *  Use: To create a JSON string listing the number of each communication that
 *       has happened between this agent and all the other agents.
 *****************************************************************************/
public class MessageHistory {
    //private HashMap<String, Integer> _agentsNames;
    private ConversationCountList _conversations;
    private String _agent_name;

    public MessageHistory (ArrayList<ACLMessage> msgs, String agent_name) {
        _agent_name = agent_name;
        HashMap<String, Integer> _agentsNames = new HashMap<>();
        for (ACLMessage msg : msgs) {
            if (msg != null) {
                if (msg.getSender().getName().contains("GlobalValues")) continue;
                if (msg.getSender().getName().contains("LoggingAgent")) continue;
                if (msg.getSender().getName().contains("WebServer")) continue;
                if (!_agentsNames.containsKey(msg.getSender().getName())) {
                    _agentsNames.put(msg.getSender().getName(), 0);
                }
            }
        }
        // Made unique list, count times
        for (ACLMessage msg : msgs) {
            if (msg != null) {
                _agentsNames.computeIfPresent(msg.getSender().getName(), (key, value) -> value + 1);
            }
        }
        _conversations = new ConversationCountList(_agentsNames);
    }

    private class ConversationCountList implements Serializable {
        private ArrayList<ConversationCount> _conversation_counts ;
        ConversationCountList (HashMap<String, Integer> map) {
            _conversation_counts = new ArrayList<>();
            Iterator it = map.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry pair = (Map.Entry)it.next();
                _conversation_counts.add(new ConversationCount(
                        (String) pair.getKey(), _agent_name, (Integer) pair.getValue()));
            }
        }
        public ArrayList<ConversationCount> getlinks() { return _conversation_counts;}
    }

    private class ConversationCount implements Serializable {
        private String source;
        private String target;
        private Integer value;
        ConversationCount (String sender, String receiver, Integer count) {
            source = sender;
            target = receiver;
            value = count;
            //LogDebug(sender + " spoke to " + receiver + " " + count + " times");
        }
        public String getsource () { return source; }
        public String gettarget () { return target; }
        public Integer getvalue () { return value; }
    }

    public String getMessages () {
        String json = "";
        try {
            json = new ObjectMapper().writeValueAsString(_conversations);
            // json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(_conversations);
            //LogDebug("Message count: " + json);
        }
        catch (JsonProcessingException e) {
            //LogError("Error parsing message data to json in " + getName() + " exeption thrown");
        }
        return json;
    }
}