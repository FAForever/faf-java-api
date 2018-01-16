package com.faforever.api.featuredmods;

import org.springframework.data.repository.Repository;
import org.springframework.util.Assert;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@org.springframework.stereotype.Repository
public class LegacyFeaturedModFileRepository implements Repository<FeaturedModFile, Integer> {
  private static final Pattern MOD_NAME_PATTERN = Pattern.compile("[a-z0-9]+");
  private final EntityManager entityManager;

  public LegacyFeaturedModFileRepository(EntityManager entityManager) {
    this.entityManager = entityManager;
  }

  @SuppressWarnings("unchecked")
  public List<FeaturedModFile> getFiles(String modName, Integer version) {
    // Please shoot me.
    String innerModName = "ladder1v1".equals(modName) ? "faf" : modName;
    verifyModName(innerModName);

    // The following joke is sponsored by FAF's patcher mechanism which shouldn't even require a DB.
    Query query = entityManager.createNativeQuery(String.format(
      "SELECT" +
        "  b.filename   AS `name`," +
        "  file.version AS `version`," +
        "  b.path       AS `group`," +
        "  file.md5     AS `md5`," +
        "  file.fileId  AS `fileId`," +
        "  file.id      AS `id`," +
        "  file.name    AS `fileName`," +
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
        "    ON b.id = file.fileId;", innerModName, (version == null ? "" : "WHERE version <= :version")), FeaturedModFile.class);

    if (version != null) {
      query.setParameter("version", version);
    }
    return (List<FeaturedModFile>) query.getResultList();
  }

  public void save(String modName, short version, List<FeaturedModFile> featuredModFiles) {
    // Please shoot me.
    String innerModName = "ladder1v1".equals(modName) ? "faf" : modName;
    verifyModName(innerModName);

    // Upsert would be preferred, but the tables have no unique constraints and it's not worth fixing them
    Query deleteQuery = entityManager.createNativeQuery(String.format(
      "DELETE FROM updates_%s_files WHERE version = :version", innerModName
    ));
    deleteQuery.setParameter("version", version);
    deleteQuery.executeUpdate();

    Query insertQuery = entityManager.createNativeQuery(String.format(
      "INSERT INTO updates_%s_files (fileid, version, name, md5) VALUES (:fileId, :version, :name, :md5)", innerModName
    ));

    featuredModFiles.forEach(featuredModFile -> {
      insertQuery.setParameter("fileId", featuredModFile.getFileId());
      insertQuery.setParameter("version", featuredModFile.getVersion());
      insertQuery.setParameter("name", featuredModFile.getName());
      insertQuery.setParameter("md5", featuredModFile.getMd5());
      insertQuery.executeUpdate();
    });
  }

  @SuppressWarnings("unchecked")
  public Map<String, Short> getFileIds(String modName) {
    // Please shoot me.
    String innerModName = "ladder1v1".equals(modName) ? "faf" : modName;
    verifyModName(innerModName);

    Query query = entityManager.createNativeQuery(String.format("SELECT id, filename FROM updates_%s", innerModName));

    return ((List<Object[]>) query.getResultList()).stream()
      .collect(Collectors.toMap(row -> (String) row[1], row -> (short) row[0]));
  }

  private void verifyModName(String modName) {
    Assert.isTrue(MOD_NAME_PATTERN.matcher(modName).matches(), "Invalid mod name: " + modName);
  }
}
