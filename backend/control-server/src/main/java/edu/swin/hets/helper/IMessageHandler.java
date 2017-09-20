package edu.swin.hets.helper;

import jade.lang.acl.ACLMessage;

/******************************************************************************
 *  Use: Get around the fact that java does not have function pointers or
 *       delegates, "sighs loudly"
 *****************************************************************************/
public interface IMessageHandler {
    void Handler(ACLMessage msg);
}
