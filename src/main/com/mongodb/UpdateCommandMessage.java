/*
 * Copyright (c) 2008 MongoDB, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mongodb;

import org.bson.io.OutputBuffer;

import java.util.List;

class UpdateCommandMessage extends BaseWriteCommandMessage {
    private final List<Update> updates;
    private final DBEncoder encoder;

    public UpdateCommandMessage(final MongoNamespace writeNamespace, final WriteConcern writeConcern,
                                final List<Update> updates, final DBEncoder commandEncoder, final DBEncoder encoder,
                                final MessageSettings settings) {
        super(writeNamespace, writeConcern, commandEncoder, settings);
        this.updates = updates;
        this.encoder = encoder;
    }

    @Override
    protected UpdateCommandMessage writeTheWrites(final OutputBuffer buffer, final int commandStartPosition,
                                                  final BSONBinaryWriter writer) {
        UpdateCommandMessage nextMessage = null;
        writer.writeStartArray("updates");
        for (int i = 0; i < updates.size(); i++) {
            writer.mark();
            Update update = updates.get(i);
            writer.writeStartDocument();
            writer.pushMaxDocumentSize(getSettings().getMaxDocumentSize());
            writer.writeName("q");
            writer.encodeDocument(getCommandEncoder(), update.getFilter());
            writer.writeName("u");
            writer.encodeDocument(encoder, update.getUpdateOperations());
            writer.writeBoolean("multi", update.isMulti());
            writer.writeBoolean("upsert", update.isUpsert());
            writer.popMaxDocumentSize();
            writer.writeEndDocument();
            if (maximumCommandDocumentSizeExceeded(buffer, commandStartPosition)) {
                writer.reset();
                nextMessage = new UpdateCommandMessage(getWriteNamespace(), getWriteConcern(), updates.subList(i, updates.size()),
                                                       getCommandEncoder(), encoder, getSettings());
                break;
            }
        }
        writer.writeEndArray();
        return nextMessage;
    }

    @Override
    protected String getCommandName() {
        return "update";
    }
}