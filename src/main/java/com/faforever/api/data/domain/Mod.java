package com.faforever.api.data.domain;

import com.faforever.api.data.checks.Prefab;
import com.yahoo.elide.annotation.Include;
import com.yahoo.elide.annotation.UpdatePermission;
import lombok.Setter;
import org.apache.lucene.analysis.core.LowerCaseFilterFactory;
import org.apache.lucene.analysis.ngram.NGramTokenizerFactory;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.JoinColumnOrFormula;
import org.hibernate.annotations.JoinColumnsOrFormulas;
import org.hibernate.annotations.JoinFormula;
import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.Analyzer;
import org.hibernate.search.annotations.AnalyzerDef;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.Parameter;
import org.hibernate.search.annotations.Store;
import org.hibernate.search.annotations.TokenFilterDef;
import org.hibernate.search.annotations.TokenizerDef;
import org.hibernate.validator.constraints.NotEmpty;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

@Entity
@Table(name = "\"mod\"")
@Include(rootLevel = true, type = Mod.TYPE_NAME)
@Immutable
@Setter
@Indexed
public class Mod extends AbstractEntity implements OwnableEntity {

  public static final String TYPE_NAME = "mod";

  private String displayName;
  private String author;
  private List<ModVersion> versions;
  private ModVersion latestVersion;
  private Player uploader;
  private ModReviewsSummary reviewsSummary;

  @Column(name = "display_name")
  @Size(max = 100)
  @NotNull
  @Field(index = Index.YES, analyze = Analyze.YES,
    store = Store.NO, analyzer = @Analyzer(definition = "case_insensitive"))
  public String getDisplayName() {
    return displayName;
  }

  @Column(name = "author")
  @Size(max = 100)
  @NotNull
  public String getAuthor() {
    return author;
  }

  @ManyToOne
  @JoinColumn(name = "uploader")
  public Player getUploader() {
    return uploader;
  }

  @OneToMany(mappedBy = "mod", cascade = CascadeType.ALL, orphanRemoval = true)
  @NotEmpty
  @Valid
  public List<ModVersion> getVersions() {
    return versions;
  }

  @ManyToOne
  @JoinColumnsOrFormulas({
    @JoinColumnOrFormula(
      formula = @JoinFormula(
        value = "(SELECT mod_version.id FROM mod_version WHERE mod_version.mod_id = id ORDER BY mod_version.version DESC LIMIT 1)",
        referencedColumnName = "id")
    )
  })
  public ModVersion getLatestVersion() {
    return latestVersion;
  }

  @OneToOne(mappedBy = "mod")
  @UpdatePermission(expression = Prefab.ALL)
  public ModReviewsSummary getReviewsSummary() {
    return reviewsSummary;
  }

  @Transient
  @Override
  public Login getEntityOwner() {
    return uploader;
  }
}
