/*
 *  Copyright 2016 NAVER Corp.
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.navercorp.pinpoint.plugin.strace;

import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;

/**
 * @author haiman
 */
public class StraceConfig {

    private final boolean profile;

    public StraceConfig(ProfilerConfig config) {
        this.profile = config.readBoolean("profiler.trace.Strace", true);
    }

    public boolean isProfile() {
        return profile;
    }

    @Override
    public String toString() {
        return "StraceConfig{" +"profile=" + profile + '}';
    }

}
