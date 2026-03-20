/*
 * Copyright (C) 2026 BlissLabs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.android.settings.deviceinfo.firmwareversion;

import android.content.Context;
import com.android.settings.core.BasePreferenceController;

public class VulkanVersionPreferenceController extends BasePreferenceController {

    public VulkanVersionPreferenceController(Context context, String preferenceKey) {
        super(context, preferenceKey);
    }

    @Override
    public int getAvailabilityStatus() {
        return AVAILABLE;
    }

    @Override
    public CharSequence getSummary() {
        try {
            VulkanUtils.VkPhysicalDevices devices = VulkanUtils.getVkInfo();
            if (devices != null && !devices.isEmpty()) {
                return devices.get(0).getFormattedSummary();
            }
        } catch (Exception | UnsatisfiedLinkError e) {
            // Failsafe if the native library isn't loaded properly
        }
        return "Vulkan not supported or info unavailable";
    }
}
