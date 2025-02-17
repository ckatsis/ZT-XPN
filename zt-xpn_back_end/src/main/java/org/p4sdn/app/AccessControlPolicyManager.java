/**
 * ZT-XPN: An end-to-end Zero-Trust Architecture for Next Generation 
 * Programmable Networks
 * 
 * Authors:  Charalampos Katsis  (ckatsis@purdue.edu)
 *           Elisa Bertino       (bertino@purdue.edu)
 * =================================================================
 */


package org.p4sdn.app;

import java.util.HashSet;

public final class AccessControlPolicyManager {

    // This is where we store all the policies
    private static HashSet<Requirement> commRequirements = new HashSet<Requirement>();

    private AccessControlPolicyManager() {}

    public void addRequirement(Requirement r) {
        commRequirements.add(r);
    }

    public boolean contains(Requirement r) {
        if(commRequirements.contains(r))
            return true;
        else 
            return false;
    }
}
