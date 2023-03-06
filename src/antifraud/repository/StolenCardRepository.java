package antifraud.repository;

import antifraud.entity.StolenCard;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface StolenCardRepository extends CrudRepository<StolenCard, Long> {
    StolenCard findByNumber(String number);

    @Transactional
    @Modifying
    @Query("delete from StolenCard s where s.number = :number")
    int deleteByNumber(@Param("number") String number);

    @Query("select s from StolenCard s order by s.id asc")
    @Override
    List<StolenCard> findAll();


}