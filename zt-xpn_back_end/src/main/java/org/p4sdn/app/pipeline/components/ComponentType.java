/**
 * ZT-XPN: An end-to-end Zero-Trust Architecture for Next Generation 
 * Programmable Networks
 * 
 * Authors:  Charalampos Katsis  (ckatsis@purdue.edu)
 *           Elisa Bertino       (bertino@purdue.edu)
 * =================================================================
 */


package org.p4sdn.app.pipeline.components;

public enum ComponentType {
    INCLUDE,
    DEFINE,
    VARIABLE,
    HEADER,
    ANNOTATION,
    CONTROL,
    CONTROL_PARAMETER,
    ACTION_PARAMETER,
    STRUCT,
    STRUCT_MEMBER,
    PARSER,
    PARSER_STATE,
    PARSER_ACTION,
    TABLE,
    CODE_BLOCK,
    FLOW_COUNTER,
    REGISTER
}
