/*        Copyright 2024 Aziz Atoev

* Licensed under the Apache License, Version 2.0 (the "License");
* You may not use this file except in compliance with the License.
* You may obtain a copy of the License at

        Apache License, Version 2.0

* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package properties

import kotlin.properties.ReadOnlyProperty
import kotlinx.coroutines.flow.StateFlow

interface StateFlowPropertyDelegate<T> : ReadOnlyProperty<Properties, StateFlow<T>>
