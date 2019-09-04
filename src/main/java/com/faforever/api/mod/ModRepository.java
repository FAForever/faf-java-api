package com.faforever.api.mod;

import com.faforever.api.data.domain.Mod;
import com.faforever.api.data.domain.Player;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
@Transactional(propagation = Propagation.MANDATORY)
public interface ModRepository extends JpaRepository<Mod, Integer> {

//  @Query("select case when(count(m) > 0) then true else false end " +
//      "from Mod m where lower(m.displayName) = lower(:displayName) and m.uploader <> :uploader")
//  boolean modExistsByDifferentUser(@Param("displayName") String displayName, @Param("uploader") User user);

  boolean existsByDisplayNameAndUploaderIsNot(String displayName, Player uploader);

  /**
   * @deprecated get rid of this as soon as proper review mechanisms are in place.
   */
  @Deprecated
  @Query(value = "INSERT INTO mod_stats (mod_id, likers) " +
      "SELECT id, '' FROM `mod` " +
      "WHERE lower(display_name) = lower(:displayName)" +
      "AND NOT EXISTS (SELECT mod_id FROM mod_stats WHERE mod_id = id)", nativeQuery = true)
  @Modifying
  void insertModStats(@Param("displayName") String displayName);

  Optional<Mod> findOneByDisplayName(String name);
}
