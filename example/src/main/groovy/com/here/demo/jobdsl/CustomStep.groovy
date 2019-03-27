/*
 * Copyright (C) 2016-2019 HERE Europe B.V.
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
 *
 * SPDX-License-Identifier: Apache-2.0
 * License-Filename: LICENSE
 */

package com.here.demo.jobdsl

import javaposse.jobdsl.dsl.helpers.step.StepContext

/**
 * Example for a class that implements a custom method to use in DSL scripts. This is useful when you want to re-use the
 * same build step in many different jobs.<br>
 * In this example the {@link StepContext} is used because the method will be used in the step closure of the DSL
 * script. If you want to add a custom method for another part of Job DSL you will have to use another context class.
 */
class CustomStep {

    static void echo(StepContext context, String output) {
        context.with {
            shell("echo ${output}")
        }
    }

}
