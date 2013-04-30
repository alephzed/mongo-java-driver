/*
 * Copyright (c) 2008 - 2013 10gen, Inc. <http://10gen.com>
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

package org.mongodb;


import org.mongodb.impl.MongoConnectionStrategiesImpl;

import java.util.List;

public final class MongoConnectionStrategies {
    private MongoConnectionStrategies() {
    }

    public static MongoConnectionStrategy replicaSet(final List<ServerAddress> serverAddressSeedList, final MongoClientOptions options) {
        return MongoConnectionStrategiesImpl.replicaSet(serverAddressSeedList, options);
    }

    public static MongoConnectionStrategy mongosHighAvailability(final List<ServerAddress> serverAddressList,
                                                                 final MongoClientOptions options) {
        return MongoConnectionStrategiesImpl.mongosHighAvailability(serverAddressList, options);
    }
}