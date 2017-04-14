package com.faforever.api.data.domain;

import com.yahoo.elide.annotation.Include;
import lombok.Setter;
import org.hibernate.annotations.Formula;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.JoinColumnOrFormula;
import org.hibernate.annotations.JoinColumnsOrFormulas;
import org.hibernate.annotations.JoinFormula;
import org.hibernate.validator.constraints.NotEmpty;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.OffsetDateTime;
import java.util.List;

@Entity
@Table(name = "\"mod\"")
@Include(rootLevel = true, type = "mod")
@Immutable
@Setter
public class Mod {

  private Integer id;
  private String displayName;
  private String author;
  private OffsetDateTime createTime;
  private OffsetDateTime updateTime;
  private List<ModVersion> versions;
  private ModVersion latestVersion;
  private Player uploader;

  @Id
  @Column(name = "id")
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  public Integer getId() {
    return id;
  }

  @Column(name = "display_name")
  @Size(max = 100)
  @NotNull
  public String getDisplayName() {
    return displayName;
  }

  @Column(name = "author")
  @Size(max = 100)
  @NotNull
  public String getAuthor() {
    return author;
  }

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "uploader")
  public Player getUploader() {
    return uploader;
  }

  @Column(name = "create_time")
  public OffsetDateTime getCreateTime() {
    return createTime;
  }

  @Formula(value = "(SELECT mod_version.update_time FROM mod_version WHERE mod_version.mod_id = id ORDER BY mod_version.version DESC LIMIT 1)")
  public OffsetDateTime getUpdateTime() {
    return updateTime;
  }

  @OneToMany(mappedBy = "mod", cascade = CascadeType.ALL, orphanRemoval = true)
  @NotEmpty
  @Valid
  public List<ModVersion> getVersions() {
    return versions;
  }

  @ManyToOne(fetch = FetchType.LAZY)
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
}
