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
@AnalyzerDef(name = "case_insensitive",
  tokenizer = @TokenizerDef(factory = NGramTokenizerFactory.class, params = {
    @Parameter(name = "minGramSize", value = "3"),
    @Parameter(name = "maxGramSize", value = "10")
  }),
  filters = {
    @TokenFilterDef(factory = LowerCaseFilterFactory.class)
  }
)
package com.faforever.api.data;

import com.faforever.api.data.checks.Prefab;
import com.yahoo.elide.annotation.CreatePermission;
import com.yahoo.elide.annotation.DeletePermission;
import com.yahoo.elide.annotation.ReadPermission;
import com.yahoo.elide.annotation.SharePermission;
import com.yahoo.elide.annotation.UpdatePermission;
import org.apache.lucene.analysis.core.LowerCaseFilterFactory;
import org.apache.lucene.analysis.ngram.NGramTokenizerFactory;
import org.hibernate.search.annotations.AnalyzerDef;
import org.hibernate.search.annotations.Parameter;
import org.hibernate.search.annotations.TokenFilterDef;
import org.hibernate.search.annotations.TokenizerDef;
