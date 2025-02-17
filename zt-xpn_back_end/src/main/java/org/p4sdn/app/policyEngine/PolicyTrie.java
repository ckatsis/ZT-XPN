/**
 * ZT-XPN: An end-to-end Zero-Trust Architecture for Next Generation 
 * Programmable Networks
 * 
 * Authors:  Charalampos Katsis  (ckatsis@purdue.edu)
 *           Elisa Bertino       (bertino@purdue.edu)
 * =================================================================
 */


package org.p4sdn.app.policyEngine;

import java.util.ArrayList;

import org.p4sdn.app.net.Protocol;
import org.slf4j.Logger;

public class PolicyTrie {

    private final TrieNode root;
    private Logger log;


    public PolicyTrie() {
        root = new TrieNode();
    }

    public void setLogger(Logger l) {
        this.log = l;
    }

    public void insert(Protocol policy) {
        TrieNode node = root;
        ArrayList<String> properties = getPropertyValues(policy);

        log.info("Policy: " + properties.toString());

        for (String field : properties) {
            if (!node.children.containsKey(field)) {
                node.children.put(field, new TrieNode());
            }
            node = node.children.get(field);
        }

        node.policy = policy;
    }

    public Protocol search(Protocol packet) {
        String[] properties = getPropertyValues(packet).toArray(new String[0]);
        // log.info("Packet: " + properties);

        String out = "";

        // Printing the String array
        for (String s : properties) {
            // System.out.println(s);
            out += s + " ";
        }

        // log.info("Packet: " + out);

        return searchHelper(root, properties, 0);
    }

    public ArrayList<String> getPropertyValues(Protocol proto) {
        ArrayList<String> properties = new ArrayList<>();
        Protocol curr = proto;

        while (curr != null) {
            properties.addAll(curr.getProtocolPropertyValues());
            curr = curr.getEncapsulated_proto();
        }

        return properties;
    }

    private Protocol searchHelper(TrieNode node, String[] fields, int index) {
        if (index == fields.length) {
            return node.policy;
        }

        // Try exact match
        if (node.children.containsKey(fields[index])) {
            Protocol result = searchHelper(node.children.get(fields[index]), fields, index + 1);
            if (result != null) {
                return result;
            }
        }

        // Try wildcard match
        if (node.children.containsKey("-1")) {
            Protocol result = searchHelper(node.children.get("-1"), fields, index + 1);
            if (result != null) {
                return result;
            }
        }

        return null;
    }
    
}
