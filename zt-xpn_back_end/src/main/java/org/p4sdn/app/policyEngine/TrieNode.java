/**
 * ZT-XPN: An end-to-end Zero-Trust Architecture for Next Generation 
 * Programmable Networks
 * 
 * Authors:  Charalampos Katsis  (ckatsis@purdue.edu)
 *           Elisa Bertino       (bertino@purdue.edu)
 * =================================================================
 */


package org.p4sdn.app.policyEngine;

import java.util.HashMap;
import java.util.Map;

import org.p4sdn.app.net.Protocol;

public class TrieNode {
    Map<String, TrieNode> children;
    Protocol policy;

    public TrieNode() {
        children = new HashMap<>();
        policy = null;
    }
    
}
