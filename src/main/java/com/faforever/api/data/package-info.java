/**
 * Contains classes to access data according to the <a href="http://jsonapi.org/">JSON-API specification</a>.
 */
@SharePermission
// Everybody can read from the api
@ReadPermission(expression = Prefab.ALL)
// By default restrict all data manipulation operation
@UpdatePermission(expression = Prefab.NONE)
@CreatePermission(expression = Prefab.NONE)
@DeletePermission(expression = Prefab.NONE)
package com.faforever.api.data;

import com.faforever.api.data.checks.Prefab;
import com.yahoo.elide.annotation.CreatePermission;
import com.yahoo.elide.annotation.DeletePermission;
import com.yahoo.elide.annotation.ReadPermission;
import com.yahoo.elide.annotation.SharePermission;
import com.yahoo.elide.annotation.UpdatePermission;
