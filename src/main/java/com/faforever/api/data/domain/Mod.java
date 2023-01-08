package com.faforever.api.data.domain;

import com.faforever.api.data.checks.Prefab;
import com.faforever.api.data.listeners.ModChangeListener;
import com.faforever.api.security.elide.permission.AdminModCheck;
import com.yahoo.elide.annotation.Include;
import com.yahoo.elide.annotation.UpdatePermission;
import lombok.Setter;
import org.hibernate.annotations.JoinColumnOrFormula;
import org.hibernate.annotations.JoinColumnsOrFormulas;
import org.hibernate.annotations.JoinFormula;
import org.hibernate.validator.constraints.NotEmpty;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

@Entity
@Table(name = "\"mod\"")
@Include(name = Mod.TYPE_NAME)
@Setter
@EntityListeners(ModChangeListener.class)
public class Mod extends AbstractEntity<Mod> implements OwnableEntity {

  public static final String TYPE_NAME = "mod";

  private boolean recommended;
  private String displayName;
  private String author;
  private List<ModVersion> versions;
  private ModVersion latestVersion;
  private Player uploader;
  private ModReviewsSummary reviewsSummary;

  @Column(name = "recommended")
  @NotNull
  @UpdatePermission(expression = AdminModCheck.EXPRESSION)
  public boolean getRecommended() {
    return recommended;
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
