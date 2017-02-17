package com.faforever.api.data.domain;

import com.yahoo.elide.annotation.Include;
import lombok.Setter;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.JoinColumnOrFormula;
import org.hibernate.annotations.JoinColumnsOrFormulas;
import org.hibernate.annotations.JoinFormula;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
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

  @Id
  @Column(name = "id")
  public Integer getId() {
    return id;
  }

  @Column(name = "display_name")
  public String getDisplayName() {
    return displayName;
  }

  @Column(name = "author")
  public String getAuthor() {
    return author;
  }

  @Column(name = "create_time")
  public OffsetDateTime getCreateTime() {
    return createTime;
  }

  @Column(name = "update_time")
  public OffsetDateTime getUpdateTime() {
    return updateTime;
  }

  @OneToMany(mappedBy = "mod")
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
