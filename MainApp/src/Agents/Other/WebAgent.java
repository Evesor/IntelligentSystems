package Agents.Other;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Vector;
/******************************************************************************
 *  Use: Used to push data periodically to the WS
 *  Preformatives used:
 *       - inform : Used to ask send info to server
 *             content: "any info that the server needs."
 *****************************************************************************/
public class WebAgent extends Agent{
    private Vector<String> _messages;

    protected void setup() {
        _messages = new Vector<String>();

        // Tick every second to check for updates and push if there are any.
        addBehaviour(new TickerBehaviour(this, 1000) {
            @Override
            protected void onTick() {
                if (_messages.size() != 0) {
                    new SendUpdate().Post(formatData(_messages));
                    _messages.clear();
                }
            }
        });

        // Deal with any messages to this agent by adding them to the messages vector
        addBehaviour(new CyclicBehaviour(this) {
            @Override
            public void action() {
                ACLMessage msg = receive();
                if (msg != null) {
                    if (msg.getPerformative() == ACLMessage.INFORM) {
                        // Should do some formating and error checking here.
                        _messages.add(msg.getContent());
                    }
                    else { //Not an inform message, send back a not understood.
                        ACLMessage response = new ACLMessage();
                        response.setPerformative(ACLMessage.NOT_UNDERSTOOD);
                        response.setInReplyTo(msg.getReplyWith());
                        response.addReceiver(msg.getSender());
                        send(response);
                    }
                }
                block();
            }
        });

    }

    // This is basically a hook for later, we likely will need a few classes for data types and a largish class for
    // formatting it properly.
    private String formatData (Vector<String> input) {
        String newS = "content=";
        for (String s: input) {
            newS += s;
        }
        return newS;
    }


    class SendUpdate  {
        String _url = "http://hello-udacity-170204.appspot.com/" ;

        public SendUpdate() {
            this("");
        }

        public SendUpdate(String url_extension) {
            _url += url_extension;
        }

        public void Post(String message) {
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
