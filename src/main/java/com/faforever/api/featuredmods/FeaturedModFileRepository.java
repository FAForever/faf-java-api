package com.faforever.api.featuredmods;

import org.springframework.data.repository.Repository;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.List;
import java.util.regex.Pattern;

@org.springframework.stereotype.Repository
public class FeaturedModFileRepository implements Repository<FeaturedModFile, Integer> {
  private static final Pattern MOD_NAME_PATTERN = Pattern.compile("[a-z]+");
  private final EntityManager entityManager;

  public FeaturedModFileRepository(EntityManager entityManager) {
    this.entityManager = entityManager;
  }

  @SuppressWarnings("unchecked")
  public List<FeaturedModFile> getFiles(String modName, Integer version) {
    if (!MOD_NAME_PATTERN.matcher(modName).matches()) {
      throw new IllegalArgumentException("Invalid mod name: " + modName);
    }

    // The following joke is sponsored by FAF's patcher mechanism which shouldn't even require a DB.
    Query query = entityManager.createNativeQuery(String.format(
        "SELECT" +
            "  b.filename   AS `name`," +
            "  file.version AS `version`," +
            "  b.path       AS `group`," +
            "  file.md5     AS `md5`," +
            "  file.id      AS `id`," +
            "  file.name    AS `url`," +
            "  'updates_%1$s_files'    AS `folderName`" +
            "FROM updates_%1$s_files file" +
            "  INNER JOIN" +
            "  (" +
            "    SELECT" +
            "      fileId," +
            "      MAX(version) AS version" +
            "    FROM updates_%1$s_files" +
            "       %2$s" +
            "    GROUP BY fileId" +
            "  ) latest" +
            "    ON file.fileId = latest.fileId" +
            "       AND file.version = latest.version" +
            "  LEFT JOIN updates_%1$s b" +
            "    ON b.id = file.fileId;", modName, (version == null ? "" : "WHERE version <= :version")), FeaturedModFile.class);

    if (version != null) {
      query.setParameter("version", version);
    }
    return (List<FeaturedModFile>) query.getResultList();
  }
}
