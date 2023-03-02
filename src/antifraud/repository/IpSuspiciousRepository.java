package antifraud.repository;

import antifraud.entity.IpSuspicious;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface IpSuspiciousRepository extends CrudRepository<IpSuspicious, Long> {

    @Query("select i from IpSuspicious i where i.ip = :ip")
    IpSuspicious findByIp(@Param("ip") String ip);

    @Transactional
    @Modifying
    @Query("delete from IpSuspicious i where i.ip = :ip")
    int deleteByIp(@Param("ip") String ip);

    @Query("select s from IpSuspicious s order by s.id asc")
    @Override
    List<IpSuspicious> findAll();

}