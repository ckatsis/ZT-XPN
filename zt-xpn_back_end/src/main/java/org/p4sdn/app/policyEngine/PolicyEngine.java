/**
 * ZT-XPN: An end-to-end Zero-Trust Architecture for Next Generation 
 * Programmable Networks
 * 
 * Authors:  Charalampos Katsis  (ckatsis@purdue.edu)
 *           Elisa Bertino       (bertino@purdue.edu)
 * =================================================================
 */


package org.p4sdn.app.policyEngine;

import org.p4sdn.app.net.Protocol;
import org.slf4j.Logger;

public final class PolicyEngine {

    private static final PolicyTrie trie = new PolicyTrie();

    private PolicyEngine() {
        // trie = new PolicyTrie();
    }

    public static void setLogger(Logger l) {
        trie.setLogger(l);
    }

    public static void addPolicy(Protocol policy) {
        trie.insert(policy);
    }

    public static Protocol matchPacket(Protocol packet) {
        return trie.search(packet);
    }
}
