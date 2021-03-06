//
//
// Copyright 2012 Kii Corporation
// http://kii.com
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//
//

package com.kii.world;

import android.app.Application;
import com.kii.cloud.storage.Kii;
import com.kii.cloud.storage.Kii.Site;

public class HelloKii extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // initialize the Kii SDK!
        Kii.initialize("844722b7", "e6dcfa638e000507e04f7e6a614b3318", Site.JP);
    }

    public static boolean isActivityVisible() {
        return activityVisible;
    }

    public static void activityResumed() {
        activityVisible = true;
    }

    public static void activityPaused() {
        activityVisible = false;
    }

    private static boolean activityVisible;

}
