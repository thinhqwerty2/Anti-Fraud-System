package antifraud.repository;

import antifraud.entity.Transaction;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TransactionRepository extends CrudRepository<Transaction, Long> {
    @Query("select t from Transaction t where t.number = :number")
    List<Transaction> findByNumber(@Param("number") String number);


    @Query("select t from Transaction t where t.number = :number order by t.date DESC")
    List<Transaction> findByNumberOrderByDateDesc(@Param("number") String number);
    Transaction save(Transaction entity);

    Optional<Transaction> findById(Long id);

    @Query("select t from Transaction t order by t.id")
    @Override
    List<Transaction> findAll();

    @Query("select t from Transaction t where t.date between :from and :to")
    List<Transaction> findByDateBetween(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);
}