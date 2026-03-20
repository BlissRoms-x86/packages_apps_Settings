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

import java.util.ArrayList;

public class VulkanUtils {
    static {
        System.loadLibrary("settings_vulkan");
    }

    public static class VkPhysicalDevices extends ArrayList<VkPhysicalDevice> {
        public boolean addDevice(long apiVersion, long driverVersion, long vendorId, long deviceId, long deviceType, String deviceName) {
            return add(new VkPhysicalDevice(apiVersion, driverVersion, vendorId, deviceId, deviceType, deviceName));
        }
    }

    public static class VkPhysicalDevice {
        public final long apiVersion;
        public final long driverVersion;
        public final long vendorId;
        public final long deviceId;
        public final long deviceType;
        public final String deviceName;

        public VkPhysicalDevice(long apiVersion, long driverVersion, long vendorId, long deviceId, long deviceType, String deviceName) {
            this.apiVersion = apiVersion;
            this.driverVersion = driverVersion;
            this.vendorId = vendorId;
            this.deviceId = deviceId;
            this.deviceType = deviceType;
            this.deviceName = deviceName;
        }

        public String getFormattedSummary() {
            // Decode Vulkan API Version (Variant is ignored here for simplicity)
            int apiMajor = (int) ((apiVersion >> 22) & 0x7F);
            int apiMinor = (int) ((apiVersion >> 12) & 0x3FF);
            int apiPatch = (int) (apiVersion & 0xFFF);

            // Decode Driver Version (Assuming Mesa layout)
            int drvMajor = (int) ((driverVersion >> 22) & 0x3FF);
            int drvMinor = (int) ((driverVersion >> 12) & 0x3FF);
            int drvPatch = (int) (driverVersion & 0xFFF);

            String typeStr = "Other";
            if (deviceType == 1) typeStr = "Integrated GPU";
            else if (deviceType == 2) typeStr = "Discrete GPU";
            else if (deviceType == 3) typeStr = "Virtual GPU";
            else if (deviceType == 4) typeStr = "CPU";

            return "Device Name: " + deviceName + "\n" +
                   "Device Type: " + typeStr + "\n" +
                //    "Vendor ID: " + String.format("0x%04X", vendorId) + "\n" +
                //    "Device ID: " + String.format("0x%04X", deviceId) + "\n" +
                   "API Version: " + apiMajor + "." + apiMinor + "." + apiPatch + " (" + apiVersion + ")\n" +
                   "Driver Version: " + drvMajor + "." + drvMinor + "." + drvPatch + " (" + driverVersion + ")";
        }
    }

    public static native VkPhysicalDevices getVkInfo();
}
