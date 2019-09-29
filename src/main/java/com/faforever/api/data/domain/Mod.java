package com.faforever.api.data.domain;

import com.yahoo.elide.annotation.Include;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Formula;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.JoinColumnOrFormula;
import org.hibernate.annotations.JoinColumnsOrFormulas;
import org.hibernate.annotations.JoinFormula;

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
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.OffsetDateTime;
import java.util.List;

@Entity
@Table(name = "\"mod\"")
@Include(rootLevel = true, type = Mod.TYPE_NAME)
@Immutable
@Getter
@Setter
public class Mod {

  public static final String TYPE_NAME = "mod";

  @Id
  @Column(name = "id")
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @Column(name = "display_name")
  @Size(max = 100)
  @NotNull
  private String displayName;

  @Column(name = "author")
  @Size(max = 100)
  @NotNull
  private String author;

  @Column(name = "reviews")
  private int numberOfReviews;

  @Column(name = "average_review_score")
  private float averageReviewScore;

  @ManyToOne
  @JoinColumn(name = "uploader")
  private Player uploader;

  @Column(name = "create_time")
  private OffsetDateTime createTime;

  @Formula(value = "(SELECT MAX(mod_version.update_time) FROM mod_version WHERE mod_version.mod_id = id)")
  private OffsetDateTime updateTime;

  @OneToMany(mappedBy = "mod", cascade = CascadeType.ALL, orphanRemoval = true)
  @NotEmpty
  @Valid
  private List<ModVersion> versions;

  @ManyToOne
  @JoinColumnsOrFormulas({
    @JoinColumnOrFormula(
      formula = @JoinFormula(
        value = "(SELECT mod_version.id FROM mod_version WHERE mod_version.mod_id = id ORDER BY mod_version.version DESC LIMIT 1)",
        referencedColumnName = "id")
    )
  })
  private ModVersion latestVersion;
}
