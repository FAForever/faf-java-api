package com.faforever.api.data.domain;

import com.yahoo.elide.annotation.Include;
import lombok.Setter;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.JoinColumnOrFormula;
import org.hibernate.annotations.JoinColumnsOrFormulas;
import org.hibernate.annotations.JoinFormula;
import org.hibernate.validator.constraints.NotEmpty;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
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
public class Mod extends AbstractEntity implements OwnableEntity {

  public static final String TYPE_NAME = "mod";

  private String displayName;
  private String author;
  private List<ModVersion> versions;
  private ModVersion latestVersion;
  private Player uploader;
  private int numberOfReviews;
  private float averageReviewScore;

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

  @Transient
  @Override
  public Login getEntityOwner() {
    return uploader;
  }
}
