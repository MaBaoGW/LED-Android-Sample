/*
 * Copyright (C) 2017 MaBaoGW
 *
 * Source Link: https://github.com/MaBaoGW
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package sample.mabaogw.ledbarsample.ble;

import java.util.HashMap;

public class SampleGattAttributes {

    public static String LED_CONFIG = "6e400002-b5a3-f393-e0a9-e50e24dcca9e";

    private static HashMap<String, String> attributes = new HashMap<String, String>();

    static {
        attributes.put(LED_CONFIG, "LED");
    }

    public static String lookup(String uuid, String defaultName) {
        String name = attributes.get(uuid);
        return name == null ? defaultName : name;
    }
}
