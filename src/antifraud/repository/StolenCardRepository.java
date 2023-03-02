package antifraud.repository;

import antifraud.entity.StolenCard;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StolenCardRepository extends CrudRepository<StolenCard, Long> {
    StolenCard findByNumber(String number);

    int deleteByNumber(String number);

    @Query("select s from StolenCard s order by s.id asc")
    @Override
    List<StolenCard> findAll();


}