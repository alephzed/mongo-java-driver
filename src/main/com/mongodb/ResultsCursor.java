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

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

class ResultsCursor implements MongoCursor {
    private ServerAddress serverAddress;
    private Iterator<DBObject> iterator;
    private DBCollection collection;
    private int batchSize;
    private long cursorId;

    @SuppressWarnings("unchecked")
    public ResultsCursor(CommandResult res, DBCollection collection, int batchSize) {
        this.collection = collection;
        this.batchSize = batchSize;
        serverAddress = res.getServerUsed();

        Map cursor = (Map) res.get("cursor");
        if (cursor != null) {
            cursorId = (Long) cursor.get("id");
            List<DBObject> firstBatch = (List<DBObject>) cursor.get("firstBatch");
            iterator = firstBatch.iterator();
        } else {
            List<DBObject> result = (List<DBObject>) res.get("result");
            iterator = result.iterator();
        }
    }

    public long getCursorId() {
        return cursorId;
    }

    public ServerAddress getServerAddress() {
        return serverAddress;
    }

    public void close() throws IOException {
    }

    public boolean hasNext() {
        if (iterator.hasNext()) {
            return true;
        }
        if (cursorId == 0) {
            return false;
        }
        advance();

        return iterator.hasNext();
    }

    public DBObject next() {
        if (hasNext()) {
            return iterator.next();
        }
        throw new NoSuchElementException("no more");
    }

    public void remove() {
        throw new UnsupportedClassVersionError("Removes are not supported with cursors");
    }

    private void advance() {
        if (getCursorId() <= 0) {
            throw new RuntimeException("can't advance a cursor <= 0");
        }

        OutMessage m = OutMessage.getMore(collection, getCursorId(), batchSize);

        DBApiLayer db = (DBApiLayer) collection.getDB();
        Response res = db._connector.call(db, collection, m, getServerAddress(), getDecoder());
        DBApiLayer.throwOnQueryFailure(res, getCursorId());

        iterator = res.iterator();
        cursorId = res.cursor();
    }

    private DBDecoder getDecoder() {
        return collection.getDBDecoderFactory() != null ? collection.getDBDecoderFactory().create() : null;
    }

}
