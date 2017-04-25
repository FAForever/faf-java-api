/**
 * Contains classes to access data according to the <a href="http://jsonapi.org/">JSON-API specification</a>.
 */
@SharePermission(expression = "Prefab.Role.All")
// Everybody can read from the api
@ReadPermission(expression = "Prefab.Role.All")
// By default restrict all data manipulation operation
@UpdatePermission(expression = "Prefab.Role.None")
@CreatePermission(expression = "Prefab.Role.None")
@DeletePermission(expression = "Prefab.Role.None")
package com.faforever.api.data;

import com.yahoo.elide.annotation.CreatePermission;
import com.yahoo.elide.annotation.DeletePermission;
import com.yahoo.elide.annotation.ReadPermission;
import com.yahoo.elide.annotation.SharePermission;
import com.yahoo.elide.annotation.UpdatePermission;
