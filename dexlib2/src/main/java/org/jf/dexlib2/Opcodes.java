/*
 * Copyright 2013, Google Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 *     * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above
 * copyright notice, this list of conditions and the following disclaimer
 * in the documentation and/or other materials provided with the
 * distribution.
 *     * Neither the name of Google Inc. nor the names of its
 * contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.jf.dexlib2;

import java.util.HashMap;

import javax.annotation.Nullable;

import com.google.common.collect.Maps;

public class Opcodes {
    public static final int OFFSET_OAT_VERSION = 16;
    public final Version version;
    private final Opcode[] opcodesByValue;
    private final HashMap<String, Opcode> opcodesByName;

    public static class Version {
        public final int api;
        public final int oat;

        public Version(int apiLevel, int oatVersion) {
            api = apiLevel;
            if (oatVersion == 0) {
                switch (apiLevel) {
                    case Opcode.API_L:
                        oat = 39;
                        break;
                    case Opcode.API_L_MR1:
                        oat = 45;
                        break;
                    case Opcode.API_M:
                        oat = 64;
                        break;
                    case Opcode.API_N:
                        oat = 79;
                        break;
                    default:
                        oat = oatVersion;
                }
            } else {
                oat = oatVersion;
            }
        }

        public Version(int api) {
            this(api, 0);
        }

        @Override
        public String toString() {
            return "apiLevel=" + api + " oatVersion=" + oat;
        }
    }

    public Opcodes(int api) {
        this(api, false);
    }

    public Opcodes(int api, boolean experimental) {
        int apiLevel = api & ((1 << OFFSET_OAT_VERSION) - 1);
        int oatVersion = api >> OFFSET_OAT_VERSION;
        version = new Version(apiLevel, oatVersion);
        opcodesByValue = new Opcode[256];
        opcodesByName = Maps.newHashMap();

        for (Opcode opcode: Opcode.values()) {
            if (!opcode.format.isPayloadFormat) {
                if (apiLevel <= opcode.getMaxApi() && apiLevel >= opcode.getMinApi() &&
                        (experimental || !opcode.isExperimental())) {
                    opcodesByValue[opcode.value] = opcode;
                    opcodesByName.put(opcode.name.toLowerCase(), opcode);
                }
            }
        }
    }

    @Nullable
    public Opcode getOpcodeByName(String opcodeName) {
        return opcodesByName.get(opcodeName.toLowerCase());
    }

    @Nullable
    public Opcode getOpcodeByValue(int opcodeValue) {
        switch (opcodeValue) {
            case 0x100:
                return Opcode.PACKED_SWITCH_PAYLOAD;
            case 0x200:
                return Opcode.SPARSE_SWITCH_PAYLOAD;
            case 0x300:
                return Opcode.ARRAY_PAYLOAD;
            default:
                if (opcodeValue >= 0 && opcodeValue < opcodesByValue.length) {
                    return opcodesByValue[opcodeValue];
                }
                return null;
        }
    }
}
