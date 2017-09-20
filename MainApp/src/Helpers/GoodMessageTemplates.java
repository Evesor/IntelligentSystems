package Helpers;

import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class GoodMessageTemplates extends MessageTemplate{
    public GoodMessageTemplates (MessageTemplate.MatchExpression e) {
        super(e);
    }

    public static GoodMessageTemplates ContatinsString (String value) {
        return new GoodMessageTemplates(new ContainsString(value));
    }

    private static class ContainsString implements MessageTemplate.MatchExpression {
        private String _str;

        public ContainsString (String s) {
            _str = s;
        }

        public boolean match(ACLMessage aclMessage) {
            return aclMessage.getContent().contains(_str);
        }
    }
}
