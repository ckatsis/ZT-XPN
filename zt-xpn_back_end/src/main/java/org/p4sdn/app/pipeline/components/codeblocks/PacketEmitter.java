package org.p4sdn.app.pipeline.components.codeblocks;

import org.p4sdn.app.pipeline.BMV2Pipeline;
import org.p4sdn.app.pipeline.components.InLineStatement;
import org.p4sdn.app.pipeline.components.Struct;
import org.p4sdn.app.pipeline.components.StructMember;

public class PacketEmitter extends CodeBlock {
    public static final String EMIT_ACTION_NAME = "emit";
    public static final String NAME = "PacketEmitter";

    public PacketEmitter(Struct parsedHeaders) {
        super(NAME);
        // Make sure first it is sorted
        parsedHeaders.sortMemberList();
        String prefix = parsedHeaders.getName().replace(STRUCT_EXTENSION, "") + DOT;

        for(StructMember member: parsedHeaders.getMemberList()) {
            String emittedHeader = prefix + member.getName();
            String emitCmd = BMV2Pipeline.DEPARSED_PACKET_PARAMETER_NAME + DOT + EMIT_ACTION_NAME + LEFT_PAR +
                            emittedHeader + RIGHT_PAR;
            InLineStatement statement = new InLineStatement(null, this, emitCmd);
            addStatements(statement);
        }
    }

}
