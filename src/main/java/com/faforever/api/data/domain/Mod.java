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
@Include(rootLevel = true, type = Mod.TYPE_NAME)
@Immutable
@Setter
public class Mod {

  public static final String TYPE_NAME = "mod";

  private Integer id;
  private String uid;
  private String displayName;
  private String author;
  private OffsetDateTime createTime;
  private OffsetDateTime updateTime;
  private List<ModVersion> versions;
  private ModVersion latestVersion;
  private Player uploader;
  private int numberOfReviews;
  private float averageReviewScore;

  @Id
  @Column(name = "id")
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  public Integer getId() {
    return id;
  }

  @Column(name = "uid")
  @Size(max = 40)
  @NotNull
  public String getUid() {
    return uid;
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

  @Column(name = "reviews")
  public int getNumberOfReviews() {
    return numberOfReviews;
  }

  @Column(name = "average_review_score")
  public float getAverageReviewScore() {
    return averageReviewScore;
  }

  @ManyToOne
  @JoinColumn(name = "uploader")
  public Player getUploader() {
    return uploader;
  }

  @Column(name = "create_time")
  public OffsetDateTime getCreateTime() {
    return createTime;
  }

  @Formula(value = "(SELECT MAX(mod_version.update_time) FROM mod_version WHERE mod_version.mod_id = id)")
  public OffsetDateTime getUpdateTime() {
    return updateTime;
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
}
