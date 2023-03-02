package antifraud.repository;

import antifraud.entity.UserDetails;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface UserRepository extends CrudRepository<UserDetails, Long> {
    UserDetails findByUsernameIgnoreCase(String userName);

    //    @Query("select u from UserDetails u")
    List<UserDetails> findAll();


    @Query("delete from UserDetails u where upper(u.username) = upper(:username)")
    @Transactional
    @Modifying
    void deleteByUsernameIgnoreCase(@Param("username") String username);

    @Query(value = "SELECT MAX(id) FROM User_Details", nativeQuery = true)
    Long getMaxId();

    @Query(value = "SELECT * FROM User_Details ORDER BY id ASC ", nativeQuery = true)
    List<UserDetails> getListAuth();

    long count();


    @Transactional
    @Modifying
    @Query("update UserDetails u set u.role = :role where upper(u.username) = upper(:username)")
    int updateRoleByUsernameIgnoreCase(@Param("role") String role, @Param("username") String username);

}
