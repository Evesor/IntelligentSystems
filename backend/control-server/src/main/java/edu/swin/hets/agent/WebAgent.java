package edu.swin.hets.agent;

import edu.swin.hets.helper.IMessageHandler;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Vector;

/******************************************************************************
 *  Use: Used to push data periodically to the WS
 *  Name: Always only have one of these on the main container, have it name set
 *        to "WebServer"
 *  Preformatives used:
 *       - inform : Used to ask send info to server
 *             content: "any info that the server needs."
 *****************************************************************************/
public class WebAgent extends BaseAgent{
    private Vector<String> _messages;

    private MessageTemplate InformMessageTemplate = MessageTemplate.MatchPerformative(ACLMessage.INFORM);

    protected void setup() {
        super.setup();
        _messages = new Vector<String>();
        addMessageHandler(InformMessageTemplate, new InfromMessageHandler());
    }


    protected void TimeExpired () {
        if (_messages.size() != 0) {
            new SendUpdate().Post(formatData(_messages));
            _messages.clear();
        }
    }

    protected String getJSON() {
        return "Not implemented";
    }

    protected void TimePush(int ms_left) {

    }

    // This is basically a hook for later, we likely will need a few classes for data types and a largish class for
    // formatting it properly.
    private String formatData (Vector<String> input) {
        String newS = "content=";
        for (String s: input) {
            newS.concat(s);
        }
        return newS;
    }

    private class InfromMessageHandler implements IMessageHandler {
        public void Handler(ACLMessage msg) {


        }
    }


    class SendUpdate  {
        String _url = "http://hello-udacity-170204.appspot.com/" ;

        SendUpdate() {
            this("");
        }

        SendUpdate(String url_extension) {
            _url += url_extension;
        }

        void Post(String message) {
            System.out.println("Sending a post");
            try {
                URL url = new URL (_url);
                String formatted_message = message;

                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setDoOutput(true);
                connection.setRequestProperty ("Accept-Charset", "UTF-8");
                connection.setFixedLengthStreamingMode(formatted_message.length());

                // Write out
                OutputStreamWriter stream_out = new OutputStreamWriter(connection.getOutputStream());
                stream_out.write(formatted_message);
                stream_out.close();
                System.out.println(formatted_message);

            } catch(Exception e) {
                e.printStackTrace();
            }
        }
    }
}